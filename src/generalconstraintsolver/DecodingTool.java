package generalconstraintsolver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

public class DecodingTool {
    public int[] satSolution;
    public String path = "";
    public LatticeGenerator lattice;
    public Map<Integer, AnnotationMirror> result = new HashMap<>();
    Map<Integer, Collection<Integer>> typeForSlot = new HashMap<Integer, Collection<Integer>>();

    public DecodingTool(int[] satSolution, LatticeGenerator lattice) {
        this.satSolution = satSolution;
        this.lattice = lattice;
        decodeSatResult();
    }

    public DecodingTool(String path, LatticeGenerator lattice, Set<Integer> slotRepresentSet) {
        this.path = path;
        this.lattice = lattice;
        try {
            setDefaultResult(slotRepresentSet);
            decodeLogicBloxResult();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public DecodingTool(String path, LatticeGenerator lattice) {
        this.path = path;
        this.lattice = lattice;
    }

    private boolean isLast(int var) {
        // JLTODO: Why this strange comparison?? What is the semantics of
        // isLast? Why not Math.abs(var) == lattice.numModifiers?
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

    protected void mapSlot_ModifierRep(Integer var) {
        if (isLast(var)) {
            mapSlot_Set(Math.abs(var) / lattice.numModifiers, lattice.numModifiers, var / Math.abs(var));
        } else {
            mapSlot_Set(findSlotId(var), findModifierNumber(var), var / Math.abs(var));
        }
    }

    protected void decodeSolverResult(Map<Integer, AnnotationMirror> result) {
        for (Integer slotId : typeForSlot.keySet()) {
            Collection<Integer> ModifiersForthisSlot = typeForSlot.get(slotId);
            result.put(slotId, lattice.top);
            for (Integer modifier : ModifiersForthisSlot) {
                if (modifier.intValue() > 0) {
                    result.put(slotId,lattice.IntModifier.get(modifier));
                }
            }
        }
    }

    private void setDefaultResult(Set<Integer> slotRepresentSet) {
        for (Integer var : slotRepresentSet) {
            mapSlot_ModifierRep(var);
        }
        decodeSolverResult(result);
        typeForSlot.clear();
    }

    private void decodeSatResult() {
        for (Integer var : satSolution) {
            mapSlot_ModifierRep(var);
        }
        decodeSolverResult(result);
    }

    public void decodeLogicBloxResult() throws FileNotFoundException {
        String readPath = path + "/logicbloxOutput.txt";
        InputStream in = new FileInputStream(readPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                String[] s = line.split(" ");
                if (s[1].equals("true")) {
                    int var = Integer.parseInt(s[0]);
                    mapSlot_ModifierRep(var);
                }
            }
            decodeSolverResult(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

