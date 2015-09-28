package GeneralMaxSatSolver;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.WeightedMaxSatDecorator;

import checkers.inference.InferenceMain;
import checkers.inference.InferenceSolution;
import checkers.inference.InferenceSolver;
import checkers.inference.SlotManager;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;

public class GeneralMaxSatSolver implements InferenceSolver {
    private Collection<Constraint> constraints;
    private GeneralCnfVecIntSerializer serializer;
    private SlotManager slotManager;
    private LatticeGenerator lattice;
    Map<Integer, Collection<Integer>> typeForSlot = new HashMap<Integer, Collection<Integer>>();

    @Override
    public InferenceSolution solve(Map<String, String> configuration,
            Collection<Slot> slots, Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {

        this.constraints = constraints;
        this.slotManager = InferenceMain.getInstance().getSlotManager();
        this.lattice = new LatticeGenerator(qualHierarchy);
        this.serializer = new GeneralCnfVecIntSerializer(slotManager, lattice);
        return solve();
    }

    private boolean isLast(int var) {
        return (Math.abs(var) % lattice.numModifiers == 0);
    }

    private int findSlotId(int var) {
        return (Math.abs(var) / lattice.numModifiers + 1);

    }

    private int findModifierNumber(int var) {
        return Math.abs(var) - (Math.abs(var) / lattice.numModifiers)
                * lattice.numModifiers;
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
    
    private void decodeSolverResult(Map<Integer, AnnotationMirror> result){
        for (Integer slotId : typeForSlot.keySet()){
            Collection<Integer> ModifiersForthisSlot = typeForSlot.get(slotId);
            result.put(slotId, lattice.top);
            for (Integer modifier: ModifiersForthisSlot){
                if (modifier.intValue() >0){
                    result.put(slotId,lattice.IntModifier.get(modifier));
                }
            }
        }
    }

    private InferenceSolution solve() {
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        List<VecInt> clauses = serializer.convertAll(constraints);
        final int totalVars = (slotManager.nextId() * lattice.numModifiers);
        final int totalClauses = clauses.size();
        final WeightedMaxSatDecorator solver = new WeightedMaxSatDecorator(
                org.sat4j.pb.SolverFactory.newBoth());
        solver.newVar(totalVars);
        solver.setExpectedNumberOfClauses(totalClauses);
        solver.setTimeoutMs(1000000);
        VecInt lastClause = null;

        try {
            for (VecInt clause : clauses) {
//                System.out.println(clause);
                lastClause = clause;
                solver.addSoftClause(clause);
            }
            if (solver.isSatisfiable()) {
//                System.out.println("solved!");
                int[] solution = solver.model();
                for (Integer var : solution) {
//                    System.out.println(var);
                    if (isLast(var)) {
                        mapSlot_Set(Math.abs(var) / lattice.numModifiers, lattice.numModifiers, var / Math.abs(var));
                    } else {
                        mapSlot_Set(findSlotId(var), findModifierNumber(var), var / Math.abs(var));
                    }
                }
//                for (Integer i : typeForSlot.keySet()) {
//                    System.out.println("key: " + i + "      " + "value: "
//                            + typeForSlot.get(i));
//                }
                decodeSolverResult(result);
                System.out.println("/***********************result*****************************/");
                for (Integer j: result.keySet()){
                    System.out.println("SlotID: "+j+ "  " + "Annotation: " + result.get(j).toString());
                }
                System.out.flush();
                System.out.println("/**********************************************************/");
//                for (AnnotationMirror j: lattice.modifierInt.keySet()){
//                    System.out.println("final key "+j+ "  " + "final value: " + result.get(j).toString());
//                    System.out.println(j.toString() + " " + lattice.modifierInt.get(j));
//                }
                
            } else {
                System.out.println("Not solvable!");
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

}
