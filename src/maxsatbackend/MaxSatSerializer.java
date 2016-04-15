package maxsatbackend;

import org.checkerframework.javacutil.AnnotationUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;

import util.MathUtils;
import util.VectorUtils;
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

    private VecInt[] getMustNotBe(Set<AnnotationMirror> mustNotBe, VariableSlot vSlot, ConstantSlot cSlot) {

        List<Integer> resultList = new ArrayList<Integer>();

        for (AnnotationMirror sub : mustNotBe) {
            if (!areSameType(sub, cSlot.getValue())) {
                resultList.add(-MathUtils.mapIdToMatrixEntry(vSlot.getId(), sub));
            }
        }

        VecInt[] result = new VecInt[resultList.size()];
        if (resultList.size() > 0) {
            Iterator<Integer> iterator = resultList.iterator();
            for (int i = 0; i < result.length; i++) {
                result[i] = VectorUtils.asVec(iterator.next().intValue());
            }
            return result;
        }
        return emptyClauses;
    }

    @Override
    public VecInt[] serialize(SubtypeConstraint constraint) {

        Set<AnnotationMirror> mustNotBe = new HashSet<AnnotationMirror>();

        return new VariableCombos<SubtypeConstraint>() {
            @Override
            protected VecInt[] constant_variable(ConstantSlot subtype, VariableSlot supertype,
                    SubtypeConstraint constraint) {

                if (areSameType(subtype.getValue(), Lattice.top)) {
                    return VectorUtils.asVecArray(MathUtils.mapIdToMatrixEntry(supertype.getId(), Lattice.top));
                }

                mustNotBe.addAll(Lattice.subType.get(subtype.getValue()));
                mustNotBe.addAll(Lattice.notComparableType.get(subtype.getValue()));

                return getMustNotBe(mustNotBe, supertype, subtype);
            }

            @Override
            protected VecInt[] variable_constant(VariableSlot subtype, ConstantSlot supertype,
                    SubtypeConstraint constraint) {
                
                if (areSameType(supertype.getValue(),Lattice.bottom)) {
                    return VectorUtils.asVecArray(MathUtils.mapIdToMatrixEntry(subtype.getId(), Lattice.bottom));
                }

                mustNotBe.addAll(Lattice.superType.get(supertype.getValue()));
                mustNotBe.addAll(Lattice.notComparableType.get(supertype.getValue()));

                return getMustNotBe(mustNotBe, subtype, supertype);
            }

            @Override
            protected VecInt[] variable_variable(VariableSlot subtype,
                    VariableSlot supertype, SubtypeConstraint constraint) {
                VecInt supertypeOfTop = VectorUtils.asVec(
                        -(Lattice.modifierInt.get(Lattice.top) + Lattice.numModifiers
                                * (subtype.getId() - 1)),
                        Lattice.modifierInt.get(Lattice.top)
                                + Lattice.numModifiers
                                * (supertype.getId() - 1));
                VecInt subtypeOfBottom = VectorUtils.asVec(
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
                        list.add(VectorUtils.asVec(superArray));
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
                        list.add(VectorUtils.asVec(subArray));
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
                return VectorUtils.asVecArray(MathUtils.mapIdToMatrixEntry(slot2.getId(), slot1.getValue()));
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
                for (AnnotationMirror type : Lattice.allTypes) {
                    result[i] = VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(slot1.getId(), type),
                            MathUtils.mapIdToMatrixEntry(slot2.getId(), type));
                    result[i + 1] = VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(slot2.getId(), type),
                            MathUtils.mapIdToMatrixEntry(slot1.getId(), type));
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
                return VectorUtils.asVecArray(-MathUtils.mapIdToMatrixEntry(slot2.getId(), slot1.getValue()));
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
                for (AnnotationMirror type : Lattice.allTypes) {
                    result[i] = VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(slot1.getId(), type),
                            -MathUtils.mapIdToMatrixEntry(slot2.getId(), type));
                    result[i + 1] = VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(slot2.getId(), type),
                            MathUtils.mapIdToMatrixEntry(slot1.getId(), type));
                    i = i + 2;
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
                for (AnnotationMirror type : Lattice.allTypes) {
                    if (!Lattice.notComparableType.get(type).isEmpty()) {
                        for (AnnotationMirror notComparable : Lattice.notComparableType.get(type)) {
                            list.add(VectorUtils.asVec(
                                    -MathUtils.mapIdToMatrixEntry(slot1.getId(), type),
                                    -MathUtils.mapIdToMatrixEntry(slot2.getId(), type),
                                    MathUtils.mapIdToMatrixEntry(slot2.getId(), type),
                                    MathUtils.mapIdToMatrixEntry(slot1.getId(), type)));
                        }
                    }
                }
                VecInt[] result = list.toArray(new VecInt[list.size()]);
                return result;
            }
        }.accept(constraint.getFirst(), constraint.getSecond(), constraint);
    }


    @Override
    public VecInt[] serialize(ExistentialConstraint constraint) {
        return emptyClauses;
    }

    @Override
    public VecInt[] serialize(VariableSlot slot) {
        return null;
    }

    @Override
    public VecInt[] serialize(ConstantSlot slot) {
        return null;
    }

    @Override
    public VecInt[] serialize(ExistentialVariableSlot slot) {
        return null;
    }

    @Override
    public VecInt[] serialize(RefinementVariableSlot slot) {
        return null;
    }

    @Override
    public VecInt[] serialize(CombVariableSlot slot) {
        return null;
    }



    @Override
    public VecInt[] serialize(CombineConstraint combineConstraint) {
        return emptyClauses;
    }

    @Override
    public VecInt[] serialize(PreferenceConstraint preferenceConstraint) {
        throw new UnsupportedOperationException("APPLY WEIGHTING FOR WEIGHTED MAX-SAT");
    }

    boolean areSameType(AnnotationMirror m1, AnnotationMirror m2) {
        return AnnotationUtils.areSameIgnoringValues(m1, m2);
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

