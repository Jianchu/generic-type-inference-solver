package generalconstraintsolver.dataflowsolver;

import generalconstraintsolver.ImpliesLogic;
import generalconstraintsolver.LatticeGenerator;

import java.util.Collection;
import java.util.List;

import checkers.inference.model.Constraint;

public class DataflowImpliesLogic {

    private DataflowGeneralSerializer serializer;
    LatticeGenerator lattice;
    private List<ImpliesLogic> logics;

    public DataflowImpliesLogic(LatticeGenerator lattice) {
        this.lattice = lattice;
    }

    public void configure(Collection<Constraint> constraints,
            DataflowGeneralSerializer serializer) {
        this.serializer = serializer;
        this.logics = convertToImpliesLogic(constraints);

    }

    private List<ImpliesLogic> convertToImpliesLogic(
            Collection<Constraint> constraints) {
        return serializer.convertAll(constraints);
    }

    public List<ImpliesLogic> getLogics() {
        return this.logics;
    }
}
