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
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
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
    public boolean collectStatistic;
    protected Lattice lattice;
    protected ConstraintGraph constraintGraph;

    // timing variables:
    private long graphBuildingStart;
    private long graphBuildingEnd;

    private long solvingStart;
    private long solvingEnd;

    @Override
    public InferenceSolution solve(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {
        InferenceSolution solution = null;
        configure(configuration);
        configureLattice(qualHierarchy);
        Serializer<?, ?> defaultSerializer = createSerializer(backEndType, lattice);
        if (useGraph) {
            this.graphBuildingStart = System.currentTimeMillis();
            this.constraintGraph = generateGraph(slots, constraints, processingEnvironment);
            this.graphBuildingEnd = System.currentTimeMillis();
            StatisticPrinter.record(StatisticKey.GRAPH_GENERATION_TIME,
                    (graphBuildingEnd - graphBuildingStart));
            solution = graphSolve(constraintGraph, configuration, slots, constraints, qualHierarchy,
                    processingEnvironment, defaultSerializer);
        } else {
            realBackEnd = createBackEnd(backEndType, configuration, slots, constraints, qualHierarchy,
                    processingEnvironment, lattice, defaultSerializer);
            solution = solve();
        }
        
        if (solution == null)
            ErrorReporter.errorAbort("null solution detected!");
        if (collectStatistic) {
            countSlotConstraint(slots, constraints);
            PrintUtils.printStatistic(StatisticPrinter.getStatistic(), StatisticPrinter.getThreadsData());
            PrintUtils.writeStatistic(StatisticPrinter.getStatistic(), StatisticPrinter.getThreadsData());
        }
        return solution;
    }

    /**
     * Sanitize and apply check of the configuration of solver based on a specific type system.
     * Sub-class solver of a specific type system may override this method to sanitize the configuration of solver
     * in the context of that type system.
     */
    protected void sanitizeConfiguration() {

    }

    private void countSlotConstraint(Collection<Slot> slots, Collection<Constraint> constraints) {
        // record constraint size
        StatisticPrinter.record(StatisticKey.CONSTRAINT_SIZE, (long) constraints.size());
        // record slot size
        StatisticPrinter.record(StatisticKey.SLOTS_SIZE, (long) slots.size());
        Map<String, Integer> slotMap = new HashMap<>();
        Map<String, Integer> constraintMap = new HashMap<>();
        for (Constraint constraint : constraints) {
            String simpleName = constraint.getClass().getSimpleName();
            if (!constraintMap.containsKey(simpleName)) {
                constraintMap.put(simpleName, 1);
            } else {
                constraintMap.put(simpleName, constraintMap.get(simpleName) + 1);
            }
        }

        for (Slot slot : slots) {
            if (slot instanceof ConstantSlot) {
                if (!slotMap.containsKey("ConstantSlot")) {
                    slotMap.put("ConstantSlot", 1);
                } else {
                    slotMap.put("ConstantSlot", slotMap.get("ConstantSlot") + 1);
                }

            } else if (slot instanceof VariableSlot) {
                if (!slotMap.containsKey("VariableSlot")) {
                    slotMap.put("VariableSlot", 1);
                } else {
                    slotMap.put("VariableSlot", slotMap.get("VariableSlot") + 1);
                }
            }
        }
        System.out.println(slotMap);
        System.out.println(constraintMap);
    }

    protected ConstraintGraph generateGraph(Collection<Slot> slots, Collection<Constraint> constraints,
            ProcessingEnvironment processingEnvironment) {
        GraphBuilder graphBuilder = new GraphBuilder(slots, constraints);
        ConstraintGraph constraintGraph = graphBuilder.buildGraph();
        return constraintGraph;
    }

    private void configure(Map<String, String> configuration) {
        String backEndName = configuration.get("backEndType");
        String useGraph = configuration.get("useGraph");
        String solveInParallel = configuration.get("solveInParallel");
        String collectStatistic = configuration.get("collectStatistic");
        if (backEndName == null) {
            this.backEndType = "maxsatbackend.MaxSat";
            // TODO: warning
            // ErrorReporter.errorAbort("not found back end.");
        } else {
            if (backEndName.equals("maxsatbackend.MaxSat") || backEndName.equals("logiqlbackend.LogiQL")
                    || backEndName.equals("General") || backEndName.equals("maxsatbackend.Lingeling")) {
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

        if (collectStatistic == null || collectStatistic.equals("false")) {
            this.collectStatistic = false;
        } else if (collectStatistic.equals("true")) {
            this.collectStatistic = true;
        }

        // sanitize the configuration if needs
        sanitizeConfiguration();
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
        StatisticPrinter.record(StatisticKey.GRAPH_SIZE, (long) constraintGraph.getIndependentPath().size());
        for (Set<Constraint> independentConstraints : constraintGraph.getIndependentPath()) {
            backEnds.add(createBackEnd(backEndType, configuration, slots, independentConstraints,
                    qualHierarchy, processingEnvironment, lattice, defaultSerializer));
        }
        constraintGraph = null;
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
        solvingStart = System.currentTimeMillis();
        for (final BackEnd backEnd : backEnds) {
            solutions.add(backEnd.solve());
        }
        solvingEnd = System.currentTimeMillis();
        StatisticPrinter.record(StatisticKey.SAT_SOLVING_GRAPH_SEQUENTIAL_TIME, (solvingEnd - solvingStart));
        return solutions;
    }

    protected List<Map<Integer, AnnotationMirror>> solveInparallel(List<BackEnd<?, ?>> backEnds)
            throws InterruptedException, ExecutionException {

        ExecutorService service = Executors.newFixedThreadPool(30);

        List<Future<Map<Integer, AnnotationMirror>>> futures = new ArrayList<Future<Map<Integer, AnnotationMirror>>>();
        solvingStart = System.currentTimeMillis();

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
        solvingEnd = System.currentTimeMillis();
        StatisticPrinter.record(StatisticKey.SAT_SOLVING_GRAPH_PARALLEL_TIME, (solvingEnd - solvingStart));
        return solutions;
    }

    protected InferenceSolution mergeSolution(List<Map<Integer, AnnotationMirror>> inferenceSolutionMaps) {
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        for (Map<Integer, AnnotationMirror> inferenceSolutionMap : inferenceSolutionMaps) {
            result.putAll(inferenceSolutionMap);
        }
        PrintUtils.printResult(result);
        StatisticPrinter.record(StatisticKey.NUMBER_ANNOTATOIN, (long) result.size());
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
        solvingStart = System.currentTimeMillis();
        Map<Integer, AnnotationMirror> result = realBackEnd.solve();
        solvingEnd = System.currentTimeMillis();
        StatisticPrinter.record(StatisticKey.NEW_SAT_NO_GRAPH_SOLVING,
                (solvingEnd - solvingStart));
        StatisticPrinter.record(StatisticKey.NUMBER_ANNOTATOIN, (long) result.size());
        PrintUtils.printResult(result);
        return new DefaultInferenceSolution(result);
    }
}