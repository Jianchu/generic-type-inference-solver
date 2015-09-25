package GeneralMaxSatSolver;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

public class LatticeGenerator {
    QualifierHierarchy qualHierarchy;
    Map<AnnotationMirror, Collection<AnnotationMirror>> subType = new HashMap<AnnotationMirror, Collection<AnnotationMirror>>();
    Map<AnnotationMirror, Collection<AnnotationMirror>> superType = new HashMap<AnnotationMirror, Collection<AnnotationMirror>>();
    Map<AnnotationMirror, Collection<AnnotationMirror>> notComparableType = new HashMap<AnnotationMirror, Collection<AnnotationMirror>>();
    Set<? extends AnnotationMirror> allTypes;
    AnnotationMirror top;
    AnnotationMirror bottom;

    public LatticeGenerator(QualifierHierarchy qualHierarchy) {
        this.qualHierarchy = qualHierarchy;
        this.allTypes = qualHierarchy.getTypeQualifiers();
        this.top = qualHierarchy.getTopAnnotations().iterator().next();
        this.bottom = qualHierarchy.getBottomAnnotations().iterator().next();
        getSubSupertype();
        getNotComparable();
    }

    private void getSubSupertype() {
        for (AnnotationMirror i : allTypes) {
            Set<AnnotationMirror> subtypeFori = new HashSet<AnnotationMirror>();
            Set<AnnotationMirror> supertypeFori = new HashSet<AnnotationMirror>();
            for (AnnotationMirror j : allTypes) {
                if (qualHierarchy.isSubtype(j, i)) {
                    subtypeFori.add(j);
                }
                if (qualHierarchy.isSubtype(i, j)) {
                    supertypeFori.add(j);
                }
            }
            subType.put(i, subtypeFori);
            superType.put(i, supertypeFori);
        }
    }

    private void getNotComparable() {
        for (AnnotationMirror i : allTypes) {
            Set<AnnotationMirror> notComparableFori = new HashSet<AnnotationMirror>();
            for (AnnotationMirror j : allTypes) {
                if (!subType.get(i).contains(j)
                        && !subType.get(j).contains(i)) {
                    notComparableFori.add(j);
                }
            }
            if (!notComparableFori.isEmpty()) {
                notComparableType.put(i, notComparableFori);
            }
        }
    }
}
