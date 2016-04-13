package dataflow.solver;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

import checkers.inference.InferenceSolution;
import checkers.inference.InferenceSolver;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import dataflow.qual.DataFlow;
import dataflow.util.DataflowUtils;

public class DataflowSolver implements InferenceSolver {

    protected AnnotationMirror DATAFLOW;

    @Override
    public InferenceSolution solve(Map<String, String> configuration,
            Collection<Slot> slots, Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {

        Elements elements = processingEnvironment.getElementUtils();
        DATAFLOW = AnnotationUtils.fromClass(elements, DataFlow.class);

        Collection<String> datatypesUsed = getDatatypesUsed(slots);
        List<DatatypeSolver> dataflowSolvers = new ArrayList<>();

        // Configure datatype solvers
        for (String datatype : datatypesUsed) {
            DatatypeSolver solver = new DatatypeSolver(datatype, constraints, getSerializer(datatype));
            dataflowSolvers.add(solver);
        }

        // List<DatatypeSolution> solutions = new ArrayList<>();
        // for (DatatypeSolver solver : dataflowSolvers) {
        // solutions.add(solver.solve());
        // }

        List<DatatypeSolution> solutions = new ArrayList<>();
        try {
            solutions = solveInparallel(dataflowSolvers);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return getMergedSolution(processingEnvironment, solutions);
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

    private Collection<String> getDatatypesUsed(Collection<Slot> slots) {
        Set<String> types = new TreeSet<>();
        for (Slot slot : slots) {
            if (slot instanceof ConstantSlot) {
                ConstantSlot constantSlot = (ConstantSlot) slot;
                AnnotationMirror anno = constantSlot.getValue();
                if (AnnotationUtils.areSameIgnoringValues(anno, DATAFLOW)) {
                    String[] dataflowValues = DataflowUtils.getDataflowValue(anno);
                    for (String dataflowValue : dataflowValues) {
                        types.add(dataflowValue);
                    }
                }
            }
        }
        return types;
    }

    protected DataflowSerializer getSerializer(String datatype) {
        return new DataflowSerializer(datatype);
    }

    protected InferenceSolution getMergedSolution(ProcessingEnvironment processingEnvironment,
            List<DatatypeSolution> solutions) {
        return new DataflowSolution(solutions, processingEnvironment);
    }
}
