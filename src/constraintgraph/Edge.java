package constraintgraph;

import java.util.Objects;

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

    @Override
    public String toString() {
        return from.getId() + "--->" + to.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Edge) {
            Edge edge = (Edge) o;
            if (this.from.equals(edge.from) && this.to.equals(edge.to)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
