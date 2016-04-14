package constraintsolver;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

public class Lattice {
    QualifierHierarchy qualHierarchy;
    public static Map<AnnotationMirror, Collection<AnnotationMirror>> subType = AnnotationUtils.createAnnotationMap();
    public static Map<AnnotationMirror, Collection<AnnotationMirror>> superType = AnnotationUtils.createAnnotationMap();
    public static Map<AnnotationMirror, Collection<AnnotationMirror>> notComparableType = AnnotationUtils.createAnnotationMap();
    public static Map<AnnotationMirror, Integer> modifierInt = AnnotationUtils.createAnnotationMap();
    public static Map<Integer,AnnotationMirror> IntModifier = new HashMap<Integer,AnnotationMirror>();
    public static Set<? extends AnnotationMirror> allTypes;
    public static AnnotationMirror top;
    public static AnnotationMirror bottom;
    public static int numModifiers;

    public Lattice(QualifierHierarchy qualHierarchy) {
        this.qualHierarchy = qualHierarchy;
        this.allTypes = qualHierarchy.getTypeQualifiers();
        this.top = qualHierarchy.getTopAnnotations().iterator().next();
        this.bottom = qualHierarchy.getBottomAnnotations().iterator().next();
        this.numModifiers = qualHierarchy.getTypeQualifiers().size();
        getSubSupertype();
        getNotComparable();
    }

    private void getSubSupertype() {
        int num = 1;
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
            modifierInt.put(i, num);
            IntModifier.put(num, i);
            num++;
        }
//        for (Integer j: IntModifier.keySet()){
//            System.out.println("final key "+j+ "  " + "final value: " + IntModifier.get(j).toString());
//        }
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