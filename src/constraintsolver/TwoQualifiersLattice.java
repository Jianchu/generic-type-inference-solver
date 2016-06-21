package constraintsolver;

import org.checkerframework.framework.type.QualifierHierarchy;

import javax.lang.model.element.AnnotationMirror;

public class TwoQualifiersLattice extends Lattice {

    public TwoQualifiersLattice(QualifierHierarchy qualHierarchy) {
        super(qualHierarchy);
    }

    public TwoQualifiersLattice(QualifierHierarchy qualHierarchy, AnnotationMirror top,
            AnnotationMirror bottom) {
        super(qualHierarchy);
        this.top = top;
        this.bottom = bottom;
    }
}
