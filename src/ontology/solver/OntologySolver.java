package ontology.solver;

import ontology.qual.Ontology;
import ontology.util.OntologyUtils;

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

public class OntologySolver implements InferenceSolver {

    protected AnnotationMirror Ontology;

    @Override
    public InferenceSolution solve(Map<String, String> configuration,
            Collection<Slot> slots, Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {

        Elements elements = processingEnvironment.getElementUtils();
        Ontology = AnnotationUtils.fromClass(elements, Ontology.class);

        Collection<String> datatypesUsed = getDatatypesUsed(slots);
        List<SequenceSolver> dataflowSolvers = new ArrayList<>();

        // Configure datatype solvers
        for (String datatype : datatypesUsed) {
            SequenceSolver solver = new SequenceSolver(datatype, constraints, getSerializer(datatype));
            dataflowSolvers.add(solver);
        }

        // List<DatatypeSolution> solutions = new ArrayList<>();
        // for (DatatypeSolver solver : dataflowSolvers) {
        // solutions.add(solver.solve());
        // }

        List<SequenceSolution> solutions = new ArrayList<>();
        try {
            solutions = solveInparallel(dataflowSolvers);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return getMergedSolution(processingEnvironment, solutions);
    }

    private List<SequenceSolution> solveInparallel(List<SequenceSolver> dataflowSolvers)
            throws InterruptedException, ExecutionException {
        ExecutorService service = Executors.newFixedThreadPool(dataflowSolvers.size());

        List<Future<SequenceSolution>> futures = new ArrayList<Future<SequenceSolution>>();

        for (final SequenceSolver solver : dataflowSolvers) {
            Callable<SequenceSolution> callable = new Callable<SequenceSolution>() {
                @Override
                public SequenceSolution call() throws Exception {
                    return solver.solve();
                }
            };
            futures.add(service.submit(callable));
        }
        service.shutdown();

        List<SequenceSolution> solutions = new ArrayList<>();
        for (Future<SequenceSolution> future : futures) {
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
                if (AnnotationUtils.areSameIgnoringValues(anno, Ontology)) {
                    String[] dataflowValues = OntologyUtils.getOntologyValue(anno);
                    for (String dataflowValue : dataflowValues) {
                        types.add(dataflowValue);
                    }
                }
            }
        }
        return types;
    }

    protected OntologySerializer getSerializer(String datatype) {
        return new OntologySerializer(datatype);
    }

    protected InferenceSolution getMergedSolution(ProcessingEnvironment processingEnvironment,
            List<SequenceSolution> solutions) {
        return new OntologySolution(solutions, processingEnvironment);
    }
}
