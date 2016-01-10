package generalconstraintsolver.satsubsolver;

import generalconstraintsolver.GeneralConstrainsSolver;
import generalconstraintsolver.GeneralEncodingSerializer;
import generalconstraintsolver.ImpliesLogic;
import generalconstraintsolver.LatticeGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.DefaultInferenceSolution;
import checkers.inference.InferenceSolution;

public class GeneralSatSolver extends GeneralConstrainsSolver {
    
    @Override
    protected InferenceSolution solve(){
        LatticeGenerator lattice = new LatticeGenerator(qualHierarchy);
        GeneralEncodingSerializer serializer = new GeneralEncodingSerializer(slotManager, lattice);
        List<ImpliesLogic> allImpliesLogic = serializer.convertAll(constraints);
        
        Map<Integer, Boolean> idToExistence = new HashMap<>();

        SatSubSolver satSolver = new SatSubSolver(allImpliesLogic,slotManager,lattice);
        Map<Integer, AnnotationMirror> satResult = satSolver.satSolve()
                .getVarIdToAnnotation();
        
        System.out.println("From Sat:");        
        System.out.println("/*************the result from Sat Solver*******************/");
        for (Integer j : satResult.keySet()) {
            System.out.println("SlotID: " + j + "  " + "Annotation: "
                    + satResult.get(j).toString());
        }
        System.out.flush();
        System.out.println("/**********************************************************/");
        
        return new DefaultInferenceSolution(satResult, idToExistence);
    }
}
