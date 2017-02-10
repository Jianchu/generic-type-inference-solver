package maxsatbackend;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;

import util.StatisticPrinter;
import util.StatisticPrinter.StatisticKey;
import checkers.inference.model.Constraint;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import constraintsolver.Lattice;

public class LingelingBackEnd extends MaxSatBackEnd {

    private final String lingeling = System.getenv().get("JSR308") + "/lingeling/lingeling";
    // record cnf integers in clauses. lingeling solver give the answer for all
    // the integers from 1 to the largest one. Some of them may be not in the
    // clauses.
    private Set<Integer> variableSet = new HashSet<Integer>();
    public static AtomicInteger nth = new AtomicInteger(0);
    private long serializationStart;
    private long serializationEnd;

    public LingelingBackEnd(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, Serializer<VecInt[], VecInt[]> realSerializer,
            Lattice lattice) {
        super(configuration, slots, constraints, qualHierarchy, processingEnvironment, realSerializer,
                lattice);
    }

    private int[] getOutPut_Error(String command) throws IOException, InterruptedException {
        final List<Integer> resultList = new ArrayList<Integer>();
        final Process p = Runtime.getRuntime().exec(command);
        Thread getOutPut = new Thread() {
            @Override
            public void run() {
                String s = "";
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                try {
                    while ((s = stdInput.readLine()) != null) {
                        if (s.charAt(0) == 'v') {
                            for (String retval : s.split(" ")) {
                                if (!retval.equals("") && !retval.equals(" ") && !retval.equals("\n")
                                        && !retval.equals("v")) {
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
                StringBuilder sb = new StringBuilder();
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                try {
                    while ((s = stdError.readLine()) != null) {
                        sb.append(s);
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
    protected boolean shouldOutputCNF() {
        // We need the CNF output to pass to Linegling
        // and so we unconditionally signal we want CNF output.
        return true;
    }
    
    private void recordData() {
        int totalClauses = hardClauses.size() + softClauses.size();
        int totalVariable = variableSet.size();
        StatisticPrinter.record(StatisticKey.CNF_CLAUSES_SIZE, (long) totalClauses);
        StatisticPrinter.record(StatisticKey.CNF_VARIABLE_SIZE, (long) totalVariable);
    }

    @Override
    public Map<Integer, AnnotationMirror> solve() {
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        this.serializationStart = System.currentTimeMillis();
        this.convertAll();
        // this.hardClauses.addAll(softClauses);
        this.serializationEnd = System.currentTimeMillis();
        generateWellForm(hardClauses);
        buildCNF();
        collectVals();
        recordData();
        // saving memory of JVM...
        this.hardClauses.clear();
        int localNth = nth.incrementAndGet();
        writeCNFInput("cnfdata" + localNth + ".txt");
        this.solvingStart = System.currentTimeMillis();
        try {
            int[] resultArray = getOutPut_Error(lingeling + " " + CNFData.getAbsolutePath() + "/cnfdata"
                    + localNth + ".txt");
            result = decode(resultArray);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        this.solvingEnd = System.currentTimeMillis();
        boolean graph = (configuration.get("useGraph") == null || configuration.get("useGraph").equals(
                "true")) ? true : false;
        boolean parallel = (configuration.get("solveInParallel") == null || configuration.get(
                "solveInParallel").equals("true")) ? true : false;
        long solvingTime = solvingEnd - solvingStart;
        if (graph) {
            if (parallel) {
                // StatisticPrinter.recordSingleThread(Pair.<Long, Long> of(
                // (serializationEnd - serializationStart), solvingTime));
                StatisticPrinter.recordSerializationSingleThread((serializationEnd - serializationStart));
                StatisticPrinter.recordSolvingSingleThread(solvingTime);
            } else {
                StatisticPrinter.record(StatisticKey.SAT_SOLVING_GRAPH_SEQUENTIAL_TIME_LL, solvingTime);
                StatisticPrinter.record(StatisticKey.SAT_SERIALIZATION_TIME,
                        (serializationEnd - serializationStart));
            }
        } else {
            StatisticPrinter.record(StatisticKey.SAT_SERIALIZATION_TIME,(serializationEnd - serializationStart));
            StatisticPrinter.record(StatisticKey.SAT_SOLVING_WITHOUT_GRAPH_TIME_LL, solvingTime);
        }
        // saving memory of JVM...
        this.constraints = null;
        this.variableSet = null;
        return result;
    }

}
