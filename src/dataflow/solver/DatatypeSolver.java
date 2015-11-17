package dataflow.solver;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.WeightedMaxSatDecorator;

import checkers.inference.InferenceMain;
import checkers.inference.SlotManager;
import checkers.inference.model.Constraint;

public class DatatypeSolver {
    private SlotManager slotManager;
    private String datatype;
    private DataflowSerializer serializer;
    List<VecInt> clauses;

    public DatatypeSolver(String datatype) {
        this.datatype = datatype;
    }

    public void configure(Collection<Constraint> constraints, DataflowSerializer serializer) {
        this.serializer = serializer;
        this.slotManager = InferenceMain.getInstance().getSlotManager();
        this.clauses = convertToCNF(constraints);
    }

    private List<VecInt> convertToCNF(Collection<Constraint> constraints) {
        return serializer.convertAll(constraints);
    }

    public PermissionSolution solve() {

        Map<Integer, Boolean> idToExistence = new HashMap<>();
        Map<Integer, Boolean> result = new HashMap<>();


        final int totalVars = slotManager.nextId();
        final int totalClauses = clauses.size();

        try {
            //**** Prep Solver ****
            //org.sat4j.pb.SolverFactory.newBoth() Runs both of sat4j solves and uses the result of the first to finish
            final WeightedMaxSatDecorator solver = new WeightedMaxSatDecorator(org.sat4j.pb.SolverFactory.newBoth());

            solver.newVar(totalVars);
            solver.setExpectedNumberOfClauses(totalClauses);
            //Arbitrary timeout
            solver.setTimeoutMs(1000000);
            for (VecInt clause : clauses) {
                solver.addSoftClause(clause);
            }

            //**** Solve ****
            boolean hasSolution = solver.isSatisfiable();

            if (hasSolution) {

                //**** Remove exatential vars from solution
                final Map<Integer, Integer> existentialToPotentialIds = serializer.getExistentialToPotentialVar();
                int[] solution = solver.model();

                for (Integer var : solution) {
                    boolean varIsTrue = !(var < 0);
                    //Need postive var
                    var = Math.abs(var);

                    Integer potential = existentialToPotentialIds.get(var);
                    if (potential != null) {
                        idToExistence.put(potential, varIsTrue);
                    } else {
                        result.put(var, varIsTrue);
                    }
                }
                return new PermissionSolution(result, idToExistence, datatype);
            }

        } catch (Throwable th) {
            VecInt lastClause = clauses.get(clauses.size() - 1);
            throw new RuntimeException("Error MAX-SAT solving! " + lastClause, th);
        }

        return PermissionSolution.noSolution(datatype);
    }
}
