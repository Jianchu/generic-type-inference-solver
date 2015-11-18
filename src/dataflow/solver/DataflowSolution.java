package dataflow.solver;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.inference.InferenceSolution;

public class DataflowSolution implements InferenceSolution{
    Map<Integer, Set<String>> results;
    Map<Integer, Boolean> idToExistance;
    Map<Integer, AnnotationMirror> annotationResults;
    
    public DataflowSolution(Collection<DatatypeSolution> solutions, ProcessingEnvironment processingEnv) {
        this.results = new HashMap<>();
        this.idToExistance = new HashMap<>();
        merge(solutions);
    }

    public void merge(Collection<DatatypeSolution> solutions){
        for (DatatypeSolution solution : solutions) {
            mergeResults(solution);
            mergeIdToExistance(solution);
        }
    }
    
    private void mergeIdToExistance(DatatypeSolution solution) {
        // TODO Auto-generated method stub
        
    }

    private void mergeResults(DatatypeSolution solution) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Map<Integer, AnnotationMirror> getVarIdToAnnotation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Integer, Boolean> getIdToExistance() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean doesVariableExist(int varId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public AnnotationMirror getAnnotation(int varId) {
        // TODO Auto-generated method stub
        return null;
    }

}
