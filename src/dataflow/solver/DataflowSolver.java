package dataflow.solver;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;

import sparta.checkers.iflow.util.PFPermission;
import sparta.checkers.sat.PermissionSolution;
import sparta.checkers.sat.PermissionSolver;
import sparta.checkers.sat.SinkSolution;
import checkers.inference.InferenceSolution;
import checkers.inference.InferenceSolver;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import dataflow.quals.DataFlow;

public class DataflowSolver implements InferenceSolver{
    
    protected AnnotationMirror DATAFLOW;
    
    public InferenceSolution solve(Map<String, String> configuration,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {
        
        Elements elements = processingEnvironment.getElementUtils();
        DATAFLOW = AnnotationUtils.fromClass(elements, DataFlow.class);
        
        Collection<String> datatypesUsed = getDatatypessUsed(slots);
        List<DataflowSolution> dataflowSolvers = new ArrayList<>();
        
        //Configure permission solvers
        for (String datatype : datatypesUsed) {
            DatatypeSolver solver = new DatatypeSolver(datatype);
            solver.configure(constraints, getSerializer(permission));
            permissionSolvers.add(solver);
        }
        
        
        return null;
}
    
    
    
    private Collection<String> getDatatypessUsed(Collection<Slot> solts) {
        Set<String> types = new TreeSet<>();
        for (Slot slot : solts) {
            if (slot instanceof ConstantSlot) {
                ConstantSlot constantSlot = (ConstantSlot) slot;
                AnnotationMirror anno = constantSlot.getValue();
                if (AnnotationUtils.areSameIgnoringValues(anno, DATAFLOW)) {
                    System.out.println(anno.toString());
                    String[] dataflowValues = getDataflowValue(anno);
                    for(String dataflowValue :dataflowValues){
                        types.add(dataflowValue);
                    }
                }
            }
        }
        return types;
    }
    
    private String[] getDataflowValue(AnnotationMirror type) {
        List<String> allTypesList = AnnotationUtils.getElementValueArray(type,"typeNames", String.class, true);
        //types in this list is org.checkerframework.framework.util.AnnotationBuilder.
        String[] allTypesInArray = new String[allTypesList.size()];
        int i = 0;
        for (Object o :allTypesList){
            allTypesInArray[i] = o.toString();
            i++;
        }
        return allTypesInArray;
    }
    
    protected InferenceSolution getMergedSolution(ProcessingEnvironment processingEnvironment, List<PermissionSolution> solutions) {
        return new SinkSolution(solutions, processingEnvironment);
    }
}
