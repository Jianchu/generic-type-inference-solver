package checkers.inference.solver;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

public class DecodingTool {
    int[] satSolution;
    private LatticeGenerator lattice;
    Map<Integer, AnnotationMirror> result = new HashMap<>();
    Map<Integer, Collection<Integer>> typeForSlot = new HashMap<Integer, Collection<Integer>>();
    
    public DecodingTool(int[] satSolution, LatticeGenerator lattice){
        this.satSolution = satSolution;
        this.lattice = lattice;
        decode();
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
    
    private void decode(){
        for (Integer var : satSolution) {
            if (isLast(var)) {
                mapSlot_Set(Math.abs(var) / lattice.numModifiers, lattice.numModifiers, var / Math.abs(var));
            } else {
                mapSlot_Set(findSlotId(var), findModifierNumber(var), var / Math.abs(var));
            }
        }
        decodeSolverResult(result);
    }
}

