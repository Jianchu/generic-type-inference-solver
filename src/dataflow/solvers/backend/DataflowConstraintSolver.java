package dataflow.solvers.backend;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import checkers.inference.DefaultInferenceSolution;
import checkers.inference.InferenceSolution;
import checkers.inference.model.Constraint;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import constraintgraph.ConstraintGraph;
import constraintgraph.Vertex;
import constraintsolver.ConstraintSerializer;
import constraintsolver.ConstraintSolver;
import dataflow.qual.DataFlow;
import dataflow.solvers.DataflowSerializer;
import dataflow.solvers.DataflowSolution;
import dataflow.solvers.DatatypeSolution;
import dataflow.solvers.DatatypeSolver;
import dataflow.util.DataflowUtils;

public class DataflowConstraintSolver extends ConstraintSolver {

    protected AnnotationMirror DATAFLOW;

    @Override
    protected InferenceSolution graphSolve(ConstraintGraph constraintGraph,
            Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, Serializer<?, ?> defaultSerializer) {
        DATAFLOW = AnnotationUtils.fromClass(processingEnvironment.getElementUtils(), DataFlow.class);
        List<DatatypeSolver> dataflowSolvers = new ArrayList<>();

        for (Map.Entry<Vertex, Set<Constraint>> entry : constraintGraph.getIndependentPath().entrySet()) {
            AnnotationMirror anno = entry.getKey().getValue();
            if (AnnotationUtils.areSameIgnoringValues(anno, DATAFLOW)) {
                String[] dataflowValues = DataflowUtils.getTypeNames(anno);
                String[] dataflowRoots = DataflowUtils.getTypeNameRoots(anno);
                if (dataflowValues.length == 1) {
                    DatatypeSolver solver = new DatatypeSolver(dataflowValues[0], entry.getValue(),
                            getSerializer(dataflowValues[0], false));
                    dataflowSolvers.add(solver);
                } else if (dataflowRoots.length == 1) {
                    DatatypeSolver solver = new DatatypeSolver(dataflowRoots[0], entry.getValue(),
                            getSerializer(dataflowRoots[0], true));
                    dataflowSolvers.add(solver);
                }
            }
        }
        List<DatatypeSolution> solutions = new ArrayList<>();
        try {
            if (dataflowSolvers.size() > 0) {
                solutions = solveInparallel(dataflowSolvers);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return getMergedSolution(processingEnvironment, solutions);
    }

    @Override
    protected InferenceSolution mergeSolution(List<Map<Integer, AnnotationMirror>> inferenceSolutionMaps) {
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        for (Map<Integer, AnnotationMirror> inferenceSolutionMap : inferenceSolutionMaps) {
            result.putAll(inferenceSolutionMap);
        }
        PrintUtils.printResult(result);
        return new DefaultInferenceSolution(result);
    }

    @Override
    protected Serializer<?, ?> createSerializer(String value) {
        return new ConstraintSerializer<>(value, lattice);
    }

    private List<DatatypeSolution> solveInparallel(List<DatatypeSolver> dataflowSolvers)
            throws InterruptedException, ExecutionException {
        ExecutorService service = Executors.newFixedThreadPool(dataflowSolvers.size());

        List<Future<DatatypeSolution>> futures = new ArrayList<Future<DatatypeSolution>>();

        for (final DatatypeSolver solver : dataflowSolvers) {
            Callable<DatatypeSolution> callable = new Callable<DatatypeSolution>() {
                @Override
                public DatatypeSolution call() throws Exception {
                    return solver.solve();
                }
            };
            futures.add(service.submit(callable));
        }
        service.shutdown();

        List<DatatypeSolution> solutions = new ArrayList<>();
        for (Future<DatatypeSolution> future : futures) {
            solutions.add(future.get());
        }
        return solutions;
    }

    private DataflowSerializer getSerializer(String datatype, boolean isRoot) {
        return new DataflowSerializer(datatype, isRoot);
    }

    private InferenceSolution getMergedSolution(ProcessingEnvironment processingEnvironment,
            List<DatatypeSolution> solutions) {
        return new DataflowSolution(solutions, processingEnvironment);
    }
}
