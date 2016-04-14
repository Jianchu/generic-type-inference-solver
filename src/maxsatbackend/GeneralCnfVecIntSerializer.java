package generalmaxsatsolver;

import org.checkerframework.javacutil.AnnotationUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;

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
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.model.VariableSlot;


public class GeneralCnfVecIntSerializer implements Serializer<VecInt[], VecInt[]> {
    // private final SlotManager slotManager;
    private final LatticeGenerator lattice;

    public GeneralCnfVecIntSerializer(SlotManager slotManager,
            LatticeGenerator lattice) {
        // this.slotManager = slotManager;
        this.lattice = lattice;
    }

    public List<VecInt> convertAll(Iterable<Constraint> constraints) {
        return convertAll(constraints, new LinkedList<VecInt>());
    }

    public List<VecInt> convertAll(Iterable<Constraint> constraints,
            List<VecInt> results) {
        for (Constraint constraint : constraints) {
            for (VecInt res : constraint.serialize(this)) {
                if (res.size() != 0) {
                    System.out.println(res.toString());
                    results.add(res);
                }
            }
        }
        return results;
    }

    @Override
    public VecInt[] serialize(SubtypeConstraint constraint) {
        return new VariableCombos<SubtypeConstraint>() {

            @Override
            protected VecInt[] constant_variable(ConstantSlot subtype,
                    VariableSlot supertype, SubtypeConstraint constraint) {
                int numForsupertype = 0;
                List<Integer> list = new ArrayList<Integer>();
                if (areSameType(subtype.getValue(),lattice.top)) {
                    return asVecArray(lattice.modifierInt.get(lattice.top)
                            + lattice.numModifiers * (supertype.getId() - 1));
                }
                //AnnotationUtils.areSameIgnoringValues(a,subtype.getValue())
                for (AnnotationMirror sub : lattice.subType.get(subtype.getValue())) {
                    if (!areSameType(sub,subtype.getValue())) {
                        numForsupertype = lattice.modifierInt.get(sub)
                                + lattice.numModifiers
                                * (supertype.getId() - 1);
                        list.add(-numForsupertype);
                    }
                }
                VecInt[] result = new VecInt[list.size()];
                if (list.size() > 0) {
                    Iterator<Integer> iterator = list.iterator();
                    for (int i = 0; i < result.length; i++) {
                        result[i] = asVec(iterator.next().intValue());
                    }
                    return result;
                }
                return emptyClauses;
            }

            @Override
            protected VecInt[] variable_constant(VariableSlot subtype,
                    ConstantSlot supertype, SubtypeConstraint constraint) {
                int numForsupertype = 0;
                List<Integer> list = new ArrayList<Integer>();
                if (areSameType(supertype.getValue(),lattice.bottom)) {
                    return asVecArray(lattice.modifierInt.get(lattice.bottom)
                            + lattice.numModifiers * (subtype.getId() - 1));
                }

                for (AnnotationMirror sup : lattice.superType.get(supertype.getValue())) {
                    if (!areSameType(sup,supertype.getValue())) {
                        numForsupertype = lattice.modifierInt.get(sup)
                                + lattice.numModifiers * (subtype.getId() - 1);
                        list.add(-numForsupertype);
                    }
                }
                VecInt[] result = new VecInt[list.size()];
                if (list.size() > 0) {
                    Iterator<Integer> iterator = list.iterator();
                    for (int i = 0; i < result.length; i++) {
                        result[i] = asVec(iterator.next().intValue());
                    }
                    return result;
                }
                return emptyClauses;
            }

            @Override
            protected VecInt[] variable_variable(VariableSlot subtype,
                    VariableSlot supertype, SubtypeConstraint constraint) {
                VecInt supertypeOfTop = asVec(
                        -(lattice.modifierInt.get(lattice.top) + lattice.numModifiers
                                * (subtype.getId() - 1)),
                        lattice.modifierInt.get(lattice.top)
                                + lattice.numModifiers
                                * (supertype.getId() - 1));
                VecInt subtypeOfBottom = asVec(
                        -(lattice.modifierInt.get(lattice.bottom) + lattice.numModifiers
                                * (supertype.getId() - 1)),
                        lattice.modifierInt.get(lattice.bottom)
                                + lattice.numModifiers * (subtype.getId() - 1));

                List<VecInt> list = new ArrayList<VecInt>();
                for (AnnotationMirror modifier : lattice.allTypes) {
                    // if we know subtype
                    if (!areSameType(modifier,lattice.top)) {
                        int[] superArray = new int[lattice.superType.get(modifier).size()+1];
                        int i = 1;
                        superArray[0] = -(lattice.modifierInt.get(modifier) + lattice.numModifiers * (subtype.getId() - 1));
                        for (AnnotationMirror sup : lattice.superType.get(modifier)) {
                            //if (!areSameType(sup,modifier)) {
                                superArray[i] = lattice.modifierInt.get(sup) + lattice.numModifiers * (supertype.getId() - 1);
                                i++;
                            //}
                        }
                        list.add(asVec(superArray));
                    }
                    // if we know supertype
                    if (!areSameType(modifier, lattice.bottom)) {
                        int[] subArray = new int[lattice.subType.get(modifier).size()+1];
                        int j = 1;
                        subArray[0] = -(lattice.modifierInt.get(modifier) + lattice.numModifiers * (supertype.getId()-1));
                        for (AnnotationMirror sub : lattice.subType.get(modifier)) {
                            //if (!areSameType(sub,modifier)){
                                subArray[j] = lattice.modifierInt.get(sub) + lattice.numModifiers * (subtype.getId()-1);
                                j++;
                            //}
                        }
                        list.add(asVec(subArray));
                    }
                }
                list.add(supertypeOfTop);
                list.add(subtypeOfBottom);
                VecInt[] result = list.toArray(new VecInt[list.size()]);
                return result;
            }

        }.accept(constraint.getSubtype(), constraint.getSupertype(), constraint);
    }

    @Override
    public VecInt[] serialize(EqualityConstraint constraint) {
        return new VariableCombos<EqualityConstraint>() {

            @Override
            protected VecInt[] constant_variable(ConstantSlot slot1, VariableSlot slot2, EqualityConstraint constraint) {
                VecInt[] result = new VecInt[lattice.numModifiers];
                int i =0;
                for (AnnotationMirror modifiers : lattice.allTypes) {
                    if (areSameType(slot1.getValue(), modifiers)) {
                        result[i] = asVec(
                                lattice.modifierInt.get(slot1.getValue()) + lattice.numModifiers * (slot2.getId() - 1));
                    }
                    else{
                        //cannot be other modifiers
                        result[i] = asVec(
                                -(lattice.modifierInt.get(modifiers) + lattice.numModifiers * (slot2.getId() - 1)));
                    }
                    i++;
                }
                return result;
            }

            @Override
            protected VecInt[] variable_constant(VariableSlot slot1, ConstantSlot slot2, EqualityConstraint constraint) {
                return constant_variable(slot2, slot1, constraint);
            }

            @Override
            protected VecInt[] variable_variable(VariableSlot slot1, VariableSlot slot2, EqualityConstraint constraint) {
                // a <=> b which is the same as (!a v b) & (!b v a)
                VecInt[] result = new VecInt[lattice.numModifiers * 2];
                int i = 0;
                for (AnnotationMirror modifiers : lattice.allTypes) {
                    result[i] = asVec(
                            -(lattice.modifierInt.get(modifiers) + lattice.numModifiers * (slot1.getId() - 1)),
                            lattice.modifierInt.get(modifiers) + lattice.numModifiers * (slot2.getId() - 1));
                    result[i + 1] = asVec(
                            -(lattice.modifierInt.get(modifiers) + lattice.numModifiers * (slot2.getId() - 1)),
                            lattice.modifierInt.get(modifiers) + lattice.numModifiers * (slot1.getId() - 1));
                    i = i + 2;
                }
                return result;
            }
        }.accept(constraint.getFirst(), constraint.getSecond(), constraint);
    }

    @Override
    public VecInt[] serialize(InequalityConstraint constraint) {
        return new VariableCombos<InequalityConstraint>() {

            @Override
            protected VecInt[] constant_variable(ConstantSlot slot1, VariableSlot slot2, InequalityConstraint constraint) {
                return asVecArray(
                        -(lattice.modifierInt.get(slot1.getValue()) + lattice.numModifiers * (slot2.getId() - 1)));
            }

            @Override
            protected VecInt[] variable_constant(VariableSlot slot1, ConstantSlot slot2, InequalityConstraint constraint) {
                return constant_variable(slot2, slot1, constraint);
            }

            @Override
            protected VecInt[] variable_variable(VariableSlot slot1, VariableSlot slot2, InequalityConstraint constraint) {
                // a <=> !b which is the same as (!a v !b) & (b v a)
                VecInt[] result = new VecInt[lattice.numModifiers * 2];
                int i = 0;
                for (AnnotationMirror modifiers : lattice.allTypes) {
                    result[i] = asVec(
                            -(lattice.modifierInt.get(modifiers) + lattice.numModifiers * (slot1.getId() - 1)),
                            -(lattice.modifierInt.get(modifiers) + lattice.numModifiers * (slot2.getId() - 1)));
                    //result[i+1] = asVec(lattice.modifierInt.get(modifiers) + lattice.numModifiers * (slot2.getId()-1), lattice.modifierInt.get(modifiers) + lattice.numModifiers * (slot1.getId()-1));
                    i++;
                }
                return result;
            }

        }.accept(constraint.getFirst(), constraint.getSecond(), constraint);
    }


    @Override
    public VecInt[] serialize(ComparableConstraint comparableConstraint) {
        ComparableConstraint constraint = comparableConstraint;
        return new VariableCombos<ComparableConstraint>() {
            @Override
            protected VecInt[] variable_variable(VariableSlot slot1, VariableSlot slot2, ComparableConstraint constraint) {
                // a <=> !b which is the same as (!a v !b) & (b v a)
                List<VecInt> list = new ArrayList<VecInt>();
                for (AnnotationMirror modifiers : lattice.allTypes) {
                    if (!lattice.notComparableType.get(modifiers).isEmpty()) {
                        for (AnnotationMirror notComparable : lattice.notComparableType.get(modifiers)) {
                            list.add(asVec(
                                    -(lattice.modifierInt.get(modifiers) + lattice.numModifiers * (slot1.getId() - 1)),
                                    -(lattice.modifierInt.get(notComparable)
                                            + lattice.numModifiers * (slot2.getId() - 1))));
                        }
                    }
                }
                VecInt[] result = list.toArray(new VecInt[list.size()]);
                return result;
            }
        }.accept(constraint.getFirst(), constraint.getSecond(), constraint);
        // TODO Auto-generated method stub
    }


    @Override
    public VecInt[] serialize(ExistentialConstraint constraint) {
        // TODO Auto-generated method stub
        return emptyClauses;
    }

    @Override
    public VecInt[] serialize(VariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VecInt[] serialize(ConstantSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VecInt[] serialize(ExistentialVariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VecInt[] serialize(RefinementVariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VecInt[] serialize(CombVariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }



    @Override
    public VecInt[] serialize(CombineConstraint combineConstraint) {
        // TODO Auto-generated method stub
        return emptyClauses;
    }

    @Override
    public VecInt[] serialize(PreferenceConstraint preferenceConstraint) {
        throw new UnsupportedOperationException("APPLY WEIGHTING FOR WEIGHTED MAX-SAT");
    }

    boolean areSameType(AnnotationMirror m1, AnnotationMirror m2) {
        //System.out.println(AnnotationUtils.areSameIgnoringValues(m1, m2) +"  "+ m1.toString() + "  " + m2.toString());
        return AnnotationUtils.areSameIgnoringValues(m1, m2);
    }

    VecInt asVec(int... result) {
        return new VecInt(result);
    }

    VecInt[] asVecArray(int... vars) {
        return new VecInt[] { new VecInt(vars) };
    }

    class VariableCombos<T extends Constraint> {

        protected VecInt[] variable_variable(VariableSlot slot1,
                VariableSlot slot2, T constraint) {
            return defaultAction(slot1, slot2, constraint);
        }

        protected VecInt[] constant_variable(ConstantSlot slot1,
                VariableSlot slot2, T constraint) {
            return defaultAction(slot1, slot2, constraint);
        }

        protected VecInt[] variable_constant(VariableSlot slot1,
                ConstantSlot slot2, T constraint) {
            return defaultAction(slot1, slot2, constraint);
        }

        protected VecInt[] constant_constant(ConstantSlot slot1,
                ConstantSlot slot2, T constraint) {
            return defaultAction(slot1, slot2, constraint);
        }

        public VecInt[] defaultAction(Slot slot1, Slot slot2, T constraint) {
            return emptyClauses;
        }

        public VecInt[] accept(Slot slot1, Slot slot2, T constraint) {
            final VecInt[] result;
            if (slot1 instanceof ConstantSlot) {
                if (slot2 instanceof ConstantSlot) {
                    result = constant_constant((ConstantSlot) slot1,
                            (ConstantSlot) slot2, constraint);
                } else {
                    result = constant_variable((ConstantSlot) slot1,
                            (VariableSlot) slot2, constraint);
                }
            } else if (slot2 instanceof ConstantSlot) {
                result = variable_constant((VariableSlot) slot1,
                        (ConstantSlot) slot2, constraint);
            } else {
                result = variable_variable((VariableSlot) slot1,
                        (VariableSlot) slot2, constraint);
            }

            return result;
        }
    }

    public static final VecInt[] emptyClauses = new VecInt[0];

}

