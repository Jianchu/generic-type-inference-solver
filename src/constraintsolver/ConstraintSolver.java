package constraintsolver;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.ErrorReporter;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import util.PrintUtils;
import util.StatisticPrinter;
import util.StatisticPrinter.StatisticKey;
import checkers.inference.DefaultInferenceSolution;
import checkers.inference.InferenceSolution;
import checkers.inference.InferenceSolver;
import checkers.inference.model.Constraint;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import constraintgraph.ConstraintGraph;
import constraintgraph.GraphBuilder;

/**
 * The default solver that could be called if there is no view adaptation
 * constraint in current type system.
 *
 * @author jianchu
 *
 */
public class ConstraintSolver implements InferenceSolver {

    public BackEnd realBackEnd;
    public String backEndType;
    public boolean useGraph;
    public boolean solveInParallel;
    protected Lattice lattice;

    // timing variables:
    private long graphBuildingStart;
    private long graphBuildingEnd;
    private long solvingStart;
    private long solvingEnd;

    @Override
    public InferenceSolution solve(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {
        // record constraint size
        StatisticPrinter.record(StatisticKey.CONSTRAINT_SIZE, (long) constraints.size());
        // record slot size
        StatisticPrinter.record(StatisticKey.SLOTS_SIZE, (long) slots.size());
        configure(configuration);
        configureLattice(qualHierarchy);
        Serializer<?, ?> defaultSerializer = createSerializer(backEndType, lattice);
        InferenceSolution solution;
        if (useGraph) {
            this.graphBuildingStart = System.currentTimeMillis();
            GraphBuilder graphBuilder = new GraphBuilder(slots, constraints);
            ConstraintGraph constraintGraph = graphBuilder.buildGraph();
            this.graphBuildingEnd = System.currentTimeMillis();
            StatisticPrinter.record(StatisticKey.GRAPH_GENERATION_TIME, (graphBuildingEnd - graphBuildingStart));
            this.solvingStart = System.currentTimeMillis();
            solution = graphSolve(constraintGraph, configuration, slots, constraints, qualHierarchy,
                    processingEnvironment, defaultSerializer);
            this.solvingEnd = System.currentTimeMillis();
        } else {
            realBackEnd = createBackEnd(backEndType, configuration, slots, constraints, qualHierarchy,
                    processingEnvironment, lattice, defaultSerializer);
            this.solvingStart = System.currentTimeMillis();
            solution = solve();
            this.solvingEnd = System.currentTimeMillis();
        }
        if (useGraph) {
            if (solveInParallel) {
                StatisticPrinter.record(StatisticKey.SAT_SOLVING_GRAPH_PARALLEL_TIME,
                        (solvingEnd - solvingStart));
            } else {
                StatisticPrinter.record(StatisticKey.SAT_SOLVING_GRAPH_SEQUENTIAL_TIME,
                        (solvingEnd - solvingStart));
            }
        } else {
            StatisticPrinter.record(StatisticKey.SAT_SOLVING_WITHOUT_GRAPH_TIME,
                    (solvingEnd - solvingStart));
        }
        PrintUtils.printStatistic(StatisticPrinter.getStatistic());
        PrintUtils.writeStatistic(StatisticPrinter.getStatistic());
        return solution;
    }
    
    private void configure(Map<String, String> configuration) {
        String backEndName = configuration.get("backEndType");
        String useGraph = configuration.get("useGraph");
        String solveInParallel = configuration.get("solveInParallel");
        if (backEndName == null) {
            this.backEndType = "maxsatbackend.MaxSat";
            // TODO: warning
            // ErrorReporter.errorAbort("not found back end.");
        } else {
            if (backEndName.equals("maxsatbackend.MaxSat") || backEndName.equals("logiqlbackend.LogiQL")
                    || backEndName.equals("General")) {
                this.backEndType = backEndName;
            } else {
                ErrorReporter.errorAbort("back end is not implemented yet.");
            }
        }

        if (useGraph == null || useGraph.equals("true")) {
            this.useGraph = true;
        } else {
            this.useGraph = false;
        }

        if (this.backEndType.equals("logiqlbackend.LogiQL")) {
            this.solveInParallel = false;
        } else if (solveInParallel == null || solveInParallel.equals("true")) {
            this.solveInParallel = true;
        } else {
            this.solveInParallel = false;
        }

        System.out.println("configuration: \nback end type: " + this.backEndType + "; \nuseGraph: "
                + this.useGraph + "; \nsolveInParallel: " + this.solveInParallel + ".");
    }

    protected void configureLattice(QualifierHierarchy qualHierarchy) {
        this.lattice = new Lattice(qualHierarchy);
        lattice.configure();
    }

    protected TwoQualifiersLattice configureLatticeFor2(AnnotationMirror top, AnnotationMirror bottom) {
        TwoQualifiersLattice latticeFor2 = new TwoQualifiersLattice(top, bottom);
        latticeFor2.configure();
        return latticeFor2;
    }

    protected InferenceSolution graphSolve(ConstraintGraph constraintGraph,
            Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, Serializer<?, ?> defaultSerializer) {
        System.out.println("Using ConstraintGraph!");
        List<BackEnd<?, ?>> backEnds = new ArrayList<BackEnd<?, ?>>();

        for (Set<Constraint> independentConstraints : constraintGraph.getIndependentPath()) {
            backEnds.add(createBackEnd(backEndType, configuration, slots, independentConstraints,
                    qualHierarchy, processingEnvironment, lattice, defaultSerializer));
        }
        return mergeSolution(solve(backEnds));
    }

    protected List<Map<Integer, AnnotationMirror>> solve(List<BackEnd<?, ?>> backEnds) {
        List<Map<Integer, AnnotationMirror>> inferenceSolutionMaps = new LinkedList<Map<Integer, AnnotationMirror>>();
        if (backEnds.size() > 0) {
            if (this.solveInParallel) {
                try {
                    inferenceSolutionMaps = solveInparallel(backEnds);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                inferenceSolutionMaps = solveInSequential(backEnds);
            }
        }
        return inferenceSolutionMaps;
    }

    protected List<Map<Integer, AnnotationMirror>> solveInSequential(List<BackEnd<?, ?>> backEnds) {
        List<Map<Integer, AnnotationMirror>> solutions = new ArrayList<>();

        for (final BackEnd backEnd : backEnds) {
            solutions.add(backEnd.solve());
        }
        return solutions;
    }

    protected List<Map<Integer, AnnotationMirror>> solveInparallel(List<BackEnd<?, ?>> backEnds)
            throws InterruptedException, ExecutionException {

        ExecutorService service = Executors.newFixedThreadPool(backEnds.size());

        List<Future<Map<Integer, AnnotationMirror>>> futures = new ArrayList<Future<Map<Integer, AnnotationMirror>>>();

        for (final BackEnd backEnd : backEnds) {
            Callable<Map<Integer, AnnotationMirror>> callable = new Callable<Map<Integer, AnnotationMirror>>() {
                @Override
                public Map<Integer, AnnotationMirror> call() throws Exception {
                    return backEnd.solve();
                }
            };
            futures.add(service.submit(callable));
        }
        service.shutdown();

        List<Map<Integer, AnnotationMirror>> solutions = new ArrayList<>();
        for (Future<Map<Integer, AnnotationMirror>> future : futures) {
            solutions.add(future.get());
        }
        return solutions;
    }

    protected InferenceSolution mergeSolution(List<Map<Integer, AnnotationMirror>> inferenceSolutionMaps) {
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        for (Map<Integer, AnnotationMirror> inferenceSolutionMap : inferenceSolutionMaps) {
            result.putAll(inferenceSolutionMap);
        }
        PrintUtils.printResult(result);
        return new DefaultInferenceSolution(result);
    }



    protected BackEnd createBackEnd(String backEndType, Map<String, String> configuration,
            Collection<Slot> slots, Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy, ProcessingEnvironment processingEnvironment,
            Lattice lattice, Serializer<?, ?> defaultSerializer) {
        BackEnd backEnd = null;
        try {
            Class<?> backEndClass = Class.forName(backEndType + "BackEnd");
            Constructor<?> cons = backEndClass.getConstructor(Map.class, Collection.class,
                    Collection.class, QualifierHierarchy.class, ProcessingEnvironment.class,
                    Serializer.class, Lattice.class);
            backEnd = (BackEnd) cons.newInstance(configuration, slots, constraints, qualHierarchy,
                    processingEnvironment, defaultSerializer, lattice);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorReporter.errorAbort("back end is not implemented yet.");
        }

        return backEnd;
    }

    protected Serializer<?, ?> createSerializer(String value, Lattice lattice) {
        return new ConstraintSerializer<>(value, lattice);
    }

    protected InferenceSolution solve() {
        PrintUtils.printResult(realBackEnd.solve());
        return new DefaultInferenceSolution(realBackEnd.solve());
    }
}