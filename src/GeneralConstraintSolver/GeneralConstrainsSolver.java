package checkers.inference.solver;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.WeightedMaxSatDecorator;

import checkers.inference.InferenceMain;
import checkers.inference.InferenceSolution;
import checkers.inference.InferenceSolver;
import checkers.inference.SlotManager;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;

public class GeneralConstrainsSolver implements InferenceSolver {
    private Collection<Constraint> constraints;
    private GeneralEncodingSerializer serializer;
    private SlotManager slotManager;
    private LatticeGenerator lattice;
    
    
    @Override
    public InferenceSolution solve(Map<String, String> configuration,
            Collection<Slot> slots, Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {
        this.constraints = constraints;
        this.slotManager = InferenceMain.getInstance().getSlotManager();
        this.lattice = new LatticeGenerator(qualHierarchy);
        this.serializer = new GeneralEncodingSerializer(slotManager, lattice);
        // TODO Auto-generated method stub
        return solve();
    }
    
    private InferenceSolution solve() {
        List<ImpliesLogic> allImpliesLogic = serializer.convertAll(constraints);
        SatSubSolver satSover = new SatSubSolver(allImpliesLogic,slotManager,lattice);
//        for (ImpliesLogic res :allImpliesLogic ){
//            if (res.singleVariable == true){
//                System.out.println("just: " + res.variable);
//            }
//            else{
//                System.out.println("left: " + res.leftSide.toString()+ " ---> " + "right: " + res.rightSide.toString());
//            }
//        }
        return null;
    }
}
