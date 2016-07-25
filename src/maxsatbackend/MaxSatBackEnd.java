package maxsatbackend;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.WeightedMaxSatDecorator;

import util.MathUtils;
import util.VectorUtils;
import checkers.inference.InferenceMain;
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

    protected final SlotManager slotManager;
    protected final List<VecInt> hardClauses = new LinkedList<VecInt>();
    protected final List<VecInt> softClauses = new LinkedList<VecInt>();

    public MaxSatBackEnd(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, Serializer<VecInt[], VecInt[]> realSerializer,
            Lattice lattice) {
        super(configuration, slots, constraints, qualHierarchy, processingEnvironment, realSerializer,
                lattice);
        this.slotManager = InferenceMain.getInstance().getSlotManager();
    }

    /**
     * Convert constraints to list of VecInt.
     */
    @Override
    public void convertAll() {
        for (Constraint constraint : constraints) {
            collectVarSlots(constraint);
            for (VecInt res : constraint.serialize(realSerializer)) {
                if (res != null && res.size() != 0) {
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
    protected void generateWellForm(List<VecInt> clauses) {
        for (Integer id : this.varSlotIds) {
            int[] leastOneIsTrue = new int[lattice.numTypes];
            for (Integer i : lattice.intToType.keySet()) {
                leastOneIsTrue[i] = MathUtils.mapIdToMatrixEntry(id, i.intValue(), lattice);
            }
            clauses.add(VectorUtils.asVec(leastOneIsTrue));
            List<Integer> varList = new ArrayList<Integer>(lattice.intToType.keySet());
            for (int i = 0; i < varList.size(); i++) {
                for (int j = i + 1; j < varList.size(); j++) {
                    VecInt vecInt = new VecInt(2);
                    vecInt.push(-MathUtils.mapIdToMatrixEntry(id, varList.get(i), lattice));
                    vecInt.push(-MathUtils.mapIdToMatrixEntry(id, varList.get(j), lattice));
                    clauses.add(vecInt);
                }
            }
        }
    }

    protected Map<Integer, AnnotationMirror> decode(int[] solution) {
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        for (Integer var : solution) {
            if (var > 0) {
                var = var - 1;
                int slotId = MathUtils.getSlotId(var, lattice);
                AnnotationMirror type = lattice.intToType.get(MathUtils.getIntRep(var, lattice));
                result.put(slotId, type);
            }
        }
        return result;
    }

    @Override
    public Map<Integer, AnnotationMirror> solve() {
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
            // saving memory of JVM...
            this.hardClauses.clear();

            for (VecInt softclause : softClauses) {
                solver.addSoftClause(softclause);
            }
            // saving memory of JVM...
            this.softClauses.clear();
            if (solver.isSatisfiable()) {
                result = decode(solver.model());
                // PrintUtils.printResult(result);
            } else {
                System.out.println("Not solvable!");
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        // saving memory of JVM...
        this.constraints = null;
        return result;
    }

    /**
     * sat solver configuration Configure
     *
     * @param solver
     */
    private void configureSatSolver(WeightedMaxSatDecorator solver) {
        final int totalVars = (slotManager.nextId() * lattice.numTypes);
        final int totalClauses = hardClauses.size() + softClauses.size();

        solver.newVar(totalVars);
        solver.setExpectedNumberOfClauses(totalClauses);
        solver.setTimeoutMs(1000000);
    }

    /**
     * print all soft and hard clauses for testing.
     */
    protected void printClauses() {
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
