package constraintgraph;

import checkers.inference.model.SubtypeConstraint;

/**
 * SubtypeEdge represents SubtypeConstraint, and it has direction: from from to
 * to.
 * 
 * @author jianchu
 *
 */
public class SubtypeEdge extends Edge {

    protected SubtypeEdge(Vertex from, Vertex to, SubtypeConstraint constraint) {
        super(from, to, constraint);
    }
}
