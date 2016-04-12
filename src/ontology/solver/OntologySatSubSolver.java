package ontology.solver;

import java.util.List;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.WeightedMaxSatDecorator;

import checkers.inference.SlotManager;
import generalconstraintsolver.ImpliesLogic;
import generalconstraintsolver.LatticeGenerator;
import generalconstraintsolver.satsubsolver.SatSubSolver;

public class OntologySatSubSolver extends SatSubSolver {
    private int[] solution;

    public OntologySatSubSolver(LatticeGenerator lattice, List<ImpliesLogic> logic,
            SlotManager slotManager) {
        super(logic, slotManager, lattice);
    }

    public int[] solve() {
        List<VecInt> clauses = convertImpliesToClauses();
        becomeWellForm(clauses);
        final int totalVars = (slotManager.nextId() * lattice.numModifiers);
        final int totalClauses = clauses.size() + slotManager.nextId()
                * (1 + (lattice.numModifiers * (lattice.numModifiers - 1) / 2));
        final WeightedMaxSatDecorator solver = new WeightedMaxSatDecorator(
                org.sat4j.pb.SolverFactory.newBoth());

        solver.newVar(totalVars);
        solver.setExpectedNumberOfClauses(totalClauses);
        solver.setTimeoutMs(1000000);

        try {
            for (VecInt clause : clauses) {
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
