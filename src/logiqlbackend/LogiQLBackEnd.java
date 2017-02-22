package logiqlbackend;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import util.NameUtils;
import util.StatisticPrinter;
import util.StatisticPrinter.StatisticKey;
import checkers.inference.model.Constraint;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import constraintsolver.BackEnd;
import constraintsolver.Lattice;

public class LogiQLBackEnd extends BackEnd<String, String> {

    private final StringBuilder logiQLText = new StringBuilder();
    private final File logiqldata = new File(new File("").getAbsolutePath() + "/logiqldata");
    public static AtomicInteger nth = new AtomicInteger(0);
    private long serializationStart;
    private long serializationEnd;
    private long solvingStart;
    private long solvingEnd;
    public LogiQLBackEnd(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, Serializer<String, String> realSerializer,
            Lattice lattice) {
        super(configuration, slots, constraints, qualHierarchy, processingEnvironment, realSerializer,
                lattice);
        logiqldata.mkdir();

    }

    @Override
    public Map<Integer, AnnotationMirror> solve() {
        int localNth = nth.incrementAndGet();
        String logiqldataPath = logiqldata.getAbsolutePath();
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        /**
         * creating a instance of LogiqlConstraintGenerator and running
         * GenerateLogiqlEncoding method, in order to generate the logiql fixed
         * encoding part of current type system.
         */
        LogiQLPredicateGenerator constraintGenerator = new LogiQLPredicateGenerator(logiqldataPath,
                lattice, localNth);
        constraintGenerator.GenerateLogiqlEncoding();
        this.serializationStart = System.currentTimeMillis();
        this.convertAll();
        this.serializationEnd = System.currentTimeMillis();
        StatisticPrinter.record(StatisticKey.LOGIQL_SERIALIZATION_TIME,
                (serializationEnd - serializationStart));
        addVariables();
        addConstants();
        writeLogiQLData(logiqldataPath, localNth);
        // System.out.println(logiQLText.toString());
        // delete and create new workspace in local machine for testing.
        // writeDeleteData(logiqldataPath);
        this.solvingStart = System.currentTimeMillis();
        LogicBloxRunner runLogicBlox = new LogicBloxRunner(logiqldataPath, localNth);
        runLogicBlox.runLogicBlox();
        this.solvingEnd = System.currentTimeMillis();
        boolean graph = (configuration.get("useGraph") == null || configuration.get("useGraph").equals(
                "true")) ? true : false;
        if (graph) {
            StatisticPrinter.record(StatisticKey.LOGIQL_SOLVING_GRAPH_SEQUENTIAL_TIME,
                    (solvingEnd - solvingStart));
        } else {
            StatisticPrinter.record(StatisticKey.LOGIQL_SOLVING_WITHOUT_GRAPH_TIME,
                    (solvingEnd - solvingStart));
        }
        DecodingTool DecodeTool = new DecodingTool(varSlotIds, logiqldataPath, lattice, localNth);
        result = DecodeTool.decodeResult();
        // PrintUtils.printResult(result);

        return result;
    }

    @Override
    public void convertAll() {
        for (Constraint constraint : constraints) {
            collectVarSlots(constraint);
            String serializedConstrant = constraint.serialize(realSerializer);
            if (serializedConstrant != null) {
                logiQLText.append(serializedConstrant);
            }
        }
    }

    private void addConstants() {
        for (AnnotationMirror annoMirror : lattice.getAllTypes()) {
            String constant = NameUtils.getSimpleName(annoMirror);
            logiQLText.insert(0, "+constant(c), +hasconstantName[c] = \"" + constant + "\".\n");
        }
    }

    private void addVariables() {
        for (Integer variable : varSlotIds) {
            logiQLText.insert(0, "+variable(v), +hasvariableName[v] = " + variable + ".\n");
        }
    }

    private void writeLogiQLData(String path, int nth) {
        String[] lines = logiQLText.toString().split("\r\n|\r|\n");
        StatisticPrinter.record(StatisticKey.LOGIQL_DATA_SIZE, (long) lines.length);
        try {
            String writePath = path + "/data" + nth + ".logic";
            File f = new File(writePath);
            PrintWriter pw = new PrintWriter(f);
            pw.write(logiQLText.toString());
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeDeleteData(String path) {
        try {
            String writeDeletePath = path + "/deleteData.logic";
            String deleteContent = "-variable(v) <- variable@prev(v).";
            File df = new File(writeDeletePath);
            PrintWriter dpw = new PrintWriter(df);
            dpw.write(deleteContent);
            dpw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
