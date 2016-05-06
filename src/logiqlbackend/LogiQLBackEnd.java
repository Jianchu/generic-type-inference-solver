package logiqlbackend;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;

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
    private final List<String> logiQLText = new ArrayList<String>();
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
        // LogiqlConstraintGenerator l = new LogiqlConstraintGenerator();
        return null;
    }

    @Override
    public void convertAll() {
        for (Constraint constraint : constraints) {
            collectVarSlots(constraint);
            logiQLText.add(constraint.serialize(realSerializer));
        }
    }
}
