package constraintsolver;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.inference.InferenceSolution;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;

/**
 * The back end of inference solver, the super type of concrete solver.
 * 
 * @author jianchu
 *
 */
public abstract class BackEnd {

    public Map<String, String> configuration;
    public Collection<Slot> slots;
    public Collection<Constraint> constraints;
    public QualifierHierarchy qualHierarchy;
    public ProcessingEnvironment processingEnvironment;
    public Serializer realSerializer;
    public Set<Integer> varSlotIds;

    public BackEnd(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, Serializer realSerializer) {

        this.configuration = configuration;
        this.slots = slots;
        this.constraints = constraints;
        this.qualHierarchy = qualHierarchy;
        this.processingEnvironment = processingEnvironment;
        this.realSerializer = realSerializer;
        this.varSlotIds = new HashSet<Integer>();
    }

    public abstract InferenceSolution solve();

    public abstract void convertAll();

    /**
     * Get slot id from variable slot.
     * 
     * @param constraint
     */
    public void collectVarSlots(Constraint constraint) {
        for (Slot slot : constraint.getSlots()) {
            if (!(slot instanceof ConstantSlot)) {
                this.varSlotIds.add(((VariableSlot) slot).getId());
            }
        }
    }
}
