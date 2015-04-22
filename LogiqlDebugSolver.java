package checkers.inference.solver.LogiqlDebugSolver;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

import org.checkerframework.framework.type.QualifierHierarchy;

import checkers.inference.InferenceSolver;
import checkers.inference.model.CombVariableSlot;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.RefinementVariableSlot;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import ostrusted.quals.OsTrusted;
import ostrusted.quals.OsUntrusted;

import org.checkerframework.javacutil.AnnotationUtils;

/**
 * Debug solver prints out variables and constraints.
 *
 * @author mcarthur
 *
 */

public class LogiqlDebugSolver implements InferenceSolver {
    private final String OSTRUSTED = "-1";
    private final String OSUNTRUSTED = "-2";
    private final String currentPath = new File("").getAbsolutePath();

    Map<Integer, AnnotationMirror> result = new HashMap<Integer, AnnotationMirror>();

    @Override
    public Map<Integer, AnnotationMirror> solve(
            Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {
        String Base;
        File file = null;
        file = new File(currentPath);
        Base = file.getParent().toString();
        String Path = Base + "/src/checkers/inference/solver/LogiqlDebugSolver";
        String addVariable = "";
        String output = "";
        String s = "";
        for (int i = 0; i < slots.size(); i++) {
            addVariable = addVariable + "+Variable(" + i + ").\n";
        }
        for (Constraint constraint : constraints) {
            if (constraint.getClass().getSimpleName()
                    .contains("ComparableConstraint"))
                continue;
            List<Slot> slots1 = constraint.getSlots();
            String[] vStr = new String[2];
            vStr = SlotsToStr(slots1);
            for (int i = 0; i < 2; i++) {
                if (vStr[i].contains("OsTrusted"))
                    vStr[i] = OSTRUSTED;
                else if (vStr[i].contains("OsUntrusted")) {
                    vStr[i] = OSUNTRUSTED;
                }
            }
            output = output + "+" + constraint.getClass().getSimpleName() + "["
                    + vStr[0] + "," + vStr[1] + "]=true.\n";
        }
        try {
            String writePath = Path + "/data.logic";
            File f = new File(writePath);
            PrintWriter pw = new PrintWriter(f);
            pw.write(addVariable + output);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] command = new String[7];
        // command[0]=
        // "source /home/jianchu/Desktop/logicblox-x86_64-linux-4.1.8-91f645cf2a0e/etc/profile.d/logicblox.sh";
        command[1] = "lb create test";
        command[2] = "lb addblock test -f" + Path + "/constraint.logic";
        command[3] = "lb exec test -f" + Path + "/basicData.logic";
        command[4] = "lb exec test -f" + Path + "/data.logic";
        command[5] = "lb print test AnnotationOf";
        command[6] = "lb delete test";
        try {

            for (int i = 1; i < 5; i++) {
                Process p = Runtime.getRuntime().exec(command[i]);
                try {
                    p.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            final Elements elements = processingEnvironment.getElementUtils();
            AnnotationMirror OSuntrusted = AnnotationUtils.fromClass(elements,
                    OsUntrusted.class);
            AnnotationMirror OStrusted = AnnotationUtils.fromClass(elements,
                    OsTrusted.class);
            for (int i = 0; i < slots.size(); i++) {
                result.put(i, OSuntrusted);
            }

            Process p5 = Runtime.getRuntime().exec(command[5]);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                    p5.getInputStream()));
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
                String[] Line = s.split(" ");
                if (!Line[1].contains("-1") && !Line[1].contains("-2")) {
                    int VariableId = Integer.parseInt(Line[1]);
                    if (s.contains("Ostrusted")) {
                        result.put(VariableId, OStrusted);
                    } else if (s.contains("Osuntrusted")) {
                        result.put(VariableId, OSuntrusted);
                    }
                }
            }
            Process p6 = Runtime.getRuntime().exec(command[6]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

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
                vStr[i] = slot.getValue().toString();
            }
        }
        return vStr;
    }

}