package maxsatbackend;

import org.checkerframework.javacutil.ErrorReporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;

import util.ConstantUtils;
import util.MathUtils;
import util.VectorUtils;
import checkers.inference.model.CombVariableSlot;
import checkers.inference.model.CombineConstraint;
import checkers.inference.model.ComparableConstraint;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.EqualityConstraint;
import checkers.inference.model.ExistentialConstraint;
import checkers.inference.model.ExistentialVariableSlot;
import checkers.inference.model.InequalityConstraint;
import checkers.inference.model.PreferenceConstraint;
import checkers.inference.model.RefinementVariableSlot;
import checkers.inference.model.Serializer;
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.model.VariableSlot;
import constraintsolver.Lattice;
import constraintsolver.VariableCombos;

/**
 * The serializer for maxsat back end. Converting constraints to VecInt
 * 
 * @author jianchu
 *
 */

public class MaxSatSerializer implements Serializer<VecInt[], VecInt[]> {

    public MaxSatSerializer() {

    }

    @Override
    public VecInt[] serialize(SubtypeConstraint constraint) {

        final Set<AnnotationMirror> mustNotBe = new HashSet<AnnotationMirror>();

        return new VariableCombos<SubtypeConstraint, VecInt[]>(emptyClauses) {
            @Override
            protected VecInt[] constant_variable(ConstantSlot subtype, VariableSlot supertype, SubtypeConstraint constraint) {
                if (ConstantUtils.areSameType(subtype.getValue(), Lattice.top)) {
                    return VectorUtils.asVecArray(MathUtils.mapIdToMatrixEntry(supertype.getId(), Lattice.top));
                }

                mustNotBe.addAll(Lattice.subType.get(subtype.getValue()));
                if (Lattice.incomparableType.keySet().contains(subtype.getValue())) {
                    mustNotBe.addAll(Lattice.incomparableType.get(subtype.getValue()));
                }
                return getMustNotBe(mustNotBe, supertype, subtype);
            }

            @Override
            protected VecInt[] variable_constant(VariableSlot subtype, ConstantSlot supertype, SubtypeConstraint constraint) {

                if (ConstantUtils.areSameType(supertype.getValue(), Lattice.bottom)) {
                    return VectorUtils.asVecArray(MathUtils.mapIdToMatrixEntry(subtype.getId(), Lattice.bottom));
                }

                mustNotBe.addAll(Lattice.superType.get(supertype.getValue()));
                if (Lattice.incomparableType.keySet().contains(supertype.getValue())) {
                    mustNotBe.addAll(Lattice.incomparableType.get(supertype.getValue()));
                }
                return getMustNotBe(mustNotBe, subtype, supertype);
            }

            @Override
            protected VecInt[] variable_variable(VariableSlot subtype,  VariableSlot supertype, SubtypeConstraint constraint) {

                // if subtype is top, then supertype is top.
                // if supertype is bottom, then subtype is bottom.
                VecInt supertypeOfTop = VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(subtype.getId(), Lattice.top),
                        MathUtils.mapIdToMatrixEntry(supertype.getId(), Lattice.top));
                VecInt subtypeOfBottom = VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(supertype.getId(), Lattice.bottom),
                        MathUtils.mapIdToMatrixEntry(subtype.getId(), Lattice.bottom));

                List<VecInt> resultList = new ArrayList<VecInt>();
                for (AnnotationMirror type : Lattice.allTypes) {
                    // if we know subtype
                    if (!ConstantUtils.areSameType(type, Lattice.top)) {
                        resultList.add(VectorUtils.asVec(getMaybe(type, subtype, supertype,
                                Lattice.superType.get(type))));
                    }

                    // if we know supertype
                    if (!ConstantUtils.areSameType(type, Lattice.bottom)) {
                        resultList.add(VectorUtils.asVec(getMaybe(type, supertype, subtype,
                                Lattice.subType.get(type))));
                    }
                }
                resultList.add(supertypeOfTop);
                resultList.add(subtypeOfBottom);
                VecInt[] result = resultList.toArray(new VecInt[resultList.size()]);
                return result;
            }

            @Override
            protected VecInt[] constant_constant(ConstantSlot slot1, ConstantSlot slot2, SubtypeConstraint constraint) {
                if (!ConstantUtils.checkConstant(slot1, slot2, constraint)) {
                    ErrorReporter.errorAbort("Confliction in subtype constraint: " + slot1.getValue()
                            + " is not subtype of " + slot2.getValue());
                }

                return defaultAction(slot1, slot2, constraint);
            }

        }.accept(constraint.getSubtype(), constraint.getSupertype(), constraint);
    }
    
    /**
     * for subtype constraint, if supertype is constant slot, then the subtype
     * cannot be the super type of supertype, same for subtype
     * 
     * @param mustNotBe
     * @param vSlot
     * @param cSlot
     * @return
     */
    private VecInt[] getMustNotBe(Set<AnnotationMirror> mustNotBe, VariableSlot vSlot, ConstantSlot cSlot) {

        List<Integer> resultList = new ArrayList<Integer>();

        for (AnnotationMirror sub : mustNotBe) {
            if (!ConstantUtils.areSameType(sub, cSlot.getValue())) {
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

    /**
     * 
     * @param type
     * @param knownType
     * @param unknownType
     * @param maybeSet
     * @return
     */
    private int[] getMaybe(AnnotationMirror type, VariableSlot knownType, VariableSlot unknownType,
            Collection<AnnotationMirror> maybeSet) {
        int[] maybeArray = new int[maybeSet.size() + 1];
        int i = 1;
        maybeArray[0] = -MathUtils.mapIdToMatrixEntry(knownType.getId(), type);
        for (AnnotationMirror sup : maybeSet) {
            maybeArray[i] = MathUtils.mapIdToMatrixEntry(unknownType.getId(), sup);
            i++;
        }
        return maybeArray;
    }

    @Override
    public VecInt[] serialize(EqualityConstraint constraint) {
        return new VariableCombos<EqualityConstraint, VecInt[]>(emptyClauses) {

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
                VecInt[] result = new VecInt[Lattice.numTypes * 2];
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
            
            @Override
            protected VecInt[] constant_constant(ConstantSlot slot1, ConstantSlot slot2, EqualityConstraint constraint) {
                if (!ConstantUtils.checkConstant(slot1, slot2, constraint)) {
                    ErrorReporter.errorAbort("Confliction in equality constraint: " + slot1.getValue()
                            + " is not equal to " + slot2.getValue());
                }

                return defaultAction(slot1, slot2, constraint);
            }
        }.accept(constraint.getFirst(), constraint.getSecond(), constraint);
    }

    @Override
    public VecInt[] serialize(InequalityConstraint constraint) {
        return new VariableCombos<InequalityConstraint, VecInt[]>(emptyClauses) {

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
                VecInt[] result = new VecInt[Lattice.numTypes * 2];
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
            
            @Override
            protected VecInt[] constant_constant(ConstantSlot slot1, ConstantSlot slot2, InequalityConstraint constraint) {
                if (!ConstantUtils.checkConstant(slot1, slot2, constraint)) {
                    ErrorReporter.errorAbort("Confliction in inequality constraint: " + slot1.getValue()
                            + " is equal to " + slot2.getValue());
                }

                return defaultAction(slot1, slot2, constraint);
            }

        }.accept(constraint.getFirst(), constraint.getSecond(), constraint);
    }


    @Override
    public VecInt[] serialize(ComparableConstraint comparableConstraint) {
        ComparableConstraint constraint = comparableConstraint;
        return new VariableCombos<ComparableConstraint, VecInt[]>(emptyClauses) {
            @Override
            protected VecInt[] variable_variable(VariableSlot slot1, VariableSlot slot2, ComparableConstraint constraint) {
                // a <=> !b which is the same as (!a v !b) & (b v a)
                List<VecInt> list = new ArrayList<VecInt>();
                for (AnnotationMirror type : Lattice.allTypes) {
                    if (Lattice.incomparableType.keySet().contains(type)) {
                        for (AnnotationMirror notComparable : Lattice.incomparableType.get(type)) {
                            list.add(VectorUtils.asVec(
                                    -MathUtils.mapIdToMatrixEntry(slot1.getId(), type),
                                    -MathUtils.mapIdToMatrixEntry(slot2.getId(), notComparable),
                                    MathUtils.mapIdToMatrixEntry(slot2.getId(), notComparable),
                                    MathUtils.mapIdToMatrixEntry(slot1.getId(), type)));
                        }
                    }
                }
                VecInt[] result = list.toArray(new VecInt[list.size()]);
                return result;
            }
            
            @Override
            protected VecInt[] constant_constant(ConstantSlot slot1, ConstantSlot slot2, ComparableConstraint constraint) {
                if (!ConstantUtils.checkConstant(slot1, slot2, constraint)) {
                    ErrorReporter.errorAbort("Confliction in comparable constraint: " + slot1.getValue()
                            + " is not comparable to " + slot2.getValue());
                }

                return defaultAction(slot1, slot2, constraint);
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

    // TODO: we should consider the situation that the type annotations with
    // different weights.
    @Override
    public VecInt[] serialize(PreferenceConstraint preferenceConstraint) {
        VariableSlot vs = preferenceConstraint.getVariable();
        ConstantSlot cs = preferenceConstraint.getGoal();
        return VectorUtils.asVecArray(MathUtils.mapIdToMatrixEntry(vs.getId(), cs.getValue()));
    }

    protected static final VecInt[] emptyClauses = new VecInt[0];

}

