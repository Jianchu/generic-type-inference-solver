package constraintgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.ExistentialConstraint;
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
    private SubtypeDirection subtypeDirection = SubtypeDirection.UNDIRECTED;
    private Map<Vertex, Set<Vertex>> vertexCache = new HashMap<>();
    private Collection<Constraint> missingConstraints = new HashSet<>();
    
    public GraphBuilder(Collection<Slot> slots, Collection<Constraint> constraints) {
        this.slots = slots;
        this.constraints = constraints;
        this.graph = new ConstraintGraph();
    }

    public GraphBuilder(Collection<Slot> slots, Collection<Constraint> constraints,
            SubtypeDirection subtypeDirection) {
        this(slots, constraints);
        this.subtypeDirection = subtypeDirection;
    }

    public ConstraintGraph buildGraph() {
        for (Constraint constraint : constraints) {
            if (constraint instanceof SubtypeConstraint) {
                addSubtypeEdge((SubtypeConstraint) constraint);
            } else if (!(constraint instanceof ExistentialConstraint)) {
                ArrayList<Slot> slots = new ArrayList<Slot>();
                slots.addAll(constraint.getSlots());
                addEdges(slots, constraint);
            }
        }

        addConstant();
        calculateIndependentPath();
        calculateConstantPath();
        // System.out.println(this.missingConstraint);
        // printEdges();
        // printGraph();
        return getGraph();
    }
    
    private void calculateIndependentPath() {
        Set<Vertex> visited = new HashSet<Vertex>();
        for (Vertex vertex : this.graph.getVerticies()) {
            if (!visited.contains(vertex)) {
                Set<Constraint> independentPath = new HashSet<Constraint>();
                Queue<Vertex> queue = new LinkedList<Vertex>();
                queue.add(vertex);
                while (!queue.isEmpty()) {
                    Vertex current = queue.remove();
                    visited.add(current);
                    for (Edge edge : current.getEdges()) {
                        independentPath.add(edge.getConstraint());
                        Vertex next = edge.getFromVertex().equals(current) ? edge.getToVertex() : edge
                                .getFromVertex();
                        if (!visited.contains(next)) {
                            queue.add(next);
                        }
                    }
                }
                this.graph.addIndependentPath(independentPath);
            }
        }
    }

    private void calculateConstantPath() {
        Collection<Constraint> alias = new HashSet<Constraint>(this.constraints);
        for (Vertex vertex : this.graph.getConstantVerticies()) {
            Set<Constraint> constantPathConstraints = BFSSearch(vertex);
            alias.removeAll(constantPathConstraints);
            this.graph.addConstantPath(vertex, constantPathConstraints);
        }
        this.graph.SetMissingConstraints(alias);
        missingConstraints = alias;
        // addMissingVertex(alias);
    }

    private void addMissingVertex(Collection<Constraint> alias) {
        for (Constraint constraint : alias) {
            Edge edge = this.graph.findEdge(constraint);
            if (edge != null) {
                Vertex from = edge.getFromVertex();
                Vertex to = edge.getToVertex();
                for (Map.Entry<Vertex, Set<Vertex>> entry : this.vertexCache.entrySet()) {
                    Set<Vertex> vertexes = entry.getValue();
                    if (vertexes.contains(from) || vertexes.contains(to)) {
                        this.graph.addEdgeToConstantPath(entry.getKey(), constraint);
                    }
                }
            }
        }
    }

    private Set<Constraint> BFSSearch(Vertex vertex) {
        Set<Constraint> constantPathConstraints = new HashSet<Constraint>();
        Queue<Vertex> queue = new LinkedList<Vertex>();
        queue.add(vertex);
        Set<Vertex> visited = new HashSet<Vertex>();
        while (!queue.isEmpty()) {
            Vertex current = queue.remove();
            visited.add(current);
            for (Edge edge : current.getEdges()) {
                if (edge instanceof SubtypeEdge) {
                    if (this.subtypeDirection.equals(SubtypeDirection.FROMSUBTYPE)
                            && current.equals(edge.to)) {
                        continue;
                    } else if (this.subtypeDirection.equals(SubtypeDirection.FROMSUPERTYPE)
                            && current.equals(edge.from)) {
                        continue;
                    }
                }
                constantPathConstraints.add(edge.getConstraint());
                Vertex next = edge.getToVertex();
                Set<Vertex> cacheSet;
                if (this.vertexCache.keySet().contains(vertex)) {
                    cacheSet = vertexCache.get(vertex);
                } else {
                    cacheSet = new HashSet<Vertex>();
                }
                cacheSet.add(current);
                cacheSet.add(next);
                this.vertexCache.put(vertex, cacheSet);
                if (!visited.contains(next)) {
                    queue.add(next);
                }
            }
        }
        return constantPathConstraints;
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
            this.graph.createEdge(first, next, constraint);
            addEdges(slots, constraint);
        }
    }
    
    /**
     * The order of subtype and supertype matters, first one has to be subtype,
     * second one has to be supertype.
     * 
     * @param subtypeConstraint
     */
    private void addSubtypeEdge(SubtypeConstraint subtypeConstraint) {
        Slot subtype = subtypeConstraint.getSubtype();
        Slot supertype = subtypeConstraint.getSupertype();
        if (subtype instanceof ConstantSlot && supertype instanceof ConstantSlot) {
            return;
        }
        this.graph.createEdge(subtype, supertype, subtypeConstraint);
    }

    public ConstraintGraph getGraph() {
        return this.graph;
    }

    private void printEdges() {
        System.out.println("new graph!");
        for (Map.Entry<Vertex, Set<Constraint>> entry : this.graph.getConstantPath().entrySet()) {
            System.out.println(entry.getKey().getSlot());
            for (Constraint constraint : entry.getValue()) {
                if (constraint instanceof ExistentialConstraint) {
                    continue;
                }
                System.out.println(constraint);
            }
            System.out.println("**************");
        }

        // for (Edge edge : graph.getEdges()) {
        // System.out.println(edge.getFromVertex().getSlot());
        // System.out.println(edge.getToVertex().getSlot());
        // System.out.println(edge.getConstraint());
        // }
        //
        // for (Vertex vertex : graph.getVerticies()) {
        // System.out.println(vertex.getSlot());
        // System.out.println("incoming edge: " + vertex.getIncomingEdges());
        // System.out.println("outgoing edge: " + vertex.getOutgoingEdge());
        // }
    }

    private void printGraph() {
        for (Edge edge : this.graph.getEdges()) {
            System.out.println(edge);
        }

        for (Vertex vertex : this.graph.getVerticies()) {
            System.out.println(vertex.getId());
        }
    }

    public enum SubtypeDirection {
        UNDIRECTED, FROMSUBTYPE, FROMSUPERTYPE
    }
}
