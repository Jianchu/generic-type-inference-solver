package checkers.inference.solver.generalconstrainssolver.logiqlsubsolver;

import java.util.List;

import checkers.inference.DefaultInferenceSolution;
import checkers.inference.InferenceSolution;
import checkers.inference.solver.generalconstrainssolver.GeneralConstrainsSolver;
import checkers.inference.solver.generalconstrainssolver.ImpliesLogic;

public class GeneralLogiqlSolver extends GeneralConstrainsSolver{
    @Override
    protected InferenceSolution solve(){
        List<ImpliesLogic> allImpliesLogic = serializer.convertAll(constraints);
        System.out.println("From Logiql:");
        LogiqlSubSolver logiqlSolver = new LogiqlSubSolver(allImpliesLogic, lattice);
        
        DefaultInferenceSolution logiqlResult =(DefaultInferenceSolution) logiqlSolver.logiqlSolve();
        
        System.out.println("/**********the result from Logiql Solver*******************/");
        for (Integer j: logiqlResult.getVarIdToAnnotation().keySet()){
            System.out.println("SlotID: "+j+ "  " + "Annotation: " + logiqlResult.getAnnotation(j).toString());
        }
        System.out.flush();
        System.out.println("/**********************************************************/");
        return logiqlResult;
    }
}
