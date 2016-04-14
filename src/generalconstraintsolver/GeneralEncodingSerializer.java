package generalconstraintsolver;

import org.checkerframework.javacutil.AnnotationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.model.VariableSlot;

public class GeneralEncodingSerializer implements Serializer<ImpliesLogic[], ImpliesLogic[]> {
    protected final SlotManager slotManager;
    protected final LatticeGenerator lattice;
    // protected final Map<Integer, Integer> existentialToPotentialVar = new
    // HashMap<>();

    public GeneralEncodingSerializer(SlotManager slotManager,
            LatticeGenerator lattice) {
        this.slotManager = slotManager;
        this.lattice = lattice;
    }

    /* JLTODO: This map is always empty. What is it's purpose???
     * I commented out all uses.
    public Map<Integer, Integer> getExistentialToPotentialVar() {
        return existentialToPotentialVar;
    }*/

    public List<ImpliesLogic> convertAll(Iterable<Constraint> constraints) {
        return convertAll(constraints, new LinkedList<ImpliesLogic>());
    }

    public List<ImpliesLogic> convertAll(Iterable<Constraint> constraints,
            List<ImpliesLogic> results) {

        for (Constraint constraint : constraints) {
            for (ImpliesLogic res : constraint.serialize(this)) {
                if (res.size() != 0) {
//                    if (res.singleVariable == true){
//                        System.out.println("just: " + res.variable);
//                    }
//                    else{
//                        System.out.println("left: " + res.leftSide.toString()+ " ---> " + "right: " + res.rightSide.toString());
//                    }
                    results.add(res);
                }
            }
        }
        return results;
    }

    private ImpliesLogic[] hasNotToBeInSub(Collection<AnnotationMirror> collection, VariableSlot currentSlot,
            ConstantSlot currentConslot) {
        List<Integer> Listresult = new ArrayList<Integer>();
        for (AnnotationMirror subOrSup : collection) {
            if (!areSameType(subOrSup, currentConslot.getValue())) {
                // JLTODO: can you put this computation into a helper? It
                // appears multiple times in this file.
                Listresult.add(-(lattice.modifierInt.get(subOrSup) + lattice.numModifiers * (currentSlot.getId() - 1)));
            }
        }

        ImpliesLogic[] result = new ImpliesLogic[Listresult.size()];
        if (Listresult.size() > 0) {
            Iterator<Integer> iterator = Listresult.iterator();
            for (int i = 0; i < result.length; i++) {
                result[i] = asSingleImp(iterator.next().intValue())[0];
            }
            return result;
        }
        return emptyClauses;
    }

    
    @Override
    public ImpliesLogic[] serialize(SubtypeConstraint constraint) {
        return new VariableCombos<SubtypeConstraint>() {

            @Override
            protected ImpliesLogic[] constant_variable(ConstantSlot subtype,
                    VariableSlot supertype, SubtypeConstraint constraint) {
                if (areSameType(subtype.getValue(),lattice.top)) {
                    return asSingleImp(lattice.modifierInt.get(lattice.top)
                            + lattice.numModifiers * (supertype.getId() - 1));
                }
                //System.out.println(subtype.toString());
                Collection<AnnotationMirror> subOfConstant = lattice.subType.get(subtype.getValue());
                Collection<AnnotationMirror> unCompOfConstant = lattice.notComparableType.get(subtype.getValue());
                subOfConstant.addAll(unCompOfConstant);
                ImpliesLogic[] result = hasNotToBeInSub(subOfConstant, supertype, subtype);
                if (result.length > 0) {
                    return result;
                }
                return emptyClauses;
            }

            @Override
            protected ImpliesLogic[] variable_constant(VariableSlot subtype,
                    ConstantSlot supertype, SubtypeConstraint constraint) {
                if (areSameType(supertype.getValue(),lattice.bottom)) {
                    return asSingleImp(lattice.modifierInt.get(lattice.bottom)
                            + lattice.numModifiers * (subtype.getId() - 1));
                }
                Collection<AnnotationMirror> subOfConstant = lattice.superType.get(supertype.getValue());
                Collection<AnnotationMirror> unCompOfConstant = lattice.notComparableType.get(supertype.getValue());
                subOfConstant.addAll(unCompOfConstant);
                ImpliesLogic[] result = hasNotToBeInSub(subOfConstant,subtype, supertype);
                if (result.length > 0) {
                    return result;
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

            // JLTODO: this is quite a large anonymous inner class. Extract it
            // to the top level and document.
        }.accept(constraint.getSubtype(), constraint.getSupertype(), constraint);
    }

    @Override
    public ImpliesLogic[] serialize(EqualityConstraint constraint) {
        return new VariableCombos<EqualityConstraint>() {

            @Override
            protected ImpliesLogic[] constant_variable(ConstantSlot slot1, VariableSlot slot2, EqualityConstraint constraint) {
                //result[0] = asSingleImp(lattice.modifierInt.get(slot1.getValue())+ lattice.numModifiers * (slot2.getId()-1));
                return asSingleImp(
                        lattice.modifierInt.get(slot1.getValue()) + lattice.numModifiers * (slot2.getId() - 1));
            }

            @Override
            protected ImpliesLogic[] variable_constant(VariableSlot slot1, ConstantSlot slot2, EqualityConstraint constraint) {
                return constant_variable(slot2, slot1, constraint);
            }

            @Override
            protected ImpliesLogic[] variable_variable(VariableSlot slot1, VariableSlot slot2, EqualityConstraint constraint) {
                // a <=> b which is the same as (!a v b) & (!b v a)
                ImpliesLogic[] result = new ImpliesLogic[lattice.numModifiers];
                int i = 0;
                for (AnnotationMirror modifiers : lattice.allTypes) {
                    result[i] = asDoubleImp(
                            lattice.modifierInt.get(modifiers) + lattice.numModifiers * (slot1.getId() - 1),
                            lattice.modifierInt.get(modifiers) + lattice.numModifiers * (slot2.getId() - 1));
                    i++;
                }
                return result;
            }
        }.accept(constraint.getFirst(), constraint.getSecond(), constraint);
    }

    @Override
    public ImpliesLogic[] serialize(ExistentialConstraint constraint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ImpliesLogic[] serialize(InequalityConstraint constraint) {
        return new VariableCombos<InequalityConstraint>() {

            @Override
            protected ImpliesLogic[] constant_variable(ConstantSlot slot1, VariableSlot slot2, InequalityConstraint constraint) {
                return asSingleImp(
                        -(lattice.modifierInt.get(slot1.getValue()) + lattice.numModifiers * (slot2.getId() - 1)));

            }

            @Override
            protected ImpliesLogic[] variable_constant(VariableSlot slot1, ConstantSlot slot2, InequalityConstraint constraint) {
                return constant_variable(slot2, slot1, constraint);
            }

            @Override
            protected ImpliesLogic[] variable_variable(VariableSlot slot1, VariableSlot slot2, InequalityConstraint constraint) {
                // a <=> !b which is the same as (!a v !b) & (b v a)
                ImpliesLogic[] result = new ImpliesLogic[lattice.numModifiers];
                int i = 0;
                for (AnnotationMirror modifiers : lattice.allTypes) {
                    result[i] = asDoubleImp(
                            lattice.modifierInt.get(modifiers) + lattice.numModifiers * (slot1.getId() - 1),
                            -(lattice.modifierInt.get(modifiers) + lattice.numModifiers * (slot2.getId() - 1)));
                    i++;
                }
                return result;
            }

        }.accept(constraint.getFirst(), constraint.getSecond(), constraint);
    }

    @Override
    public ImpliesLogic[] serialize(VariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ImpliesLogic[] serialize(ConstantSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ImpliesLogic[] serialize(ExistentialVariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ImpliesLogic[] serialize(RefinementVariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ImpliesLogic[] serialize(CombVariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ImpliesLogic[] serialize(ComparableConstraint comparableConstraint) {
        ComparableConstraint constraint = comparableConstraint;
        return new VariableCombos<ComparableConstraint>() {
            @Override
            protected ImpliesLogic[] variable_variable(VariableSlot slot1, VariableSlot slot2, ComparableConstraint constraint) {
                // a <=> !b which is the same as (!a v !b) & (b v a)
                List<ImpliesLogic> list = new ArrayList<ImpliesLogic>();
                for (AnnotationMirror modifiers : lattice.allTypes) {
                    if (!lattice.notComparableType.get(modifiers).isEmpty()) {
                        for (AnnotationMirror notComparable : lattice.notComparableType.get(modifiers)) {
                            list.add(asDoubleImp(
                                    lattice.modifierInt.get(modifiers) + lattice.numModifiers * (slot1.getId() - 1),
                                    -(lattice.modifierInt.get(notComparable)
                                            + lattice.numModifiers * (slot2.getId() - 1))));
                        }
                    }
                }
                ImpliesLogic[] result = list.toArray(new ImpliesLogic[list.size()]);
                return result;
            }
        }.accept(constraint.getFirst(), constraint.getSecond(), constraint);
    }

    @Override
    public ImpliesLogic[] serialize(CombineConstraint combineConstraint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ImpliesLogic[] serialize(PreferenceConstraint preferenceConstraint) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean areSameType(AnnotationMirror m1, AnnotationMirror m2) {
        //System.out.println(AnnotationUtils.areSameIgnoringValues(m1, m2) +"  "+ m1.toString() + "  " + m2.toString());
        return AnnotationUtils.areSameIgnoringValues(m1, m2);
    }

    public ImpliesLogic[] asSingleImp(int variable) {
            return new ImpliesLogic[] {new ImpliesLogic(variable,true)};
    }

    public ImpliesLogic asDoubleImp(int leftVariable, int RightVariable) {
        ImpliesLogic imply =  new ImpliesLogic(leftVariable,RightVariable);
        return imply;
    }

    public ImpliesLogic asMutipleImp(int[] leftVariable, int[] RightVariable,
            String insideLogic) {
        return new ImpliesLogic(leftVariable,RightVariable, insideLogic);
    }

//    VecInt[] asVecArray(int... vars) {
//        return new VecInt[] { new VecInt(vars) };
//    }

    public class VariableCombos<T extends Constraint> {

        protected ImpliesLogic[] variable_variable(VariableSlot slot1,
                VariableSlot slot2, T constraint) {
            return defaultAction(slot1, slot2, constraint);
        }

        protected ImpliesLogic[] constant_variable(ConstantSlot slot1,
                VariableSlot slot2, T constraint) {
            return defaultAction(slot1, slot2, constraint);
        }

        protected ImpliesLogic[] variable_constant(VariableSlot slot1,
                ConstantSlot slot2, T constraint) {
            return defaultAction(slot1, slot2, constraint);
        }

        protected ImpliesLogic[] constant_constant(ConstantSlot slot1,
                ConstantSlot slot2, T constraint) {
            return defaultAction(slot1, slot2, constraint);
        }

        public ImpliesLogic[] defaultAction(Slot slot1, Slot slot2, T constraint) {
            return emptyClauses;
        }

        public ImpliesLogic[] accept(Slot slot1, Slot slot2, T constraint) {

            final ImpliesLogic[] result;
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

    public static final ImpliesLogic[] emptyClauses = new ImpliesLogic[0];
}
