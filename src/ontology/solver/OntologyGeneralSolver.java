package ontology.solver;

import java.util.Collection;
import java.util.List;

import checkers.inference.InferenceSolution;
import checkers.inference.model.Constraint;
import generalconstraintsolver.GeneralConstrainsSolver;
import generalconstraintsolver.ImpliesLogic;
import generalconstraintsolver.LatticeGenerator;

public abstract class OntologyGeneralSolver extends GeneralConstrainsSolver {
    @Override
    protected InferenceSolution solve() {
        LatticeGenerator lattice = new LatticeGenerator(qualHierarchy);
        OntologyGeneralSerializer serializer = new OntologyGeneralSerializer(slotManager, lattice);
        List<ImpliesLogic> logic = convertToImpliesLogic(constraints, serializer);
        OntologySolution solution = solveImpliesLogic(lattice, logic);
        return solution;
    }

    private List<ImpliesLogic> convertToImpliesLogic(Collection<Constraint> constraints,
            OntologyGeneralSerializer serializer) {
        return serializer.convertAll(constraints);
    }

    protected abstract OntologySolution solveImpliesLogic(LatticeGenerator lattice,
            List<ImpliesLogic> logic);
}
