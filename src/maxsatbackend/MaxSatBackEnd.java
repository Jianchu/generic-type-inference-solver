package maxsatbackend;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.WeightedMaxSatDecorator;

import util.PrintUtils;
import util.MathUtils;
import util.VectorUtils;
import checkers.inference.DefaultInferenceSolution;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceSolution;
import checkers.inference.SlotManager;
import checkers.inference.model.Constraint;
import checkers.inference.model.PreferenceConstraint;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import constraintsolver.BackEnd;
import constraintsolver.Lattice;

/**
 * @author jianchu MaxSat back end converts constraints to VecInt, and solves
 *         them by sat4j.
 */
public class MaxSatBackEnd extends BackEnd<VecInt[], VecInt[]> {

    private final SlotManager slotManager;
    private final List<VecInt> hardClauses = new LinkedList<VecInt>();
    private final List<VecInt> softClauses = new LinkedList<VecInt>();

    public MaxSatBackEnd(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, Serializer<VecInt[], VecInt[]> realSerializer) {
        super(configuration, slots, constraints, qualHierarchy, processingEnvironment, realSerializer);
        this.slotManager = InferenceMain.getInstance().getSlotManager();
        Lattice.configure(qualHierarchy);
    }

    /**
     * Convert constraints to list of VecInt.
     */
    @Override
    public void convertAll() {
        for (Constraint constraint : constraints) {
            collectVarSlots(constraint);
            for (VecInt res : constraint.serialize(realSerializer)) {
                if (res.size() != 0) {
                    if (constraint instanceof PreferenceConstraint) {
                        softClauses.add(res);
                    } else {
                        hardClauses.add(res);
                    }
                }
            }
        }
    }

    /**
     * generate well form clauses such that there is one and only one beta value
     * can be true.
     *
     * @param clauses
     */
    private void generateWellForm(List<VecInt> clauses) {
        for (Integer id : this.varSlotIds) {
            int[] leastOneIsTrue = new int[Lattice.numTypes];
            for (Integer i : Lattice.intToType.keySet()) {
                leastOneIsTrue[i] = MathUtils.mapIdToMatrixEntry(id, i.intValue());
            }
            clauses.add(VectorUtils.asVec(leastOneIsTrue));

            Iterator<Integer> entries1 = Lattice.intToType.keySet().iterator();
            Set<Integer> entries2 = Lattice.intToType.keySet();
            while (entries1.hasNext()) {
                Integer entry1 = entries1.next();
                for (Integer entry2 : entries2) {
                    int[] onlyOneIsTrue = new int[2];
                    if (entry2.intValue() != entry1.intValue()) {
                        onlyOneIsTrue[0] = -MathUtils.mapIdToMatrixEntry(id, entry1.intValue());
                        onlyOneIsTrue[1] = -MathUtils.mapIdToMatrixEntry(id, entry2.intValue());
                        clauses.add(VectorUtils.asVec(onlyOneIsTrue));
                    }
                }
            }
        }
    }

    private Map<Integer, AnnotationMirror> decode(int[] solution) {
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        for (Integer var : solution) {
            if (var > 0) {
                var = var - 1;
                int slotId = MathUtils.getSlotId(var);
                AnnotationMirror type = Lattice.intToType.get(MathUtils.getIntRep(var));
                result.put(slotId, type);
            }
        }
        return result;
    }

    @Override
    public InferenceSolution solve() {
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        final WeightedMaxSatDecorator solver = new WeightedMaxSatDecorator(org.sat4j.pb.SolverFactory.newBoth());
        this.convertAll();
        generateWellForm(hardClauses);
        // printClauses();
        configureSatSolver(solver);

        try {
            for (VecInt hardClause : hardClauses) {
                solver.addHardClause(hardClause);
            }

            for (VecInt softclause : softClauses) {
                solver.addSoftClause(softclause);
            }

            if (solver.isSatisfiable()) {
                result = decode(solver.model());
                PrintUtils.printResult(result);
            } else {
                System.out.println("Not solvable!");
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

        return new DefaultInferenceSolution(result);
    }

    /**
     * sat solver configuration Configure
     *
     * @param solver
     */
    private void configureSatSolver(WeightedMaxSatDecorator solver) {
        final int totalVars = (slotManager.nextId() * Lattice.numTypes);
        final int totalClauses = hardClauses.size() + softClauses.size();

        solver.newVar(totalVars);
        solver.setExpectedNumberOfClauses(totalClauses);
        solver.setTimeoutMs(1000000);
    }

    /**
     * print all soft and hard clauses for testing.
     */
    private void printClauses() {
        System.out.println("Hard clauses: ");
        for (VecInt hardClause : hardClauses) {
            System.out.println(hardClause);
        }
        System.out.println();
        System.out.println("Soft clauses: ");
        for (VecInt softClause : softClauses) {
            System.out.println(softClause);
        }
    }
}
