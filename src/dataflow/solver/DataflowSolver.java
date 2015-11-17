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
import sparta.checkers.sat.IFlowSerializer;
import sparta.checkers.sat.PermissionSolution;
import sparta.checkers.sat.PermissionSolver;
import sparta.checkers.sat.SinkSolution;
import sparta.checkers.sat.SourceSerializer;
import checkers.inference.InferenceSolution;
import checkers.inference.InferenceSolver;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import dataflow.quals.DataFlow;
import dataflow.util.DataflowUtils;

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
        List<DatatypeSolver> dataflowSolvers = new ArrayList<>();
        
        //Configure datatype solvers
        for (String datatype : datatypesUsed) {
            DatatypeSolver solver = new DatatypeSolver(datatype);
            solver.configure(constraints, getSerializer(datatype));
            dataflowSolvers.add(solver);
        }
        
        List<DataflowSolution> solutions = new ArrayList<>();
        for (DatatypeSolver solver : dataflowSolvers) {
            solutions.add(solver.solve());
        }
        
        return getMergedSolution(processingEnvironment, solutions);
}
    
    
    
    private Collection<String> getDatatypessUsed(Collection<Slot> solts) {
        Set<String> types = new TreeSet<>();
        for (Slot slot : solts) {
            if (slot instanceof ConstantSlot) {
                ConstantSlot constantSlot = (ConstantSlot) slot;
                AnnotationMirror anno = constantSlot.getValue();
                if (AnnotationUtils.areSameIgnoringValues(anno, DATAFLOW)) {
                    System.out.println(anno.toString());
                    String[] dataflowValues = DataflowUtils.getDataflowValue(anno);
                    for(String dataflowValue :dataflowValues){
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
        
    protected InferenceSolution getMergedSolution(ProcessingEnvironment processingEnvironment, List<DataflowSolution> solutions) {
        return new SinkSolution(solutions, processingEnvironment);
    }
}
