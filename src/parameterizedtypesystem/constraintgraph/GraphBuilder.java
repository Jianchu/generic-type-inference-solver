package parameterizedtypesystem.constraintgraph;

import java.util.Collection;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;

public class GraphBuilder {

    private final Collection<Slot> slots;
    private final Collection<Constraint> constraints;
    private ConstraintGraph graph;

    public GraphBuilder(Collection<Slot> slots, Collection<Constraint> constraints) {
        this.slots = slots;
        this.constraints = constraints;
        this.graph = new ConstraintGraph();
    }

    public void buildGraph() {
        for (Constraint constraint : constraints) {

        }
    }
}
