package ontology.solver;

import org.checkerframework.javacutil.AnnotationUtils;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.SlotManager;
import checkers.inference.model.CombVariableSlot;
import checkers.inference.model.CombineConstraint;
import checkers.inference.model.ComparableConstraint;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.EqualityConstraint;
import checkers.inference.model.ExistentialConstraint;
import checkers.inference.model.ExistentialVariableSlot;
import checkers.inference.model.PreferenceConstraint;
import checkers.inference.model.RefinementVariableSlot;
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.model.VariableSlot;
import generalconstraintsolver.GeneralEncodingSerializer;
import generalconstraintsolver.ImpliesLogic;
import generalconstraintsolver.LatticeGenerator;
import ontology.qual.OntologyTop;

// JLTODO: put more of the computations into helper functions to reduce duplication
public class OntologyGeneralSerializer extends GeneralEncodingSerializer {

    public OntologyGeneralSerializer(SlotManager slotManager,
            LatticeGenerator lattice) {
        super(slotManager, lattice);
    }

    protected boolean isTop(ConstantSlot constantSlot) {
        AnnotationMirror anno = constantSlot.getValue();
        return AnnotationUtils.areSameByClass(anno, OntologyTop.class);
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
    public ImpliesLogic[] serialize(EqualityConstraint constraint) {
        return new VariableCombos<EqualityConstraint>() {

            @Override
            protected ImpliesLogic[] constant_variable(ConstantSlot slot1, VariableSlot slot2, EqualityConstraint constraint) {
                if (isTop(slot1)) {
                    return asSingleImp(
                            lattice.modifierInt.get(lattice.top) + lattice.numModifiers * (slot2.getId() - 1));
                }
                //result[0] = asSingleImp(lattice.modifierInt.get(slot1.getValue())+ lattice.numModifiers * (slot2.getId()-1));
                return emptyClauses;
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
                            (lattice.modifierInt.get(modifiers) + lattice.numModifiers * (slot1.getId() - 1)),
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
        // TODO Auto-generated method stub
        return null;
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
}
