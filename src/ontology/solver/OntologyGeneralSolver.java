package ontology.solver;

import checkers.inference.InferenceSolution;
import generalconstraintsolver.GeneralConstrainsSolver;
import generalconstraintsolver.LatticeGenerator;

public abstract class OntologyGeneralSolver extends GeneralConstrainsSolver {
    @Override
    protected InferenceSolution solve() {
        LatticeGenerator lattice = new LatticeGenerator(qualHierarchy);
        OntologyGeneralSerializer serializer = new OntologyGeneralSerializer(slotManager, lattice);
        OntologyImpliesLogic logic = new OntologyImpliesLogic(lattice, constraints, serializer);
        OntologySolution solution = solveImpliesLogic(logic);
        return solution;
    }

    protected abstract OntologySolution solveImpliesLogic(OntologyImpliesLogic logic);
}
