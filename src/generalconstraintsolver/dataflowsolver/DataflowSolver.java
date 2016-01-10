package generalconstraintsolver.dataflowsolver;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

import checkers.inference.InferenceSolution;
import checkers.inference.InferenceSolver;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import dataflow.quals.DataFlow;
import dataflow.util.DataflowUtils;


public class DataflowSolver implements InferenceSolver {
    
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
        
        List<DatatypeSolution> solutions = new ArrayList<>();
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
        
    protected InferenceSolution getMergedSolution(ProcessingEnvironment processingEnvironment, List<DatatypeSolution> solutions) {
        return new DataflowSolution(solutions, processingEnvironment);
    }
}
