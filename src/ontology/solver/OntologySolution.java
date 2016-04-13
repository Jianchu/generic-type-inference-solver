package ontology.solver;

import java.util.Map;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.InferenceSolution;

public class OntologySolution implements InferenceSolution {
    private final Map<Integer, AnnotationMirror> result;

    public OntologySolution(Map<Integer, AnnotationMirror> result) {
        this.result = result;
    }

    @Override
    public boolean doesVariableExist(int varId) {
        return result.containsKey(varId);
    }

    @Override
    public AnnotationMirror getAnnotation(int varId) {
        return result.get(varId);
    }

}
