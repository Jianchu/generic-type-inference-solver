package ontology.solver;

import ontology.util.OntologyUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.inference.InferenceMain;
import checkers.inference.InferenceSolution;

public class OntologySolution implements InferenceSolution {
    private final Map<Integer, Set<String>> results;
    private final Map<Integer, Boolean> idToExistance;
    private final Map<Integer, AnnotationMirror> annotationResults;

    public OntologySolution(Collection<SequenceSolution> solutions, ProcessingEnvironment processingEnv) {
        this.results = new HashMap<>();
        this.idToExistance = new HashMap<>();
        this.annotationResults = new HashMap<>();

        merge(solutions);
        createAnnotations(processingEnv);

        System.out.println("FINAL RESULT FROM ONTOLOGYSOVLER: " + annotationResults.toString());
    }

    public void merge(Collection<SequenceSolution> solutions) {
        for (SequenceSolution solution : solutions) {
            mergeResults(solution);
            mergeIdToExistance(solution);
        }
    }

    private void mergeResults(SequenceSolution solution) {
        for (Map.Entry<Integer, Boolean> entry : solution.getResult().entrySet()) {
            boolean shouldContainValue = shouldContainValue(entry);
            String value = solution.getValue();
            if (!value.equals("")) {
                Set<String> values = results.get(entry.getKey());
                if (values == null) {
                    values = new TreeSet<>();
                    results.put(entry.getKey(), values);
                }

                if (shouldContainValue) {
                    values.add(value);
                }
            }
        }
    }

    protected boolean shouldContainValue(Map.Entry<Integer, Boolean> entry) {
        return entry.getValue();
    }

    private void createAnnotations(ProcessingEnvironment processingEnv) {
        for (Map.Entry<Integer, Set<String>> entry : results.entrySet()) {
            int slotId = entry.getKey();
            Set<String> values = entry.getValue();
            AnnotationMirror anno = createAnnotationFromValues(processingEnv, values);
            annotationResults.put(slotId, anno);
        }
    }

    protected AnnotationMirror createAnnotationFromValues(ProcessingEnvironment processingEnv,
            Set<String> values) {
        return OntologyUtils.createOntologyAnnotation(values, processingEnv);
    }


    private void mergeIdToExistance(SequenceSolution solution) {
        for (Map.Entry<Integer, Boolean> entry : solution.getResult().entrySet()) {
            int id = entry.getKey();
            boolean existsValue = entry.getValue();
            if (idToExistance.containsKey(id)) {
                boolean alreadyExists = idToExistance.get(id);
                if (alreadyExists ^ existsValue) {
                    InferenceMain.getInstance().logger.log(Level.INFO, "Mismatch between existance of annotation");
                }
            } else {
                idToExistance.put(id, existsValue);
            }
        }
    }

    @Override
    public boolean doesVariableExist(int varId) {
        return idToExistance.containsKey(varId);
    }

    @Override
    public AnnotationMirror getAnnotation(int varId) {
        return annotationResults.get(varId);
    }

}
