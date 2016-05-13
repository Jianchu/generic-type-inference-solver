package logiqlbackend;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import util.NameUtils;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceSolution;
import checkers.inference.SlotManager;
import checkers.inference.model.Constraint;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import constraintsolver.BackEnd;
import constraintsolver.Lattice;

public class LogiQLBackEnd extends BackEnd<String, String> {

    private final SlotManager slotManager;
    private final StringBuilder logiQLText = new StringBuilder();
    private final File logiqldata = new File(new File("").getAbsolutePath() + "/logiqldata");

    public LogiQLBackEnd(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, Serializer<String, String> realSerializer) {
        super(configuration, slots, constraints, qualHierarchy, processingEnvironment, realSerializer);
        this.slotManager = InferenceMain.getInstance().getSlotManager();
        Lattice.configure(qualHierarchy);
        logiqldata.mkdir();

    }

    @Override
    public InferenceSolution solve() {
        String logiqldataPath = logiqldata.getAbsolutePath();
        /**
         * creating a instance of LogiqlConstraintGenerator and running
         * GenerateLogiqlEncoding method, in order to generate the logiql fixed
         * encoding part of current type system.
         */
        LogiQLPredicateGenerator constraintGenerator = new LogiQLPredicateGenerator(logiqldataPath);
        constraintGenerator.GenerateLogiqlEncoding();

        this.convertAll();
        addVariables();
        addConstants();
        writeLogiQLData(logiqldataPath);
        // System.out.println(logiQLText.toString());

        return null;
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
        for (AnnotationMirror annoMirror : Lattice.allTypes) {
            String constant = NameUtils.getSimpleName(annoMirror);
            logiQLText.insert(0, "+constant(c), +hasconstantName[c] = \"" + constant + "\".\n");
        }
    }

    private void addVariables() {
        for (Integer variable : varSlotIds) {
            logiQLText.insert(0, "+variable(v), +hasvariableName[v] = " + variable + ".\n");
        }
    }

    private void writeLogiQLData(String path) {
        try {
            String writePath = path + "/data.logic";
            File f = new File(writePath);
            PrintWriter pw = new PrintWriter(f);
            pw.write(logiQLText.toString());
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
