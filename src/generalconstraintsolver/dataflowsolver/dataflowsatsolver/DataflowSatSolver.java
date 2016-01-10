package generalconstraintsolver.dataflowsolver.dataflowsatsolver;

import org.checkerframework.javacutil.AnnotationUtils;

import generalconstraintsolver.GeneralConstrainsSolver;
import generalconstraintsolver.GeneralEncodingSerializer;
import generalconstraintsolver.LatticeGenerator;
import generalconstraintsolver.dataflowsolver.DatatypeSolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

import checkers.inference.InferenceSolution;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Slot;
import dataflow.quals.DataFlow;
import dataflow.util.DataflowUtils;

public class DataflowSatSolver extends GeneralConstrainsSolver{
    
    protected AnnotationMirror DATAFLOW;
       
    @Override
    protected InferenceSolution solve(){
        Elements elements = processingEnvironment.getElementUtils();
        DATAFLOW = AnnotationUtils.fromClass(elements, DataFlow.class);
        
        Collection<String> datatypesUsed = getDatatypessUsed(slots);
        List<DatatypeSolver> dataflowSolvers = new ArrayList<>();
        
        for (String datatype : datatypesUsed) {
            Set<String> datatypeSet = new HashSet<String>();
            datatypeSet.add(datatype);
            AnnotationMirror dataflowAnnotation= DataflowUtils.createDataflowAnnotation(datatypeSet, processingEnvironment);
            LatticeGenerator lattice = new LatticeGenerator(dataflowAnnotation,processingEnvironment);
            GeneralEncodingSerializer serializer = new GeneralEncodingSerializer(slotManager, lattice);
            
            for (AnnotationMirror i : lattice.subType.keySet()) {
                System.out.println("key: " + i.toString() + "value: "
                        + lattice.modifierInt.get(i));
            }
            
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
                    String[] dataflowValues = DataflowUtils.getDataflowValue(anno);
                    for(String dataflowValue :dataflowValues){
                        types.add(dataflowValue);
                    }
                }
            }
        }
        return types;
    }
    
}
