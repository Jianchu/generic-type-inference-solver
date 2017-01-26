package constraintgraph;

import java.util.Objects;

import checkers.inference.model.Constraint;

/**
 * Edge in ConstraintGraph There is no direction for this edge, which means the
 * edge represented by this class is undirected edge.
 * 
 * @author jianchu
 *
 */
public class Edge {

    protected Vertex from;
    protected Vertex to;
    protected Constraint constraint;

    protected Edge(Vertex from, Vertex to, Constraint constraint) {
        this.from = from;
        this.to = to;
        this.constraint = constraint;
        attachEdge();
    }

    protected void attachEdge() {
        from.addEdge(this);
        to.addEdge(this);
    }

    protected Vertex getFromVertex() {
        return this.from;
    }

    protected Vertex getToVertex() {
        return this.to;
    }

    protected Constraint getConstraint() {
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