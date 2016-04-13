package generalconstraintsolver.dataflowsolver;

import java.util.Collection;
import java.util.List;

import checkers.inference.model.Constraint;
import generalconstraintsolver.ImpliesLogic;
import generalconstraintsolver.LatticeGenerator;

public class DataflowImpliesLogic {

    private final DataflowGeneralSerializer serializer;
    private final LatticeGenerator lattice;
    private final List<ImpliesLogic> logics;

    public DataflowImpliesLogic(LatticeGenerator lattice, Collection<Constraint> constraints,
            DataflowGeneralSerializer serializer) {
        this.lattice = lattice;
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

    public LatticeGenerator getLattice() {
        return this.lattice;
    }

    public DataflowGeneralSerializer getSerializer() {
        return this.serializer;
    }
}
