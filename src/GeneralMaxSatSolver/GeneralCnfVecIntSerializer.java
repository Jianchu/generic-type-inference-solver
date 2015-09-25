package GeneralMaxSatSolver;

import java.util.LinkedList;
import java.util.List;

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
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.model.VariableSlot;

public class GeneralCnfVecIntSerializer implements Serializer {
    private final SlotManager slotManager;
    private final LatticeGenerator lattice;
    
    public GeneralCnfVecIntSerializer(SlotManager slotManager,LatticeGenerator lattice){
        this.slotManager = slotManager;
        this.lattice = lattice;
    }
    
    public List<VecInt> convertAll(Iterable<Constraint> constraints) {
        return convertAll(constraints, new LinkedList<VecInt>());
    }

    public List<VecInt> convertAll(Iterable<Constraint> constraints, List<VecInt> results) {
        for (Constraint constraint : constraints) {
            for (VecInt res : ((VecInt[]) constraint.serialize(this))) {
                if (res.size() != 0) {
                    System.out.println(res.toString());
                    results.add(res);
                }
            }
        }
        return results;
    }
    

    @Override
    public Object serialize(SubtypeConstraint constraint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(EqualityConstraint constraint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(ExistentialConstraint constraint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(InequalityConstraint constraint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(VariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(ConstantSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(ExistentialVariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(RefinementVariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(CombVariableSlot slot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(ComparableConstraint comparableConstraint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(CombineConstraint combineConstraint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object serialize(PreferenceConstraint preferenceConstraint) {
        // TODO Auto-generated method stub
        return null;
    }
    
    
}
