package ontology.solver;

import java.util.Collection;
import java.util.List;

import checkers.inference.model.Constraint;
import generalconstraintsolver.ImpliesLogic;
import generalconstraintsolver.LatticeGenerator;

public class OntologyImpliesLogic {

    private final OntologyGeneralSerializer serializer;
    private final LatticeGenerator lattice;
    private final List<ImpliesLogic> logics;

    public OntologyImpliesLogic(LatticeGenerator lattice, Collection<Constraint> constraints,
            OntologyGeneralSerializer serializer) {
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

    public OntologyGeneralSerializer getSerializer() {
        return this.serializer;
    }
}
