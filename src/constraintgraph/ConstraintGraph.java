package constraintgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private List<Vertex> verticies;
    private List<Edge> edges;
    private List<Vertex> constantVerticies;
    private Map<Vertex, Set<Constraint>> independentPath;

    public ConstraintGraph() {
        this.verticies = new ArrayList<Vertex>();
        this.edges = new ArrayList<Edge>();
        this.constantVerticies = new ArrayList<Vertex>();
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

    public List<Vertex> getVerticies() {
        return this.verticies;
    }

    public List<Edge> getEdges() {
        return this.edges;
    }

    public List<Vertex> getConstantVerticies() {
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
