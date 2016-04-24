package constraintsolver;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

/**
 *
 * @author jianchu
 *
 */
/* JLTODO: it is not nice that there is so much public static state in this class.
 * Determine who needs access to this information and instanciate the class there.
 * Make the fields private and final, as much as possible.
 */
public class Lattice {

    private static QualifierHierarchy qualHierarchy;
    public static Map<AnnotationMirror, Collection<AnnotationMirror>> subType = AnnotationUtils.createAnnotationMap();
    public static Map<AnnotationMirror, Collection<AnnotationMirror>> superType = AnnotationUtils.createAnnotationMap();
    public static Map<AnnotationMirror, Collection<AnnotationMirror>> incomparableType = AnnotationUtils.createAnnotationMap();
    public static Map<AnnotationMirror, Integer> typeToInt = AnnotationUtils.createAnnotationMap();
    public static Map<Integer,AnnotationMirror> intToType = new HashMap<Integer,AnnotationMirror>();
    public static Set<? extends AnnotationMirror> allTypes;
    public static AnnotationMirror top;
    public static AnnotationMirror bottom;
    public static int numTypes;

    public static void configure(QualifierHierarchy qualHierarchy) {
        Lattice.qualHierarchy = qualHierarchy;
        allTypes = qualHierarchy.getTypeQualifiers();
        top = qualHierarchy.getTopAnnotations().iterator().next();
        bottom = qualHierarchy.getBottomAnnotations().iterator().next();
        numTypes = qualHierarchy.getTypeQualifiers().size();
        getSubSupertype();
        getIncomparable();
    }

    private static void getSubSupertype() {
        int num = 0;
        for (AnnotationMirror i : allTypes) {
            Set<AnnotationMirror> subtypeOfi = new HashSet<AnnotationMirror>();
            Set<AnnotationMirror> supertypeOfi = new HashSet<AnnotationMirror>();
            for (AnnotationMirror j : allTypes) {
                if (qualHierarchy.isSubtype(j, i)) {
                    subtypeOfi.add(j);
                }
                if (qualHierarchy.isSubtype(i, j)) {
                    supertypeOfi.add(j);
                }
            }
            subType.put(i, subtypeOfi);
            superType.put(i, supertypeOfi);
            typeToInt.put(i, num);
            intToType.put(num, i);
            num++;
        }
    }

    private static void getIncomparable() {
        for (AnnotationMirror i : allTypes) {
            Set<AnnotationMirror> incomparableOfi = new HashSet<AnnotationMirror>();
            for (AnnotationMirror j : allTypes) {
                if (!subType.get(i).contains(j) && !subType.get(j).contains(i)) {
                    incomparableOfi.add(j);
                }
            }
            if (!incomparableOfi.isEmpty()) {
                incomparableType.put(i, incomparableOfi);
            }
        }
    }
}