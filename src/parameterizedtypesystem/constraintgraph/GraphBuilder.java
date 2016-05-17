package parameterizedtypesystem.constraintgraph;

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
            ArrayList<Slot> slots = new ArrayList<Slot>();
            slots.addAll(constraint.getSlots());
            addEdges(slots, constraint);
        }
        addConstant();
        calculateIndependentPath();
        return getGraph();
        // printEdges();
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
            for (Edge edge : current.getEdges()) {
                independentConstraints.add(edge.getConstraint());
                Vertex next = current.equals(edge.getVertex1()) ? edge.getVertex2() : edge.getVertex1();
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
            for (Vertex vertex : this.graph.getVerticies()) {
                if (vertex1.equals(vertex)) {
                    vertex1 = vertex;
                } else if (vertex2.equals(vertex)) {
                    vertex2 = vertex;
                }
            }
            Edge edge = new Edge(vertex1, vertex2, constraint);
            this.graph.addEdge(edge);
            addEdges(slots, constraint);
        }
    }
    
    private ConstraintGraph getGraph() {
        return this.graph;
    }
    
    private void printEdges() {
        for (Map.Entry<Vertex, Set<Constraint>> entry : this.graph.getIndependentPath().entrySet()) {
            System.out.println(entry.getKey().getSlot());
            System.out.println(entry.getValue());
        }

        for (Edge edge : graph.getEdges()) {
            System.out.println(edge.getVertex1().getSlot());
            System.out.println(edge.getVertex2().getSlot());
            System.out.println(edge.getConstraint());
        }

        for (Vertex vertex : graph.getVerticies()) {
            System.out.println(vertex.getSlot());
            System.out.println(vertex.getEdges());
        }
    }

}
