package generalconstraintsolver.dataflowsolver.dataflowsatsolver;

import java.util.List;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.WeightedMaxSatDecorator;

import checkers.inference.SlotManager;
import generalconstraintsolver.dataflowsolver.DataflowImpliesLogic;
import generalconstraintsolver.satsubsolver.SatSubSolver;


public class DatatypeSatSolver extends SatSubSolver {
    // private DataflowImpliesLogic logic;
    private int[] solution;

    public DatatypeSatSolver(DataflowImpliesLogic logic ,SlotManager slotManager) {
        super(logic.getLogics(), slotManager, logic.getLattice());
        // this.logic = logic;
    }

    public int[] dataflowsatSolve() {
        List<VecInt> clauses = convertImpliesToClauses();
        becomeWellForm(clauses);
        final int totalVars = (slotManager.getNumberOfSlots() * lattice.numModifiers);
        final int totalClauses = clauses.size() + slotManager.getNumberOfSlots()
                * (1 + (lattice.numModifiers * (lattice.numModifiers - 1) / 2));
        final WeightedMaxSatDecorator solver = new WeightedMaxSatDecorator(
                org.sat4j.pb.SolverFactory.newBoth());

        solver.newVar(totalVars);
        solver.setExpectedNumberOfClauses(totalClauses);
        solver.setTimeoutMs(1000000);
        // VecInt lastClause = null;

        try {
            for (VecInt clause : clauses) {
                // lastClause = clause;
                solver.addHardClause(clause);
            }

            if (solver.isSatisfiable()) {
                solution = solver.model();
            } else {
                System.out.println("Not solvable!");
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return solution;
    }
}
