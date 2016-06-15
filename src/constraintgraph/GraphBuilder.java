package constraintgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.model.SubtypeConstraint;

/**
 * ConstraintGraph Builder
 * 
 * @author jianchu
 *
 */
public class GraphBuilder {

    private final Collection<Slot> slots;
    private final Collection<Constraint> constraints;
    private final ConstraintGraph graph;

    public GraphBuilder(Collection<Slot> slots, Collection<Constraint> constraints) {
        this.slots = slots;
        this.constraints = constraints;
        this.graph = new ConstraintGraph();
    }

    public ConstraintGraph buildGraph() {
        for (Constraint constraint : constraints) {
            if (constraint instanceof SubtypeConstraint) {
                addSubtypeEdge((SubtypeConstraint) constraint);
            } else {
                ArrayList<Slot> slots = new ArrayList<Slot>();
                slots.addAll(constraint.getSlots());
                addEdges(slots, constraint);
            }
        }
        addConstant();
        calculateIndependentPath();
        // printEdges();
        return getGraph();
    }
    
    private void calculateIndependentPath() {
        for (Vertex vertex : this.graph.getConstantVerticies()) {
            Set<Constraint> independentConstraints = BFSSearch(vertex);
            this.graph.addIndependentPath(vertex, independentConstraints);
        }
    }

    private Set<Constraint> BFSSearch(Vertex vertex) {
        Set<Constraint> independentConstraints = new HashSet<Constraint>();
        Queue<Vertex> queue = new LinkedList<Vertex>();
        queue.add(vertex);
        Set<Vertex> visited = new HashSet<Vertex>();
        while (!queue.isEmpty()) {
            Vertex current = queue.remove();
            visited.add(current);
            for (Edge edge : current.getOutgoingEdge()) {
                independentConstraints.add(edge.getConstraint());
                Vertex next = current.equals(edge.getFromVertex()) ? edge.getToVertex() : edge.getFromVertex();
                if (!visited.contains(next)) {
                    queue.add(next);
                }
            }
        }
        return independentConstraints;
    }

    private void addConstant() {
        for (Vertex vertex : graph.getVerticies()) {
            if (vertex.isConstant()) {
                this.graph.addConstant(vertex);
            }
        }
    }

    private void addEdges(ArrayList<Slot> slots, Constraint constraint) {
        Slot first = slots.remove(0);
        for (int i = 0; i < slots.size(); i++) {
            Slot next = slots.get(i);
            if (first instanceof ConstantSlot && next instanceof ConstantSlot) {
                continue;
            }
            Vertex vertex1 = new Vertex(first);
            Vertex vertex2 = new Vertex(next);
            createDoubleEdge(vertex1, vertex2, constraint);
        }
    }
    
    private void addSubtypeEdge(SubtypeConstraint subtypeConstraint) {
        Slot subtype = subtypeConstraint.getSubtype();
        Slot supertype = subtypeConstraint.getSupertype();
        if (subtype instanceof ConstantSlot && supertype instanceof ConstantSlot) {
            return;
        }
        Vertex subtypeVertex = new Vertex(subtype);
        Vertex supertypeVertex = new Vertex(supertype);
        createSingleEdge(subtypeVertex, supertypeVertex, subtypeConstraint);
    }

    private void createSingleEdge(Vertex from, Vertex to, Constraint constraint) {

        for (Vertex vertex : this.graph.getVerticies()) {
            if (from.equals(vertex)) {
                from = vertex;
            } else if (to.equals(vertex)) {
                to = vertex;
            }
        }
        Edge edge = new Edge(from, to, constraint);
        this.graph.addEdge(edge);
    }

    private void createDoubleEdge(Vertex vertex1, Vertex vertex2, Constraint constraint) {

        for (Vertex vertex : this.graph.getVerticies()) {
            if (vertex1.equals(vertex)) {
                vertex1 = vertex;
            } else if (vertex2.equals(vertex)) {
                vertex2 = vertex;
            }
        }
        Edge edge1 = new Edge(vertex1, vertex2, constraint);
        Edge edge2 = new Edge(vertex2, vertex1, constraint);
        this.graph.addEdge(edge1);
        this.graph.addEdge(edge2);
    }

    public ConstraintGraph getGraph() {
        return this.graph;
    }

    private void printEdges() {
        for (Map.Entry<Vertex, Set<Constraint>> entry : this.graph.getIndependentPath().entrySet()) {
            System.out.println(entry.getKey().getSlot());
            System.out.println(entry.getValue());
        }

        for (Edge edge : graph.getEdges()) {
            System.out.println(edge.getFromVertex().getSlot());
            System.out.println(edge.getToVertex().getSlot());
            System.out.println(edge.getConstraint());
        }

        for (Vertex vertex : graph.getVerticies()) {
            System.out.println(vertex.getSlot());
            System.out.println("incoming edge: " + vertex.getIncomingEdges());
            System.out.println("outgoing edge: " + vertex.getOutgoingEdge());
        }
    }

}
