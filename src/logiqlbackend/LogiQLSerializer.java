package logiqlbackend;

import util.NameUtils;
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

public class LogiQLSerializer implements Serializer<String, String> {

    private final Lattice lattice;

    public LogiQLSerializer(Lattice lattice) {
        this.lattice = lattice;
    }

    @Override
    public String serialize(SubtypeConstraint constraint) {

        return new VariableCombos<SubtypeConstraint>() {
            
            @Override
            protected String constant_variable(ConstantSlot subtype, VariableSlot supertype, SubtypeConstraint constraint) {
                String subtypeName = NameUtils.getSimpleName(subtype.getValue());
                int supertypeId = supertype.getId();
                String logiQLData = "+subtypeConstraintLeftConstant(c, v), +constant(c), +hasconstantName[c] = \""
                        + subtypeName + "\", +variable(v), +hasvariableName[v] = " + supertypeId + ".\n";
                return logiQLData;
            }

            @Override
            protected String variable_constant(VariableSlot subtype, ConstantSlot supertype, SubtypeConstraint constraint) {
                String supertypeName = NameUtils.getSimpleName(supertype.getValue());
                int subtypeId = subtype.getId();
                String logiQLData = "+subtypeConstraintRightConstant(v, c), +variable(v), +hasvariableName[v] = "
                        + subtypeId
                        + ", +constant(c), +hasconstantName[c] = \""
                        + supertypeName
                        + "\" .\n";
                return logiQLData;
            }

            @Override
            protected String variable_variable(VariableSlot subtype,  VariableSlot supertype, SubtypeConstraint constraint) {
                String logiQLData = "+subtypeConstraint(v1, v2), +variable(v1), +hasvariableName[v1] = "
                        + subtype.getId() + ", +variable(v2), +hasvariableName[v2] = "
                        + supertype.getId() + ".\n";
                return logiQLData;
            }
            
            @Override
            protected String constant_constant(ConstantSlot slot1, ConstantSlot slot2, SubtypeConstraint constraint) {
                // if (!ConstantUtils.checkConstant(slot1, slot2, constraint)) {
                // ErrorReporter.errorAbort("Confliction in subtype constraint: "
                // + slot1.getValue()
                // + " is not subtype of " + slot2.getValue());
                // }

                return defaultAction(slot1, slot2, constraint);
            }

        }.accept(constraint.getSubtype(), constraint.getSupertype(), constraint);
    }

    @Override
    public String serialize(EqualityConstraint constraint) {
        return new VariableCombos<EqualityConstraint>() {

            @Override
            protected String constant_variable(ConstantSlot slot1, VariableSlot slot2, EqualityConstraint constraint) {
                String constantName = NameUtils.getSimpleName(slot1.getValue());
                int variableId = slot2.getId();
                String logiQLData = "+equalityConstraintContainsConstant(c, v), +constant(c), +hasconstantName[c] = \""
                        + constantName + "\", +variable(v), +hasvariableName[v] = " + variableId + ".\n";
                return logiQLData;
            }

            @Override
            protected String variable_constant(VariableSlot slot1, ConstantSlot slot2,
                    EqualityConstraint constraint) {
                return constant_variable(slot2, slot1, constraint);
            }

            @Override
            protected String variable_variable(VariableSlot slot1, VariableSlot slot2,
                    EqualityConstraint constraint) {
                String logiQLData = "+equalityConstraint(v1, v2), +variable(v1), +hasvariableName[v1] = "
                        + slot1.getId() + ", +variable(v2), +hasvariableName[v2] = " + slot2.getId()
                        + ".\n";
                return logiQLData;
            }
            
            @Override
            protected String constant_constant(ConstantSlot slot1, ConstantSlot slot2, EqualityConstraint constraint) {
                // if (!ConstantUtils.checkConstant(slot1, slot2, constraint)) {
                // ErrorReporter.errorAbort("Confliction in equality constraint: "
                // + slot1.getValue()
                // + " is not equal to " + slot2.getValue());
                // }

                return defaultAction(slot1, slot2, constraint);
            }

        }.accept(constraint.getFirst(), constraint.getSecond(), constraint);
    }

    @Override
    public String serialize(ExistentialConstraint constraint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String serialize(InequalityConstraint constraint) {
        return new VariableCombos<InequalityConstraint>() {

            @Override
            protected String constant_variable(ConstantSlot slot1, VariableSlot slot2,
                    InequalityConstraint constraint) {
                String constantName = NameUtils.getSimpleName(slot1.getValue());
                int variableId = slot2.getId();
                String logiQLData = "+inequalityConstraintContainsConstant(c, v), +constant(c), +hasconstantName[c] = \""
                        + constantName + "\", +variable(v), +hasvariableName[v] = " + variableId + ".\n";
                return logiQLData;
            }

            @Override
            protected String variable_constant(VariableSlot slot1, ConstantSlot slot2,
                    InequalityConstraint constraint) {
                return constant_variable(slot2, slot1, constraint);
            }

            @Override
            protected String variable_variable(VariableSlot slot1, VariableSlot slot2,
                    InequalityConstraint constraint) {
                String logiQLData = "+inequalityConstraint(v1, v2), +variable(v1), +hasvariableName[v1] = "
                        + slot1.getId()
                        + ", +variable(v2), +hasvariableName[v2] = "
                        + slot2.getId()
                        + ".\n";
                return logiQLData;
            }
            
            @Override
            protected String constant_constant(ConstantSlot slot1, ConstantSlot slot2, InequalityConstraint constraint) {
                // if (!ConstantUtils.checkConstant(slot1, slot2, constraint)) {
                // ErrorReporter.errorAbort("Confliction in inequality constraint: "
                // + slot1.getValue()
                // + " is equal to " + slot2.getValue());
                // }

                return defaultAction(slot1, slot2, constraint);
            }

        }.accept(constraint.getFirst(), constraint.getSecond(), constraint);
    }

    @Override
    public String serialize(VariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String serialize(ConstantSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String serialize(ExistentialVariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String serialize(RefinementVariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String serialize(CombVariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String serialize(ComparableConstraint comparableConstraint) {
        ComparableConstraint constraint = comparableConstraint;
        return new VariableCombos<ComparableConstraint>() {

            @Override
            protected String constant_variable(ConstantSlot slot1, VariableSlot slot2,
                    ComparableConstraint constraint) {
                String constantName = NameUtils.getSimpleName(slot1.getValue());
                int variableId = slot2.getId();
                String logiQLData = "+equalityConstraintContainsConstant(c, v), +constant(c), +hasconstantName[c] = \""
                        + constantName + "\", +variable(v), +hasvariableName[v] = " + variableId + ".\n";
                return logiQLData;
            }

            @Override
            protected String variable_constant(VariableSlot slot1, ConstantSlot slot2,
                    ComparableConstraint constraint) {
                return constant_variable(slot2, slot1, constraint);
            }

            @Override
            protected String variable_variable(VariableSlot slot1, VariableSlot slot2, ComparableConstraint constraint) {
                String logiQLData = "+comparableConstraint(v1, v2), +variable(v1), +hasvariableName[v1] = "
                        + slot1.getId()
                        + ", +variable(v2), +hasvariableName[v2] = "
                        + slot2.getId()
                        + ".\n";
                return logiQLData;
            }
            
            @Override
            protected String constant_constant(ConstantSlot slot1, ConstantSlot slot2, ComparableConstraint constraint) {
                // if (!ConstantUtils.checkConstant(slot1, slot2, constraint)) {
                // ErrorReporter.errorAbort("Confliction in comparable constraint: "
                // + slot1.getValue()
                // + " is not comparable to " + slot2.getValue());
                // }

                return defaultAction(slot1, slot2, constraint);
            }
            
        }.accept(constraint.getFirst(), constraint.getSecond(), constraint);
    }

    @Override
    public String serialize(CombineConstraint combineConstraint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String serialize(PreferenceConstraint preferenceConstraint) {
        // TODO Auto-generated method stub
        return null;
    }

    class VariableCombos<T extends Constraint> {

        protected String variable_variable(VariableSlot slot1, VariableSlot slot2, T constraint) {
            return defaultAction(slot1, slot2, constraint);
        }

        protected String constant_variable(ConstantSlot slot1, VariableSlot slot2, T constraint) {
            return defaultAction(slot1, slot2, constraint);
        }

        protected String variable_constant(VariableSlot slot1, ConstantSlot slot2, T constraint) {
            return defaultAction(slot1, slot2, constraint);
        }

        protected String constant_constant(ConstantSlot slot1, ConstantSlot slot2, T constraint) {
            return defaultAction(slot1, slot2, constraint);
        }

        public String defaultAction(Slot slot1, Slot slot2, T constraint) {
            return emptyString;
        }

        public String accept(Slot slot1, Slot slot2, T constraint) {

            final String result;

            if (slot1 instanceof ConstantSlot) {
                if (slot2 instanceof ConstantSlot) {
                    result = constant_constant((ConstantSlot) slot1, (ConstantSlot) slot2, constraint);
                } else {
                    result = constant_variable((ConstantSlot) slot1, (VariableSlot) slot2, constraint);
                }
            } else if (slot2 instanceof ConstantSlot) {
                result = variable_constant((VariableSlot) slot1, (ConstantSlot) slot2, constraint);
            } else {
                result = variable_variable((VariableSlot) slot1, (VariableSlot) slot2, constraint);
            }
            return result;
        }
    }

    public static final String emptyString = "";

}
