package generalconstraintsolver.logiqlsubsolver;

import java.util.List;

import checkers.inference.DefaultInferenceSolution;
import checkers.inference.InferenceSolution;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import generalconstraintsolver.GeneralConstrainsSolver;
import generalconstraintsolver.GeneralEncodingSerializer;
import generalconstraintsolver.ImpliesLogic;
import generalconstraintsolver.LatticeGenerator;

public class GeneralLogiqlSolver extends GeneralConstrainsSolver {
    @Override
    protected InferenceSolution solve() {
        LatticeGenerator lattice = new LatticeGenerator(qualHierarchy);
        GeneralEncodingSerializer serializer = new GeneralEncodingSerializer(slotManager, lattice);
        List<ImpliesLogic> allImpliesLogic = serializer.convertAll(constraints);
        LogiqlSubSolver logiqlSolver = new LogiqlSubSolver(allImpliesLogic, lattice);
        DefaultInferenceSolution logiqlResult =(DefaultInferenceSolution) logiqlSolver.logiqlSolve();

        System.out.println("From Logiql:");
        System.out.println("/**********the result from Logiql Solver*******************/");
        for (Slot s : this.slots) {
            if (s.getKind() == Slot.Kind.VARIABLE) {
                VariableSlot vs = (VariableSlot) s;
                System.out.println("SlotID: " + vs.getId() + "  " + "Annotation: "
                        + logiqlResult.getAnnotation(vs.getId()).toString());
            }
        }
        System.out.flush();
        System.out.println("/**********************************************************/");

        return logiqlResult;
    }
}
