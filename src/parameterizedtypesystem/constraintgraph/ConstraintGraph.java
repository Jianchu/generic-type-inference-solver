package parameterizedtypesystem.constraintgraph;

import java.util.ArrayList;
import java.util.List;

public class ConstraintGraph {

    private List<Vertex> verticies;
    private List<Edge> edges;

    public ConstraintGraph() {
        this.verticies = new ArrayList<Vertex>();
        this.edges = new ArrayList<Edge>();
    }

    public void addVertex(Vertex vertex) {
        if (!verticies.contains(vertex)) {
            this.verticies.add(vertex);
        }
    }

    public void addEdge(Edge edge) {
        if (!this.edges.contains(edge)) {
            this.edges.add(edge);
            addVertex(edge.getVertex1());
            addVertex(edge.getVertex2());
        }
    }

    public List<Vertex> getVerticies() {
        return this.verticies;
    }

    public List<Edge> getEdges() {
        return this.edges;
    }

}
