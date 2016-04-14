package maxsatbackend;

import org.checkerframework.javacutil.AnnotationUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;

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
import constraintsolver.Lattice;


public class MaxSatSerializer implements Serializer<VecInt[], VecInt[]> {

    public MaxSatSerializer() {

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
                if (areSameType(subtype.getValue(), Lattice.top)) {
                    return asVecArray(Lattice.modifierInt.get(Lattice.top)
                            + Lattice.numModifiers * (supertype.getId() - 1));
                }
                //AnnotationUtils.areSameIgnoringValues(a,subtype.getValue())
                for (AnnotationMirror sub : Lattice.subType.get(subtype.getValue())) {
                    if (!areSameType(sub,subtype.getValue())) {
                        numForsupertype = Lattice.modifierInt.get(sub)
                                + Lattice.numModifiers
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
                if (areSameType(supertype.getValue(),Lattice.bottom)) {
                    return asVecArray(Lattice.modifierInt.get(Lattice.bottom)
                            + Lattice.numModifiers * (subtype.getId() - 1));
                }

                for (AnnotationMirror sup : Lattice.superType.get(supertype.getValue())) {
                    if (!areSameType(sup,supertype.getValue())) {
                        numForsupertype = Lattice.modifierInt.get(sup)
                                + Lattice.numModifiers * (subtype.getId() - 1);
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
                        -(Lattice.modifierInt.get(Lattice.top) + Lattice.numModifiers
                                * (subtype.getId() - 1)),
                        Lattice.modifierInt.get(Lattice.top)
                                + Lattice.numModifiers
                                * (supertype.getId() - 1));
                VecInt subtypeOfBottom = asVec(
                        -(Lattice.modifierInt.get(Lattice.bottom) + Lattice.numModifiers
                                * (supertype.getId() - 1)),
                        Lattice.modifierInt.get(Lattice.bottom)
                                + Lattice.numModifiers * (subtype.getId() - 1));

                List<VecInt> list = new ArrayList<VecInt>();
                for (AnnotationMirror modifier : Lattice.allTypes) {
                    // if we know subtype
                    if (!areSameType(modifier,Lattice.top)) {
                        int[] superArray = new int[Lattice.superType.get(modifier).size()+1];
                        int i = 1;
                        superArray[0] = -(Lattice.modifierInt.get(modifier) + Lattice.numModifiers * (subtype.getId() - 1));
                        for (AnnotationMirror sup : Lattice.superType.get(modifier)) {
                            //if (!areSameType(sup,modifier)) {
                                superArray[i] = Lattice.modifierInt.get(sup) + Lattice.numModifiers * (supertype.getId() - 1);
                                i++;
                            //}
                        }
                        list.add(asVec(superArray));
                    }
                    // if we know supertype
                    if (!areSameType(modifier, Lattice.bottom)) {
                        int[] subArray = new int[Lattice.subType.get(modifier).size()+1];
                        int j = 1;
                        subArray[0] = -(Lattice.modifierInt.get(modifier) + Lattice.numModifiers * (supertype.getId()-1));
                        for (AnnotationMirror sub : Lattice.subType.get(modifier)) {
                            //if (!areSameType(sub,modifier)){
                                subArray[j] = Lattice.modifierInt.get(sub) + Lattice.numModifiers * (subtype.getId()-1);
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
                VecInt[] result = new VecInt[Lattice.numModifiers];
                int i =0;
                for (AnnotationMirror modifiers : Lattice.allTypes) {
                    if (areSameType(slot1.getValue(), modifiers)) {
                        result[i] = asVec(
                                Lattice.modifierInt.get(slot1.getValue()) + Lattice.numModifiers * (slot2.getId() - 1));
                    }
                    else{
                        //cannot be other modifiers
                        result[i] = asVec(
                                -(Lattice.modifierInt.get(modifiers) + Lattice.numModifiers * (slot2.getId() - 1)));
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
                VecInt[] result = new VecInt[Lattice.numModifiers * 2];
                int i = 0;
                for (AnnotationMirror modifiers : Lattice.allTypes) {
                    result[i] = asVec(
                            -(Lattice.modifierInt.get(modifiers) + Lattice.numModifiers * (slot1.getId() - 1)),
                            Lattice.modifierInt.get(modifiers) + Lattice.numModifiers * (slot2.getId() - 1));
                    result[i + 1] = asVec(
                            -(Lattice.modifierInt.get(modifiers) + Lattice.numModifiers * (slot2.getId() - 1)),
                            Lattice.modifierInt.get(modifiers) + Lattice.numModifiers * (slot1.getId() - 1));
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
                        -(Lattice.modifierInt.get(slot1.getValue()) + Lattice.numModifiers * (slot2.getId() - 1)));
            }

            @Override
            protected VecInt[] variable_constant(VariableSlot slot1, ConstantSlot slot2, InequalityConstraint constraint) {
                return constant_variable(slot2, slot1, constraint);
            }

            @Override
            protected VecInt[] variable_variable(VariableSlot slot1, VariableSlot slot2, InequalityConstraint constraint) {
                // a <=> !b which is the same as (!a v !b) & (b v a)
                VecInt[] result = new VecInt[Lattice.numModifiers * 2];
                int i = 0;
                for (AnnotationMirror modifiers : Lattice.allTypes) {
                    result[i] = asVec(
                            -(Lattice.modifierInt.get(modifiers) + Lattice.numModifiers * (slot1.getId() - 1)),
                            -(Lattice.modifierInt.get(modifiers) + Lattice.numModifiers * (slot2.getId() - 1)));
                    //result[i+1] = asVec(Lattice.modifierInt.get(modifiers) + Lattice.numModifiers * (slot2.getId()-1), Lattice.modifierInt.get(modifiers) + Lattice.numModifiers * (slot1.getId()-1));
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
                for (AnnotationMirror modifiers : Lattice.allTypes) {
                    if (!Lattice.notComparableType.get(modifiers).isEmpty()) {
                        for (AnnotationMirror notComparable : Lattice.notComparableType.get(modifiers)) {
                            list.add(asVec(
                                    -(Lattice.modifierInt.get(modifiers) + Lattice.numModifiers * (slot1.getId() - 1)),
                                    -(Lattice.modifierInt.get(notComparable)
                                            + Lattice.numModifiers * (slot2.getId() - 1))));
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

