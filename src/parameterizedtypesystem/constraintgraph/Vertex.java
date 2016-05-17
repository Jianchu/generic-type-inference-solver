package parameterizedtypesystem.constraintgraph;

import java.util.ArrayList;
import java.util.List;

public class Vertex {

    private List<Edge> edges;
    private boolean isConstant;
    private int id;
    private String value;

    public Vertex() {
        this.edges = new ArrayList<Edge>();
    }

    public Vertex(int id, String value) {
        this.id = id;
        this.value = value;
        this.isConstant = true;
    }

    public Vertex(int id) {
        this.id = id;
        this.value = null;
        this.isConstant = false;
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

}
