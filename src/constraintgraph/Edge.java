package constraintgraph;

import checkers.inference.model.Constraint;

/**
 * Edge in ConstraintGraph
 * 
 * @author jianchu
 *
 */
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

    public Vertex getFromVertex() {
        return this.from;
    }

    public Vertex getToVertex() {
        return this.to;
    }

    public Constraint getConstraint() {
        return this.constraint;
    }
}
