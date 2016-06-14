package constraintgraph;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;

public class Vertex {

    private List<Edge> edges;
    private Slot slot;
    private int id;
    private AnnotationMirror value;

    public Vertex(Slot slot) {
        this.edges = new ArrayList<Edge>();
        this.slot = slot;
        if (slot instanceof VariableSlot) {
            VariableSlot vs = (VariableSlot) slot;
            this.id = vs.getId();
            if (slot instanceof ConstantSlot) {
                ConstantSlot cs = (ConstantSlot) slot;
                this.value = cs.getValue();
            } else {
                this.value = null;
            }
        }
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

    public int getId() {
        return this.id;
    }

    public List<Edge> getEdges() {
        return this.edges;
    }

    public AnnotationMirror getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Vertex) {
            Vertex vertex = (Vertex) o;
            if (vertex.id == this.id) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
