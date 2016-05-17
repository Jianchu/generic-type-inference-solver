package parameterizedtypesystem;

import parameterizedtypesystem.constraintgraph.GraphBuilder;
import checkers.inference.InferenceSolution;
import constraintsolver.ConstraintSolver;

public class ParameterizedTypeSolver extends ConstraintSolver {

    @Override
    protected InferenceSolution solve() {
        GraphBuilder graphBuilder = new GraphBuilder(this.realBackEnd.getSlots(), this.realBackEnd.getConstraints());
        graphBuilder.buildGraph();
        return realBackEnd.solve();
    }
}
