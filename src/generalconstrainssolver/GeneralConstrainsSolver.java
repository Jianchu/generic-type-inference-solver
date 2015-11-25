package checkers.inference.solver.generalconstrainssolver;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.util.Collection;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.inference.InferenceMain;
import checkers.inference.InferenceSolution;
import checkers.inference.InferenceSolver;
import checkers.inference.SlotManager;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;

public abstract class GeneralConstrainsSolver implements InferenceSolver {
    protected Collection<Constraint> constraints;
    protected GeneralEncodingSerializer serializer;
    protected SlotManager slotManager;
    protected LatticeGenerator lattice;
    
    
    @Override
    public InferenceSolution solve(Map<String, String> configuration,
            Collection<Slot> slots, Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {
        this.constraints = constraints;
        this.slotManager = InferenceMain.getInstance().getSlotManager();
        this.lattice = new LatticeGenerator(qualHierarchy);
        this.serializer = new GeneralEncodingSerializer(slotManager, lattice);
        return solve();
    }
    
    protected abstract InferenceSolution solve(); 
//    {
//        List<ImpliesLogic> allImpliesLogic = serializer.convertAll(constraints);
//        System.out.println("From Sat:");
//        SatSubSolver satSolver = new SatSubSolver(allImpliesLogic,slotManager,lattice);
//        System.out.println("From Logiql:");
//        LogiqlSubSolver logiqlSolver = new LogiqlSubSolver(allImpliesLogic, lattice);
////        for (ImpliesLogic res :allImpliesLogic ){
////            if (res.singleVariable == true){
////                System.out.println("just: " + res.variable);
////            }
////            else{
////                System.out.println("left: " + res.leftSide.toString()+ " ---> " + "right: " + res.rightSide.toString());
////            }
////        }
//        return null;
//    }
}
