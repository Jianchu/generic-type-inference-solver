package constraintgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
    private Map<Vertex, Set<Constraint>> constantPath;
    private Map<Integer, Vertex> verticies;
    private List<Set<Constraint>> independentPath;
    private Collection<Constraint> missingConstraints = new HashSet<>();

    protected ConstraintGraph() {
        this.edges = new HashSet<Edge>();
        this.constantVerticies = new HashSet<Vertex>();
        this.constantPath = new HashMap<Vertex, Set<Constraint>>();
        this.verticies = new HashMap<Integer, Vertex>();
        this.independentPath = new LinkedList<Set<Constraint>>();
    }

    protected void addEdge(Edge edge) {
        if (!this.edges.contains(edge)) {
            this.edges.add(edge);
        }
    }

    protected List<Vertex> getVerticies() {
        return new ArrayList<Vertex>(this.verticies.values());
    }

    protected Set<Edge> getEdges() {
        return this.edges;
    }

    protected Set<Vertex> getConstantVerticies() {
        return this.constantVerticies;
    }

    public Map<Vertex, Set<Constraint>> getConstantPath() {
        return this.constantPath;
    }

    protected void addConstantPath(Vertex vertex, Set<Constraint> constraints) {
        this.constantPath.put(vertex, constraints);
    }

    protected void addEdgeToConstantPath(Vertex vertex, Constraint constraint) {
        Set<Constraint> constrants = this.constantPath.get(vertex);
        constrants.add(constraint);
    }

    public List<Set<Constraint>> getIndependentPath() {
        return this.independentPath;
    }

    protected void addIndependentPath(Set<Constraint> independentPath) {
        this.independentPath.add(independentPath);
    }

    protected void addConstant(Vertex vertex) {
        if (!this.constantVerticies.contains(vertex)) {
            this.constantVerticies.add(vertex);
        }
    }
    
    public Edge findEdge(Constraint constraint) {
        for (Edge edge : this.edges) {
            if (edge.getConstraint().equals(constraint)) {
                return edge;
            }
        }
        return null;
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

    public Collection<Constraint> getMissingConstraints() {
        return this.missingConstraints;
    }

    public void SetMissingConstraints(Collection<Constraint> missingConstraint) {
        this.missingConstraints = missingConstraint;
    }
}
