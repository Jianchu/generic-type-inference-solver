package checkers.inference.solver.LogiqlDebugSolver;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import checkers.inference.model.CombVariableSlot;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.RefinementVariableSlot;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;

/**
 * 
 *
 * @author Jianchu Li
 *
 */

public class LogiqlDataGenerator {
    Collection<Slot> slots;
    Collection<Constraint> constraints;
    private final String currentPath = new File("").getAbsolutePath();
    boolean flag = false;
    int constantSlot = -1;

    public LogiqlDataGenerator(Collection<Slot> slots,
            Collection<Constraint> constraints) {
        this.slots = slots;
        this.constraints = constraints;
    }

    public void GenerateLogiqlData() {
        String output = "";
        for (Constraint constraint : constraints) {
            List<Slot> slot = constraint.getSlots();
            if (slot.get(0) != slot.get(1)) {
                String[] vStr = new String[2];
                vStr = SlotsToStr(slot);
                String nameOfConstraint = getConstraintName(constraint
                        .getClass().getSimpleName());
                if (vStr[0] != vStr[1]) {
                    output = output + writeOutputString(nameOfConstraint, vStr, slot);
                }
            }
            flag = false;
            constantSlot = -1;
        }
        writeFile(output);
    }


    private String writeOutputString(String nameOfConstraint, String[] vStr,
            List<Slot> slot) {
        String output = "";
        if (flag == true && constantSlot == 0) {
            output = "+modifier(" + vStr[0] + "),+hasmodifierName[" + vStr[0]
                    + "]=" + "\"" + vStr[0] + "\"," + "+variable(_" + vStr[1]
                    + "),+hasvariableName[_" + vStr[1] + "]=" + vStr[1] + ","
                    + "+" + nameOfConstraint + "(" + vStr[0] + "," + "_"
                    + vStr[1] + ").\n";
        } else if (flag == true && constantSlot == 1) {
            output = "+variable(_" + vStr[0] + "),+hasvariableName[_" + vStr[0]
                    + "]=" + vStr[0] + "," + "+modifier(" + vStr[1]
                    + "),+hasmodifierName[" + vStr[1] + "]=" + "\"" + vStr[1]
                    + "\"," + "+" + nameOfConstraint + "(" + "_" + vStr[0]
                    + "," + vStr[1] + ").\n";
        } else {
            output = output + "+variable(_" + vStr[0] + "),+hasvariableName[_"
                    + vStr[0] + "]=" + vStr[0] + "," + "+variable(_" + vStr[1] + "),+hasvariableName[_"
                    + vStr[1] + "]=" + vStr[1] + "," + "+" + nameOfConstraint + "(" + "_" + vStr[0] + "," + "_"
                    + vStr[1] + ").\n";
        }
        return output;
    }

    /*
     * Transform two constraint slots to their Id value
     */
    private String[] SlotsToStr(List<Slot> slots) {
        String[] vStr = new String[2];
        for (int i = 0; i < 2; i++) {
            if (slots.get(i).getClass().equals(VariableSlot.class)) {
                VariableSlot slot = (VariableSlot) slots.get(i);
                vStr[i] = Integer.toString(slot.getId());
            } else if (slots.get(i).getClass()
                    .equals(RefinementVariableSlot.class)) {
                RefinementVariableSlot slot = (RefinementVariableSlot) slots
                        .get(i);
                vStr[i] = Integer.toString(slot.getId());
            } else if (slots.get(i).getClass().equals(CombVariableSlot.class)) {
                CombVariableSlot slot = (CombVariableSlot) slots.get(i);
                vStr[i] = Integer.toString(slot.getId());
            } else if (slots.get(i).getClass().equals(ConstantSlot.class)) {
                ConstantSlot slot = (ConstantSlot) slots.get(i);
                vStr[i] = slot.getValue().toString().replaceAll("[.@]", "_");
                constantSlot = i;
                flag = true;
            }
        }
        return vStr;
    }

    private String getConstraintName(String simpleName) {
        String nameOfConstraint = "";
        if (simpleName.contains("EqualityConstraint") && flag == false) {
            nameOfConstraint = "equalityConstraint";
        } else if (simpleName.contains("InequalityConstraint") && flag == false) {
            nameOfConstraint = "inequalityConstraint";
        } else if (simpleName.contains("EqualityConstraint") && flag == true) {
            nameOfConstraint = "equalityConstraintContainsModifier";
        } else if (simpleName.contains("InequalityConstraint") && flag == true) {
            nameOfConstraint = "inequalityConstraintContainsModifier";
        } else if (simpleName.contains("SubtypeConstraint")) {
            nameOfConstraint = "subtypeConstraint";
        } else if (simpleName.contains("ComparableConstraint")) {
            nameOfConstraint = "comparableConstraint";
        } else if (simpleName.contains("CombineConstraint")) {
            nameOfConstraint = "adaptationConstraint";
        }
        return nameOfConstraint;
    }
    
    private void writeFile(String output){
        File file = new File(currentPath);
        String Base = file.getParent().toString();
        String Path = Base + "/src/checkers/inference/solver/LogiqlDebugSolver";
        try {
            String writePath = Path + "/data.logic";
            File f = new File(writePath);
            PrintWriter pw = new PrintWriter(f);
            pw.write(output);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
