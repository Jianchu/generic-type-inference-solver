package maxsatbackend;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.WeightedMaxSatDecorator;

import util.MathUtils;
import util.VectorUtils;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceSolution;
import checkers.inference.SlotManager;
import checkers.inference.model.Constraint;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import constraintsolver.BackEnd;
import constraintsolver.Lattice;

/**
 * @author jianchu
 *         MaxSat back end converts constraints to VecInt, and solves then by
 *         sat4j.
 */

public class MaxSatBackEnd extends BackEnd {
    
    private SlotManager slotManager;

    public MaxSatBackEnd(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, Serializer realSerializer) {
        super(configuration, slots, constraints, qualHierarchy, processingEnvironment, realSerializer);
        this.slotManager = InferenceMain.getInstance().getSlotManager();
        Lattice.configure(qualHierarchy);

    }


    Map<Integer, Collection<Integer>> typeForSlot = new HashMap<Integer, Collection<Integer>>();

    private boolean isLast(int var) {
        return (Math.abs(var) % Lattice.numModifiers == 0);
    }

    private int findSlotId(int var) {
        return (Math.abs(var) / Lattice.numModifiers + 1);

    }

    private int findModifierNumber(int var) {
        return Math.abs(var) - (Math.abs(var) / Lattice.numModifiers) * Lattice.numModifiers;
    }

    private void mapSlot_Set(Integer slotId, Integer number, int isPositive) {
        Integer booleanInt = new Integer(number.intValue() * isPositive);
        if (!typeForSlot.keySet().contains(slotId)) {
            Set<Integer> possiableModifier = new HashSet<Integer>();
            possiableModifier.add(booleanInt);
            typeForSlot.put(slotId, possiableModifier);
        } else {
            Collection<Integer> possiableModifier = typeForSlot.get(slotId);
            possiableModifier.add(booleanInt);
            typeForSlot.put(slotId, possiableModifier);
        }
    }

    private void decodeSolverResult(Map<Integer, AnnotationMirror> result) {
        for (Integer slotId : typeForSlot.keySet()) {
            Collection<Integer> ModifiersForthisSlot = typeForSlot.get(slotId);
            result.put(slotId, Lattice.top);
            for (Integer modifier : ModifiersForthisSlot) {
                if (modifier.intValue() > 0) {
                    result.put(slotId, Lattice.IntModifier.get(modifier));
                }
            }
        }
    }

    /**
     * Convert constraints to list of VecInt.
     */
    @Override
    public List<VecInt> convertAll() {
        List<VecInt> serializedConstraints = new LinkedList<VecInt>();
        for (Constraint constraint : constraints) {
            collectVarSlots(constraint);
            for (VecInt res : (VecInt[]) constraint.serialize(realSerializer)) {
                if (res.size() != 0) {
                    serializedConstraints.add(res);
                }
            }
        }
        return serializedConstraints;
    }

    /**
     * generate well form clauses such that there is one and only one beta value
     * can be true.
     * 
     * @param clauses
     */
    private void generateWellForm(List<VecInt> clauses) {
        for (Integer id : this.varSlotIds) {
            int[] leastOneIsTrue = new int[Lattice.numModifiers];
            for (Integer i : Lattice.IntModifier.keySet()) {
                leastOneIsTrue[i] = MathUtils.mapIdToMatrixEntry(id, i.intValue());
            }
            clauses.add(VectorUtils.asVec(leastOneIsTrue));

            Iterator<Integer> entries1 = Lattice.IntModifier.keySet().iterator();
            Set<Integer> entries2 = Lattice.IntModifier.keySet();
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

    @Override
    public InferenceSolution solve() {
        List<VecInt> clauses = this.convertAll();
        generateWellForm(clauses);

        Map<Integer, AnnotationMirror> result = new HashMap<>();
        final int totalVars = (slotManager.nextId() * Lattice.numModifiers);
        final int totalClauses = clauses.size();
        final WeightedMaxSatDecorator solver = new WeightedMaxSatDecorator(org.sat4j.pb.SolverFactory.newBoth());
        solver.newVar(totalVars);
        solver.setExpectedNumberOfClauses(totalClauses);
        solver.setTimeoutMs(1000000);
        try {
            for (VecInt clause : clauses) {
                // System.out.println(clause);
                solver.addHardClause(clause);
            }
            if (solver.isSatisfiable()) {
                int[] solution = solver.model();
                for (Integer var : solution) {
                    if (isLast(var)) {
                        mapSlot_Set(Math.abs(var) / Lattice.numModifiers, Lattice.numModifiers, var
                                / Math.abs(var));
                    } else {
                        mapSlot_Set(findSlotId(var), findModifierNumber(var), var / Math.abs(var));
                    }
                }
                decodeSolverResult(result);
                System.out.println("/***********************result*****************************/");
                for (Integer j : result.keySet()) {
                    System.out.println("SlotID: " + j + "  " + "Annotation: " + result.get(j).toString());
                }
                System.out.flush();
                System.out.println("/**********************************************************/");
            } else {
                System.out.println("Not solvable!");
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
