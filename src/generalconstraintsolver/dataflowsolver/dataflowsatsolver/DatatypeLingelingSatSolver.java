package generalconstraintsolver.dataflowsolver.dataflowsatsolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sat4j.core.VecInt;

import checkers.inference.SlotManager;
import dataflow.util.DataflowUtils;
import generalconstraintsolver.dataflowsolver.DataflowImpliesLogic;
import generalconstraintsolver.satsubsolver.SatSubSolver;

public class DatatypeLingelingSatSolver extends SatSubSolver {
    private DataflowImpliesLogic logic;
    private StringBuilder sb = new StringBuilder();
    private String input, lingeling;
    private Set<Integer> variableSet = new HashSet<Integer>();
    private List<Integer> resultList = new ArrayList<Integer>();

    public DatatypeLingelingSatSolver(DataflowImpliesLogic logic,
            SlotManager slotManager) {
        super(logic.getLogics(), slotManager, logic.getLattice());
        this.logic = logic;
    }

    public int[] dataflowsatSolve() {
        final String currentPath = new File("").getAbsolutePath();
        File file = new File(currentPath);
        String base = file.getParent().toString();
        createLingelingDir(base);

        List<VecInt> clauses = convertImpliesToClauses();
        becomeWellForm(clauses);
        buildInputString(clauses);
        return getSolution();
    }

    private void buildInputString(List<VecInt> clauses) {
        String datatype = DataflowUtils.getDataflowValue(logic.getLattice().top)[0];
        sb.append("c This is the input for ");
        sb.append(datatype + "\n");
        final int totalVars = (slotManager.nextId() * lattice.numModifiers);
        final int totalClauses = clauses.size();
        sb.append("p cnf ");
        sb.append(totalVars + " ");
        sb.append(totalClauses + "\n");
        try {
            for (VecInt clause : clauses) {
                // lastClause = clause;
                int[] clauseArray = clause.toArray();
                for (int i = 0; i < clauseArray.length; i++) {
                    variableSet.add(Math.abs(clauseArray[i]));
                    sb.append(clauseArray[i]);
                    sb.append(" ");
                }
                sb.append("0\n");
            }
            writeCNFinput(datatype);
            solveSAT(datatype);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void solveSAT(String datatype) throws FileNotFoundException {
        String readPath = input + "/" + datatype;
        String command = lingeling + " " + readPath;
        try {
            getOutPut_Error(command);
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void writeCNFinput(String datatype) throws FileNotFoundException {
        String writePath = input + "/" + datatype;
        File f = new File(writePath);
        PrintWriter pw = new PrintWriter(f);
        pw.write(sb.toString());
        pw.close();
        sb.setLength(0);
    }

    private void createLingelingDir(String base) {
        String path = base
                + "/src/generalconstraintsolver/dataflowsolver/lingelingdata";
        input = path + "/input";
        lingeling = new File(base).getParent() + "/lingeling/lingeling";
        createDir(path);
        createDir(input);
    }

    private void createDir(String path) {
        File dirPath = new File(path);
        dirPath.mkdir();
    }

    public int[] getSolution() {
        int[] result = new int[resultList.size()];
        for (int i = 0; i < resultList.size(); i++) {
            result[i] = resultList.get(i);
        }
        return result;
    }

    protected void getOutPut_Error(String command)
            throws IOException, InterruptedException {
        resultList = new ArrayList<Integer>();
        final Process p = Runtime.getRuntime().exec(command);
        Thread getOutPut = new Thread() {
            @Override
            public void run() {
                String s = "";
                BufferedReader stdInput = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));
                try {
                    while ((s = stdInput.readLine()) != null) {
                        if (s.charAt(0) == 'v') {
                            s = s.substring(1);
                            for (String retval : s.split(" ")) {
                                if (!retval.equals("") && !retval.equals(" ")
                                        && !retval.equals("\n")) {
                                    if (variableSet.contains(Math.abs(Integer.parseInt(retval)))) {
                                        resultList.add(Integer.parseInt(retval));
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
                BufferedReader stdError = new BufferedReader(
                        new InputStreamReader(p.getErrorStream()));
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
    }
}
