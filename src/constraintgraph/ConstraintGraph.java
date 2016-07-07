package constraintgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.model.VariableSlot;

/**
 * A graph representation for constants.
 * 
 * @author jianchu
 *
 */
public class ConstraintGraph {

    private Set<Edge> edges;
    private Set<Vertex> constantVerticies;
    private Map<Vertex, Set<Constraint>> independentPath;
    private Map<Integer, Vertex> verticies;

    public ConstraintGraph() {
        this.edges = new HashSet<Edge>();
        this.constantVerticies = new HashSet<Vertex>();
        this.independentPath = new HashMap<Vertex, Set<Constraint>>();
        this.verticies = new HashMap<Integer, Vertex>();
    }

    public void addEdge(Edge edge) {
        if (!this.edges.contains(edge)) {
            this.edges.add(edge);
        }
    }

    public List<Vertex> getVerticies() {
        return new ArrayList<Vertex>(this.verticies.values());
    }

    public Set<Edge> getEdges() {
        return this.edges;
    }

    public Set<Vertex> getConstantVerticies() {
        return this.constantVerticies;
    }

    public Map<Vertex, Set<Constraint>> getIndependentPath() {
        return this.independentPath;
    }

    public void addIndependentPath(Vertex vertex, Set<Constraint> constraints) {
        this.independentPath.put(vertex, constraints);
    }

    public void addConstant(Vertex vertex) {
        if (!this.constantVerticies.contains(vertex)) {
            this.constantVerticies.add(vertex);
        }
    }
    
    protected void createEdge(Slot slot1, Slot slot2, Constraint constraint) {
        Integer slot1Id = ((VariableSlot) slot1).getId();
        Integer slot2Id = ((VariableSlot) slot2).getId();
        Vertex vertex1;
        Vertex vertex2;

        if (this.verticies.keySet().contains(slot1Id)) {
            vertex1 = this.verticies.get(slot1Id);
        } else {
            vertex1 = new Vertex(slot1);
            this.verticies.put(slot1Id, vertex1);
        }

        if (this.verticies.keySet().contains(slot2Id)) {
            vertex2 = this.verticies.get(slot2Id);
        } else {
            vertex2 = new Vertex(slot2);
            this.verticies.put(slot2Id, vertex2);
        }
        
        Edge edge;
        
        if (constraint instanceof SubtypeConstraint) {
            edge = new SubtypeEdge(vertex1, vertex2, (SubtypeConstraint)constraint);
        } else {
            edge = new Edge(vertex1, vertex2, constraint);
        }

        this.addEdge(edge);
    }
}
