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

public class LogiQLSerializer implements Serializer<String, String> {

    @Override
    public String serialize(SubtypeConstraint constraint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String serialize(EqualityConstraint constraint) {
        return new VariableCombos<EqualityConstraint>() {

            @Override
            protected String constant_variable(ConstantSlot slot1, VariableSlot slot2, EqualityConstraint constraint) {
                String constantName = NameUtils.getSimpleName(slot1.getValue());
                int variableId = slot2.getId();
                String encoding = "+equalityConstraintContainsConstant(c, v), +constant(c), +hasconstantName[c] = \""
                        + constantName + "\", +variable(v), +hasvariableName[v] = " + variableId + ".\n";
                return encoding;
            }

            @Override
            protected String variable_constant(VariableSlot slot1, ConstantSlot slot2,
                    EqualityConstraint constraint) {
                return constant_variable(slot2, slot1, constraint);
            }

            @Override
            protected String variable_variable(VariableSlot slot1, VariableSlot slot2,
                    EqualityConstraint constraint) {
                String encoding = "+equalityConstraint(v1, v2), +variable(v1), +hasvariableName[v1] = "
                        + slot1.getId() + ", +variable(v2), +hasvariableName[v2] = " + slot2.getId()
                        + ".";
                return encoding;
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
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
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
