package generalconstraintsolver.dataflowsolver.dataflowsatsolver;

import generalconstraintsolver.dataflowsolver.DataflowImpliesLogic;
import generalconstraintsolver.satsubsolver.SatSubSolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.WeightedMaxSatDecorator;

import checkers.inference.SlotManager;


public class DatatypeSatSolver extends SatSubSolver {
    private DataflowImpliesLogic logic;
    private int[] solution;
    public DatatypeSatSolver(DataflowImpliesLogic logic ,SlotManager slotManager) {
        super(logic.getLogics(), slotManager, logic.getLattice());
        this.logic = logic;
    }

    public int[] dataflowsatSolve() {
        Map<Integer, Boolean> idToExistence = new HashMap<>();
        Map<Integer, AnnotationMirror> result = new HashMap<>();
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
        VecInt lastClause = null;
        try {
            for (VecInt clause : clauses) {
                lastClause = clause;
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
        return getSolution();
    }

    public int[] getSolution() {
        return this.solution;
    }
}
