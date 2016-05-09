package ontology.solver;

import ontology.qual.OntologyTop;
import ontology.util.OntologyUtils;

import org.checkerframework.javacutil.AnnotationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;

import checkers.inference.InferenceMain;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.EqualityConstraint;
import checkers.inference.model.InequalityConstraint;
import checkers.inference.model.Slot;
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.model.VariableSlot;
import checkers.inference.model.serialization.CnfVecIntSerializer;

public class OntologySerializer extends CnfVecIntSerializer {
    // private SlotManager slotManager;
    protected final String datatype;
    private final Set<Integer> touchedSlots = new HashSet<Integer>();

    public OntologySerializer(String datatype) {
        super(InferenceMain.getInstance().getSlotManager());
        // this.slotManager = InferenceMain.getInstance().getSlotManager();
        this.datatype = datatype;
        // System.out.println(datatype);
    }

    @Override
    protected boolean isTop(ConstantSlot constantSlot) {
        AnnotationMirror anno = constantSlot.getValue();
        return annoIsPresented(anno);
    }

    private boolean annoIsPresented(AnnotationMirror anno) {
        if (AnnotationUtils.areSameByClass(anno, OntologyTop.class)) {
            return true;
        }
        String[] datatypes = OntologyUtils.getOntologyValue(anno);
        return Arrays.asList(datatypes).contains(datatype);
    }

    @Override
    public List<VecInt> convertAll(Iterable<Constraint> constraints,
            List<VecInt> results) {
        List<Constraint> constraintsNoBaseCase = new ArrayList<Constraint>();
        for (Constraint constraint : constraints) {
            if (checkConstraintType(constraint)) {
                for (VecInt res : constraint.serialize(this)) {
                    if (res.size() != 0) {
                        results.add(res);
                    }
                }
                // constraints.iterator().remove();
            } else {
                constraintsNoBaseCase.add(constraint);
            }

            // System.out.println("This constraint: ****" +
            // constraint.toString());
        }
        iterateNormalCases(constraintsNoBaseCase, results);
        return results;
    }

    private boolean checkConstraintType(Constraint constraint) {
        if (constraint instanceof SubtypeConstraint) {
            Slot subtype = ((SubtypeConstraint) constraint).getSubtype();
            Slot supertype = ((SubtypeConstraint) constraint).getSupertype();
            return checkSlotTypeInSubtype(supertype, subtype);
        } else if (constraint instanceof EqualityConstraint) {
            Slot first = ((EqualityConstraint) constraint).getFirst();
            Slot second = ((EqualityConstraint) constraint).getSecond();
            return checkSlotTypeInEquality(first, second);
        }
        return false;
    }

    private boolean checkSlotTypeInSubtype(Slot supertype, Slot subtype) {
        if (!(supertype instanceof ConstantSlot)) {
            if (subtype instanceof ConstantSlot) {
                if (isTop((ConstantSlot) subtype)) {
                    this.touchedSlots.add(((VariableSlot) supertype).getId());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkSlotTypeInEquality(Slot first, Slot second) {
        if (first instanceof VariableSlot && second instanceof ConstantSlot) {
            if (isTop((ConstantSlot) second)) {
                this.touchedSlots.add(((VariableSlot) first).getId());
                return true;
            }
        } else if (second instanceof VariableSlot
                && first instanceof ConstantSlot) {
            if (isTop((ConstantSlot) first)) {
                this.touchedSlots.add(((VariableSlot) second).getId());
                return true;
            }
        }
        return false;
    }

    private void iterateNormalCases(List<Constraint> constraints,
            List<VecInt> results) {
        int touchedAfterwards, touched;
        touchedAfterwards = touched = this.touchedSlots.size();
        Iterator<Constraint> i = constraints.iterator();
        while (true) {
            while (i.hasNext()) {
                Constraint constraint = i.next();
                if (checkTouched(constraint)) {
                    for (VecInt res : constraint.serialize(this)) {
                        if (res.size() != 0) {
                            results.add(res);
                        }
                    }
                    touchedAfterwards++;
                    i.remove();
                }
            }
            if (touchedAfterwards == touched) {
                break;
            } else {
                touched = touchedAfterwards;
            }
        }
    }

    private boolean checkTouched(Constraint constraint) {
        Slot first;
        Slot second;
        if (constraint instanceof SubtypeConstraint) {
            first = ((SubtypeConstraint) constraint).getFirst();
            second = ((SubtypeConstraint) constraint).getSecond();
            return checkSlotTouched(first, second);
        } else if (constraint instanceof EqualityConstraint) {
            first = ((EqualityConstraint) constraint).getFirst();
            second = ((EqualityConstraint) constraint).getSecond();
            return checkSlotTouched(first, second);
        } else if (constraint instanceof InequalityConstraint) {
            first = ((InequalityConstraint) constraint).getFirst();
            second = ((InequalityConstraint) constraint).getSecond();
            return checkSlotTouched(first, second);
        }
        return false;
    }

    private boolean checkSlotTouched(Slot first, Slot second) {
        int firstId, secondId;
        boolean touchedFirst = false;
        boolean touchedsecond = false;
        firstId = secondId = 0;
        if (!(first instanceof ConstantSlot)) {
            firstId = ((VariableSlot) first).getId();
        }
        if (!(second instanceof ConstantSlot)) {
            secondId = ((VariableSlot) second).getId();
        }
        if (firstId != 0) {
            if (this.touchedSlots.contains(firstId)) {
                touchedFirst = true;
            }
        }
        if (secondId != 0) {
            if (this.touchedSlots.contains(secondId)) {
                touchedsecond = true;
            }
        }
        if (touchedFirst && !touchedsecond) {
            if (secondId != 0) {
                this.touchedSlots.add(secondId);
            }
        } else if (!touchedFirst && touchedsecond) {
            if (firstId != 0) {
                this.touchedSlots.add(firstId);
            }
        }
        return (touchedFirst || touchedsecond);
    }
}
