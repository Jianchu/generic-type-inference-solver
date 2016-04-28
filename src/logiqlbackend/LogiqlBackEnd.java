package logiqlbackend;

import org.checkerframework.framework.type.QualifierHierarchy;

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

public class LogiqlBackEnd extends BackEnd<String, String> {

    private final SlotManager slotManager;
    private final List<String> logiQLText = new ArrayList<String>();

    public LogiqlBackEnd(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, Serializer<String, String> realSerializer) {
        super(configuration, slots, constraints, qualHierarchy, processingEnvironment, realSerializer);
        this.slotManager = InferenceMain.getInstance().getSlotManager();
        Lattice.configure(qualHierarchy);
    }

    @Override
    public InferenceSolution solve() {
        this.convertAll();
        // TODO Auto-generated method stub
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
