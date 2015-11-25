package checkers.inference.solver.generalconstrainssolver.satsubsolver;

import java.util.List;

import checkers.inference.DefaultInferenceSolution;
import checkers.inference.InferenceSolution;
import checkers.inference.solver.generalconstrainssolver.GeneralConstrainsSolver;
import checkers.inference.solver.generalconstrainssolver.ImpliesLogic;

public class GeneralSatSolver extends GeneralConstrainsSolver {
    
    @Override
    protected InferenceSolution solve(){
        List<ImpliesLogic> allImpliesLogic = serializer.convertAll(constraints);
        System.out.println("From Sat:");
        SatSubSolver satSolver = new SatSubSolver(allImpliesLogic,slotManager,lattice);
        DefaultInferenceSolution satResult = (DefaultInferenceSolution) satSolver.satSolve();
        
        System.out.println("/*************the result from Sat Solver*******************/");
        for (Integer j : satResult.getVarIdToAnnotation().keySet()) {
            System.out.println("SlotID: " + j + "  " + "Annotation: "
                    + satResult.getAnnotation(j).toString());
        }
        System.out.flush();
        System.out.println("/**********************************************************/");
        return satResult;
    }
}
