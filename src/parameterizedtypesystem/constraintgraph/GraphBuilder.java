package parameterizedtypesystem.constraintgraph;

import java.util.ArrayList;
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
            ArrayList<Slot> slots = new ArrayList<Slot>();
            slots.addAll(constraint.getSlots());
            addEdges(slots, constraint);
        }
    }
    
    private void addEdges(ArrayList<Slot> slots, Constraint constraint) {
        Slot first = slots.remove(0);
        for (int i = 0; i < slots.size(); i++) {
            Slot next = slots.get(i);
            Vertex vertex1 = new Vertex(first);
            Vertex vertex2 = new Vertex(next);
            Edge edge = new Edge(vertex1, vertex2, constraint);
            this.graph.addEdge(edge);
            addEdges(slots, constraint);
        }
    }
    
    
}
