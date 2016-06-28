package constraintsolver;

import org.checkerframework.javacutil.AnnotationUtils;

import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

public class TwoQualifiersLattice extends Lattice {

    protected Set<AnnotationMirror> allTypes;

    public TwoQualifiersLattice(AnnotationMirror top, AnnotationMirror bottom) {
        this.top = top;
        this.bottom = bottom;
        this.allTypes = AnnotationUtils.createAnnotationSet();
        this.numTypes = 2;
    }

    @Override
    public void configure() {
        this.allTypes.add(top);
        this.allTypes.add(bottom);
        getSubSupertype();
    }

    @Override
    protected void getSubSupertype() {
        Set<AnnotationMirror> topSet = AnnotationUtils.createAnnotationSet();
        Set<AnnotationMirror> bottomSet = AnnotationUtils.createAnnotationSet();
        topSet.add(top);
        bottomSet.add(bottom);
        this.typeToInt.put(top, 0);
        this.typeToInt.put(bottom, 1);
        this.intToType.put(0, top);
        this.intToType.put(1, bottom);
        subType.put(top, allTypes);
        superType.put(top, topSet);
        subType.put(bottom, bottomSet);
        superType.put(bottom, allTypes);
    }

    @Override
    public Set<AnnotationMirror> getAllTypes() {
        return this.allTypes;
    }

}
