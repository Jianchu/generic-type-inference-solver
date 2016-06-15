package constraintgraph;

import checkers.inference.model.Constraint;

public class Edge {

    private Vertex from;
    private Vertex to;
    private Constraint constraint;

    public Edge(Vertex from, Vertex to, Constraint constraint) {
        this.from = from;
        this.to = to;
        this.constraint = constraint;
        attachEdge();
    }

    private void attachEdge() {
        from.addOutgoingEdge(this);
        to.addIncomingEdge(this);
    }

    public Vertex getVertex1() {
        return this.from;
    }

    public Vertex getVertex2() {
        return this.to;
    }

    public Constraint getConstraint() {
        return this.constraint;
    }
}
