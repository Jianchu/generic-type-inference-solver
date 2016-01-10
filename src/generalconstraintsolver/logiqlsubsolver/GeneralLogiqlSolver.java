package generalconstraintsolver.logiqlsubsolver;

import generalconstraintsolver.GeneralConstrainsSolver;
import generalconstraintsolver.GeneralEncodingSerializer;
import generalconstraintsolver.ImpliesLogic;
import generalconstraintsolver.LatticeGenerator;

import java.util.List;

import checkers.inference.DefaultInferenceSolution;
import checkers.inference.InferenceSolution;

public class GeneralLogiqlSolver extends GeneralConstrainsSolver{
    @Override
    protected InferenceSolution solve(){
        LatticeGenerator lattice = new LatticeGenerator(qualHierarchy);
        GeneralEncodingSerializer serializer = new GeneralEncodingSerializer(slotManager, lattice);
        List<ImpliesLogic> allImpliesLogic = serializer.convertAll(constraints);
        LogiqlSubSolver logiqlSolver = new LogiqlSubSolver(allImpliesLogic, lattice);        
        DefaultInferenceSolution logiqlResult =(DefaultInferenceSolution) logiqlSolver.logiqlSolve();
        
        System.out.println("From Logiql:");
        System.out.println("/**********the result from Logiql Solver*******************/");
        for (Integer j: logiqlResult.getVarIdToAnnotation().keySet()){
            System.out.println("SlotID: "+j+ "  " + "Annotation: " + logiqlResult.getAnnotation(j).toString());
        }
        System.out.flush();
        System.out.println("/**********************************************************/");
        
        return logiqlResult;
    }
}
