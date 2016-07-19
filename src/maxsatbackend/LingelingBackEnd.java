package maxsatbackend;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;

import checkers.inference.model.Constraint;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import constraintsolver.Lattice;

public class LingelingBackEnd extends MaxSatBackEnd {

    private StringBuilder CNFInput = new StringBuilder();
    private final File CNFData = new File(new File("").getAbsolutePath() + "/cnfData");
    private final String lingeling = System.getenv().get("JSR308") + "/lingeling/lingeling";
    // record cnf integers in clauses. lingeling solver give the answer for all
    // the integers from 1 to the largest one. Some of them may be not in the
    // clauses.
    private Set<Integer> variableSet = new HashSet<Integer>();

    public LingelingBackEnd(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, Serializer<VecInt[], VecInt[]> realSerializer,
            Lattice lattice) {
        super(configuration, slots, constraints, qualHierarchy, processingEnvironment, realSerializer,
                lattice);
        CNFData.mkdir();
    }

    private void buildCNF(List<VecInt> clauses) {
        CNFInput.append("c This is the CNF input\n");
        final int totalVars = (slotManager.nextId() * lattice.numTypes);
        final int totalClauses = hardClauses.size() + softClauses.size();
        CNFInput.append("p cnf ");
        CNFInput.append(totalVars + " ");
        CNFInput.append(totalClauses + "\n");
        for (VecInt clause : clauses) {
            try {
                CNFInput.append(clause.toString().replaceAll(",", " ") + " 0\n");
            } catch (java.lang.OutOfMemoryError e) {
                System.out.println("The failed string is: " + clause.toString());
            }
            
        }
    }

    private void writeCNFinput() {
        String writePath = CNFData.getAbsolutePath() + "/cnfdata.txt";
        File f = new File(writePath);
        PrintWriter pw;
        try {
            pw = new PrintWriter(f);
            pw.write(CNFInput.toString());
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private int[] getOutPut_Error(String command) throws IOException, InterruptedException {
        List<Integer> resultList = new ArrayList<Integer>();
        final Process p = Runtime.getRuntime().exec(command);
        Thread getOutPut = new Thread() {
            @Override
            public void run() {
                String s = "";
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                try {
                    while ((s = stdInput.readLine()) != null) {
                        if (s.charAt(0) == 'v') {
                            s = s.substring(1);
                            for (String retval : s.split(" ")) {
                                if (!retval.equals("") && !retval.equals(" ") && !retval.equals("\n")) {
                                    int val = Integer.parseInt(retval);
                                    if (variableSet.contains(Math.abs(val))) {
                                        resultList.add(val);
                                    }
                                }
                            }
                        }
                    }
                } catch (NumberFormatException | IOException e) {
                    e.printStackTrace();
                }
            }
        };
        getOutPut.start();
        Thread getError = new Thread() {
            @Override
            public void run() {
                String s = "";
                String errReply = "";
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                try {
                    while ((s = stdError.readLine()) != null) {
                        errReply = errReply + s;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        getError.start();
        getOutPut.join();
        getError.join();
        p.waitFor();
        // cannot convert from Integer[] to int[] directly...
        int[] resultArray = new int[resultList.size()];
        for (int i = 0; i < resultList.size(); i++) {
            resultArray[i] = resultList.get(i);
        }
        return resultArray;
    }

    private void collectVals() {
        for (VecInt clause : this.hardClauses) {
            int[] clauseArray = clause.toArray();
            for (int i = 0; i < clauseArray.length; i++) {
                variableSet.add(Math.abs(clauseArray[i]));
            }
        }
    }

    @Override
    public Map<Integer, AnnotationMirror> solve() {
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        this.convertAll();
        generateWellForm(hardClauses);
        buildCNF(this.hardClauses);
        collectVals();
        writeCNFinput();
        try {
            int[] resultArray = getOutPut_Error(lingeling + " " + CNFData.getAbsolutePath()
                    + "/cnfdata.txt");
            result = decode(resultArray);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

}
