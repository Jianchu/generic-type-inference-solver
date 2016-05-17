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
}
