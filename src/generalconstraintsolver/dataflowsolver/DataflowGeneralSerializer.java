package generalconstraintsolver.dataflowsolver;

import org.checkerframework.javacutil.AnnotationUtils;

import generalconstraintsolver.GeneralEncodingSerializer;
import generalconstraintsolver.ImpliesLogic;
import generalconstraintsolver.LatticeGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.SlotManager;
import checkers.inference.model.CombVariableSlot;
import checkers.inference.model.CombineConstraint;
import checkers.inference.model.ComparableConstraint;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.EqualityConstraint;
import checkers.inference.model.ExistentialConstraint;
import checkers.inference.model.ExistentialVariableSlot;
import checkers.inference.model.InequalityConstraint;
import checkers.inference.model.PreferenceConstraint;
import checkers.inference.model.RefinementVariableSlot;
import checkers.inference.model.Slot;
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.model.VariableSlot;
import dataflow.quals.DataFlowTop;
import dataflow.util.DataflowUtils;



public class DataflowGeneralSerializer extends GeneralEncodingSerializer {

    private Set<Integer> touchedSlots = new HashSet<Integer>();

    public DataflowGeneralSerializer(SlotManager slotManager,
            LatticeGenerator lattice) {
        super(slotManager, lattice);
        // TODO Auto-generated constructor stub
    }

    protected boolean isTop(ConstantSlot constantSlot) {
        AnnotationMirror anno = constantSlot.getValue();
        return annoIsPresented(anno);
    }

    private boolean annoIsPresented(AnnotationMirror anno) {
        if (AnnotationUtils.areSameByClass(anno, DataFlowTop.class)) {
            return true;
        }
        String[] datatypes = DataflowUtils.getDataflowValue(anno);
        String[] datatype = DataflowUtils.getDataflowValue(this.lattice.top);
        return Arrays.asList(datatypes).contains(datatype[0]);
    }

    @Override
    public List<ImpliesLogic> convertAll(Iterable<Constraint> constraints,
            List<ImpliesLogic> results) {
        List<Constraint> constraintsNoBaseCase = new ArrayList<Constraint>();
        for (Constraint constraint : constraints) {
            if (checkConstraintType(constraint)) {
                for (ImpliesLogic res : ((ImpliesLogic[]) constraint
                        .serialize(this))) {
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
            List<ImpliesLogic> results) {
        int touchedAfterwards, touched;
        touchedAfterwards = touched = this.touchedSlots.size();
        Iterator<Constraint> i = constraints.iterator();
        while (true) {
            while (i.hasNext()) {
                Constraint constraint = i.next();
                if (checkTouched(constraint)) {
                    for (ImpliesLogic res : ((ImpliesLogic[]) constraint
                            .serialize(this))) {
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

    @Override
    public ImpliesLogic[] serialize(SubtypeConstraint constraint) {
        return new VariableCombos<SubtypeConstraint>() {

            @Override
            protected ImpliesLogic[] constant_variable(ConstantSlot subtype,
                    VariableSlot supertype, SubtypeConstraint constraint) {

                if (isTop(subtype)) {
                    return asSingleImp(lattice.modifierInt.get(lattice.top)
                            + lattice.numModifiers * (supertype.getId() - 1));
                }

                return emptyClauses;
            }

            @Override
            protected ImpliesLogic[] variable_constant(VariableSlot subtype,
                    ConstantSlot supertype, SubtypeConstraint constraint) {
                if (!isTop(supertype)) {
                    return asSingleImp(-(lattice.modifierInt.get(lattice.top) + lattice.numModifiers
                            * (subtype.getId() - 1)));
                }

                return emptyClauses;
            }

            @Override
            protected ImpliesLogic[] variable_variable(VariableSlot subtype,
                    VariableSlot supertype, SubtypeConstraint constraint) {

                ImpliesLogic supertypeOfTop = asDoubleImp(
                        (lattice.modifierInt.get(lattice.top) + lattice.numModifiers
                                * (subtype.getId() - 1)),
                        lattice.modifierInt.get(lattice.top)
                                + lattice.numModifiers
                                * (supertype.getId() - 1));
                ImpliesLogic subtypeOfBottom = asDoubleImp(
                        (lattice.modifierInt.get(lattice.bottom) + lattice.numModifiers
                                * (supertype.getId() - 1)),
                        lattice.modifierInt.get(lattice.bottom)
                                + lattice.numModifiers * (subtype.getId() - 1));

                List<ImpliesLogic> list = new ArrayList<ImpliesLogic>();
                for (AnnotationMirror modifier : lattice.allTypes) {
                    // if we know subtype
                    if (!areSameType(modifier, lattice.top)) {
                        int[] rightSide = new int[lattice.superType.get(
                                modifier).size()];
                        int[] leftSide = new int[1];
                        int i = 0;
                        leftSide[0] = (lattice.modifierInt.get(modifier) + lattice.numModifiers
                                * (subtype.getId() - 1));
                        for (AnnotationMirror sup : lattice.superType
                                .get(modifier)) {
                            rightSide[i] = lattice.modifierInt.get(sup)
                                    + lattice.numModifiers
                                    * (supertype.getId() - 1);
                            i++;
                        }
                        list.add(asMutipleImp(leftSide, rightSide,
                                "right-false"));
                    }
                    // if we know supertype
                    if (!areSameType(modifier, lattice.bottom)) {
                        int[] rightSide = new int[lattice.subType.get(modifier)
                                .size()];
                        int[] leftSide = new int[1];
                        int j = 0;
                        leftSide[0] = (lattice.modifierInt.get(modifier) + lattice.numModifiers
                                * (supertype.getId() - 1));
                        for (AnnotationMirror sub : lattice.subType
                                .get(modifier)) {
                            rightSide[j] = lattice.modifierInt.get(sub)
                                    + lattice.numModifiers
                                    * (subtype.getId() - 1);
                            j++;
                        }
                        list.add(asMutipleImp(leftSide, rightSide,
                                "right-false"));
                    }
                }
                list.add(supertypeOfTop);
                list.add(subtypeOfBottom);
                ImpliesLogic[] result = list.toArray(new ImpliesLogic[list
                        .size()]);
                return result;
            }

        }.accept(constraint.getSubtype(), constraint.getSupertype(), constraint);
    }

    @Override
    public Object serialize(ExistentialConstraint constraint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(VariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(ConstantSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(ExistentialVariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(RefinementVariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(CombVariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(ComparableConstraint comparableConstraint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(CombineConstraint combineConstraint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(PreferenceConstraint preferenceConstraint) {
        // TODO Auto-generated method stub
        return null;
    }
}
