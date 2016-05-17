package parameterizedtypesystem.constraintgraph;

import java.util.ArrayList;
import java.util.List;

import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Slot;

public class Vertex {

    private List<Edge> edges;
    private Slot slot;
    private int id;
    private String value;

    public Vertex() {
        this.edges = new ArrayList<Edge>();
    }

    public Vertex(Slot slot) {
        this.slot = slot;
    }

    public boolean isConstant() {
        return (this.slot instanceof ConstantSlot);
    }

    public void addEdge(Edge edge) {
        if (!edges.contains(edge)) {
            edges.add(edge);
        }
    }

    public Slot getSlot() {
        return this.slot;
    }

}
