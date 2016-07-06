package constraintgraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import checkers.inference.model.Constraint;

/**
 * A graph representation for constants.
 * 
 * @author jianchu
 *
 */
public class ConstraintGraph {

    private Set<Vertex> verticies;
    private Set<Edge> edges;
    private Set<Vertex> constantVerticies;
    private Map<Vertex, Set<Constraint>> independentPath;

    public ConstraintGraph() {
        this.verticies = new HashSet<Vertex>();
        this.edges = new HashSet<Edge>();
        this.constantVerticies = new HashSet<Vertex>();
        this.independentPath = new HashMap<Vertex, Set<Constraint>>();
    }

    public void addVertex(Vertex vertex) {
        if (!verticies.contains(vertex)) {
            this.verticies.add(vertex);
        }
    }

    public void addEdge(Edge edge) {
        if (!this.edges.contains(edge)) {
            this.edges.add(edge);
            addVertex(edge.getFromVertex());
            addVertex(edge.getToVertex());
        }
    }

    public Set<Vertex> getVerticies() {
        return this.verticies;
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

}
