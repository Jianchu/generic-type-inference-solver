package logiqlbackend;

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

public class LogiqlSerializer implements Serializer<String, String> {

    @Override
    public String serialize(SubtypeConstraint constraint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String serialize(EqualityConstraint constraint) {
        // TODO Auto-generated method stub
        return null;
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

}
