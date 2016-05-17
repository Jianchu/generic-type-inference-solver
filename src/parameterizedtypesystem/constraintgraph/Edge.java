package parameterizedtypesystem.constraintgraph;

import checkers.inference.model.Constraint;

public class Edge {

    private Vertex vertex1;
    private Vertex vertex2;
    private Constraint constraint;

    public Edge(Vertex vertex1, Vertex vertex2, Constraint constraint) {
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.constraint = constraint;
    }
}
