package ontology.solver;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.WeightedMaxSatDecorator;

import checkers.inference.InferenceMain;
import checkers.inference.SlotManager;
import checkers.inference.model.Constraint;

public class SequenceSolver {
    private final SlotManager slotManager;
    private final String value;
    private final OntologySerializer serializer;
    private final List<VecInt> clauses;

    public SequenceSolver(String value, Collection<Constraint> constraints, OntologySerializer serializer) {
        this.value = value;
        this.serializer = serializer;
        this.slotManager = InferenceMain.getInstance().getSlotManager();
        this.clauses = convertToCNF(constraints);
        // writeCNF();
    }

    private void writeCNF() {
        StringBuilder sb = new StringBuilder();
        final String currentPath = new File("").getAbsolutePath();
        File file = new File(currentPath);
        File newdir = new File("CNFfiles");
        newdir.mkdir();
        String base = file.toString();
        String path = base + "/CNFfiles";
        String writePath = path + "/CNFResultFor-" + value + ".txt";
        sb.append("CNF for type " + value + ":" + "\n");

        for (VecInt clause : clauses) {
            sb.append("(");
            sb.append(clause.toString().replace(",", " \u22C1  "));
            sb.append(") \u22C0\n");
        }

        try {
            File f = new File(writePath);
            PrintWriter pw = new PrintWriter(f);
            pw.write(sb.toString());
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<VecInt> convertToCNF(Collection<Constraint> constraints) {
        return serializer.convertAll(constraints);
    }

    public SequenceSolution solve() {
        Map<Integer, Boolean> idToExistence = new HashMap<>();
        Map<Integer, Boolean> result = new HashMap<>();

        final int totalVars = slotManager.nextId();
        final int totalClauses = clauses.size();

        try {

            final WeightedMaxSatDecorator solver = new WeightedMaxSatDecorator(org.sat4j.pb.SolverFactory.newBoth());

            solver.newVar(totalVars);
            solver.setExpectedNumberOfClauses(totalClauses);
            //Arbitrary timeout
            solver.setTimeoutMs(1000000);
            for (VecInt clause : clauses) {
                solver.addSoftClause(clause);
            }

            boolean hasSolution = solver.isSatisfiable();

            if (hasSolution) {

                final Map<Integer, Integer> existentialToPotentialIds = serializer.getExistentialToPotentialVar();
                int[] solution = solver.model();
                for (Integer var : solution) {
                    boolean varIsTrue = var > 0;
                    var = Math.abs(var);
                    Integer potential = existentialToPotentialIds.get(var);
                    if (potential != null) {
                        idToExistence.put(potential, varIsTrue);
                    } else {
                        result.put(var, !varIsTrue);
                    }
                }
                return new SequenceSolution(result, value);
            }

        } catch (Throwable th) {
            VecInt lastClause = clauses.get(clauses.size() - 1);
            throw new RuntimeException("Error MAX-SAT solving! " + lastClause, th);
        }

        return SequenceSolution.noSolution(value);
    }
}
