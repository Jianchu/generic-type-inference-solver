package constraintsolver;

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

public class ConstraintSerializer<S, T> implements Serializer<S, T> {

    public Serializer<S, T> realSerializer;

    public ConstraintSerializer(String backEndType) {
        Class<?> serializerClass;
        try {
            serializerClass = Class.forName(backEndType + "Serializer");
            realSerializer = (Serializer<S, T>) serializerClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public T serialize(SubtypeConstraint constraint) {
        return realSerializer.serialize(constraint);
    }

    @Override
    public T serialize(EqualityConstraint constraint) {
        return realSerializer.serialize(constraint);
    }

    @Override
    public T serialize(ExistentialConstraint constraint) {
        return realSerializer.serialize(constraint);
    }

    @Override
    public T serialize(InequalityConstraint constraint) {
        return realSerializer.serialize(constraint);
    }

    @Override
    public S serialize(VariableSlot slot) {
        return realSerializer.serialize(slot);
    }

    @Override
    public S serialize(ConstantSlot slot) {
        return realSerializer.serialize(slot);
    }

    @Override
    public S serialize(ExistentialVariableSlot slot) {
        return realSerializer.serialize(slot);
    }

    @Override
    public S serialize(RefinementVariableSlot slot) {
        return realSerializer.serialize(slot);
    }

    @Override
    public S serialize(CombVariableSlot slot) {
        return realSerializer.serialize(slot);
    }

    @Override
    public T serialize(ComparableConstraint comparableConstraint) {
        return realSerializer.serialize(comparableConstraint);
    }

    @Override
    public T serialize(CombineConstraint combineConstraint) {
        return realSerializer.serialize(combineConstraint);
    }

    @Override
    public T serialize(PreferenceConstraint preferenceConstraint) {
        return realSerializer.serialize(preferenceConstraint);
    }

}
