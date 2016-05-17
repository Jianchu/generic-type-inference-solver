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
 *         Type parameters S and T correspond to the corresponding type
 *         parameters of Serializer.
 */
// JLTODO: go through all uses of BackEnd and add type arguments
public abstract class BackEnd<S, T> {

    protected final Map<String, String> configuration;
    protected final Collection<Slot> slots;
    protected final Collection<Constraint> constraints;
    protected final QualifierHierarchy qualHierarchy;
    private final ProcessingEnvironment processingEnvironment;
    protected final Serializer<S, T> realSerializer;
    protected final Set<Integer> varSlotIds;

    public BackEnd(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, Serializer<S, T> realSerializer) {
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

    public Collection<Slot> getSlots() {
        return this.slots;
    }

    public Collection<Constraint> getConstraints() {
        return this.constraints;
    }

    public ProcessingEnvironment getEnvironment() {
        return this.processingEnvironment;
    }
}
