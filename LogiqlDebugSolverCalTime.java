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
 * @author Jianchu Li
 *
 */

public class LogiqlDebugSolverCalTime implements InferenceSolver {
    private final String OSTRUSTED = "ostrusted";
    private final String OSUNTRUSTED = "osuntrusted";
    private long t_start = 0;
    private long t_variable = 0;
    private long t_constraint = 0;
    private long t_generate_logic = 0;
    private long t_decode_e = 0;
    private long t_end = 0;
    private int trusted = 0;
    private String inReply = "";
    private int untrusted = 0;
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
        //String addVariable = "";
        String deleteVariable = "";
        String output = "";
        String deleteoutput = "";
        String s = "";
        t_start = System.currentTimeMillis();
        for (int i = 0; i < slots.size(); i++) {
//            addVariable = addVariable + "+Variable(" + i + ").\n";
            deleteVariable = deleteVariable + "-Variable(" + i + ").\n";
        }
        t_variable = System.currentTimeMillis();
        for (Constraint constraint : constraints) {
            if (constraint.getClass().getSimpleName()
                    .contains("ComparableConstraint"))
                continue;
                List<Slot> slots1 = constraint.getSlots();
            if(slots1.get(0)!= slots1.get(1)){
            String[] vStr = new String[2];
            vStr = SlotsToStr(slots1);
            for (int i = 0; i < 2; i++) {
                if (vStr[i].contains("OsTrusted"))
                    vStr[i] = OSTRUSTED;
                else if (vStr[i].contains("OsUntrusted")) {
                    vStr[i] = OSUNTRUSTED;
                }
            }
            
            if(vStr[0]!=vStr[1]){
            output = output + "+Variable(_" + vStr[0] + "),+hasVariableName[_"+vStr[0] + "]="+ vStr[0] +","  + "+Variable(_" + vStr[1] + "),+hasVariableName[_"+vStr[1] + "]="+ vStr[1]+",+"+constraint.getClass().getSimpleName() + "["
                    + "_"+vStr[0] + "," + "_"+vStr[1] + "]=true.\n";
            output = output.replaceAll("=ostrusted", "=-1");
            output = output.replaceAll("=osuntrusted", "=-2");
/*            
            deleteoutput = deleteoutput + "-Variable(_" + vStr[0] + "),-hasVariableName[_"+vStr[0] + "]="+ vStr[0] +","  + "-Variable(_" + vStr[1] + "),-hasVariableName[_"+vStr[1] + "]="+ vStr[1]+",-"+constraint.getClass().getSimpleName() + "["
                    + "_"+vStr[0] + "," + "_"+vStr[1] + "]=true.\n";
            deleteoutput = deleteoutput.replaceAll("=ostrusted", "=-1");
            deleteoutput = deleteoutput.replaceAll("=osuntrusted", "=-2");
*/            
//            deleteoutput = deleteoutput + "-"
//                    + constraint.getClass().getSimpleName() + "[" + vStr[0]
//                    + "," + vStr[1] + "]=true.\n";
            }
            }
        }
        t_constraint = System.currentTimeMillis();

        try {
            String writePath = Path + "/data.logic";
            File f = new File(writePath);
            PrintWriter pw = new PrintWriter(f);
           // pw.write(addVariable + output);
            pw.write(output);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String writePath = Path + "/deletedata.logic";
            File f = new File(writePath);
            PrintWriter pw = new PrintWriter(f);
            pw.write(deleteVariable);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        t_generate_logic = System.currentTimeMillis();
        String[] command = new String[7];
        // command[0]=
        // "source /home/jianchu/Desktop/logicblox-x86_64-linux-4.1.8-91f645cf2a0e/etc/profile.d/logicblox.sh";
        command[1] = "lb create pltest";
        command[2] = "lb addblock pltest -f" + Path + "/constraint.logic";
        command[3] = "lb exec pltest -f" + Path + "/basicData.logic";
        command[4] = "lb exec pltest -f" + Path + "/data.logic";
        command[5] = "lb print pltest AnnotationOf";
        command[6] = "lb exec pltest -f" + Path + "/deletedata.logic";

        try {
            for (int i = 4; i < 5; i++) {
                getOutPut_Error(command[i]);
            }
            t_end = System.currentTimeMillis();
            getOutPut_Error(command[5]);
            parseOutPut(processingEnvironment, slots.size());
            t_decode_e = System.currentTimeMillis();
            //Process p6 = Runtime.getRuntime().exec(command[6]);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String numberOfSlots = "The number of slots: " + slots.size() + "\n";
        String numberOfConstraints = "The number of constraints: "
                + constraints.size() + "\n";
        String timing = "Total time in LogiqlDebugSolver: "
                + (t_decode_e - t_start) + "\nTime for generating variables: "
                + (t_variable - t_start)
                + "\nTime for generating constraints: "
                + (t_constraint - t_variable)
                + "\nTime for generating logic file: "
                + (t_generate_logic - t_constraint)
                + "\nTime for Logiql solver computing: "
                + (t_end - t_generate_logic)
                + "\nTime for decoding the result of Logicblox: "
                + (t_decode_e - t_end) + "\n";
        String numberOfType = "Number Of Ostrusted: " + trusted + "\n"
                + "Number Of OsUntrusted: " + untrusted + "\n";
        System.out.println(numberOfSlots + numberOfConstraints + numberOfType
                + timing);
        return result;

    }

    private void getOutPut_Error(String command) throws IOException,
            InterruptedException {
        final Process p = Runtime.getRuntime().exec(command);
        Thread getOutPut = new Thread() {
            public void run() {
                String s = "";
                BufferedReader stdInput = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));
                try {
                    while ((s = stdInput.readLine()) != null) {
                        inReply = inReply + s + "\n";
                    }
                    // System.out.println("Output from lb print"+inReply);
                } catch (NumberFormatException | IOException e) {
                    e.printStackTrace();
                }
            }
        };
        getOutPut.start();

        Thread getError = new Thread() {
            public void run() {
                String s = "";
                String errReply = "";
                BufferedReader stdError = new BufferedReader(
                        new InputStreamReader(p.getErrorStream()));
                try {
                    while ((s = stdError.readLine()) != null) {
                        errReply = errReply + s;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Error from lb command:" + errReply);
            }
        };
        getError.start();
        getOutPut.join();
        getError.join();
        p.waitFor();
    }

    private void parseOutPut(ProcessingEnvironment processingEnvironment,
            int size) {
        Elements elements = processingEnvironment.getElementUtils();
        final AnnotationMirror OSuntrusted = AnnotationUtils.fromClass(
                elements, OsUntrusted.class);
        final AnnotationMirror OStrusted = AnnotationUtils.fromClass(elements,
                OsTrusted.class);
        String s = "";
        for (int i = 0; i < size; i++) {
            result.put(i, OSuntrusted);
        }
        BufferedReader stdInput = new BufferedReader(new StringReader(inReply));
        try {
            while ((s = stdInput.readLine()) != null) {
                // System.out.println(s);
                if (s.contains("[")) {
                    String[] Line = s.split(" ");
                    if (!Line[1].contains("-1") && !Line[1].contains("-2")) {
                        int VariableId = Integer.parseInt(Line[1]);
                        if (s.contains("Ostrusted")) {
                            trusted++;
                            result.put(VariableId, OStrusted);
                        } else if (s.contains("Osuntrusted")) {
                            untrusted++;
                            result.put(VariableId, OSuntrusted);
                        }
                    }
                }
            }
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }
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
