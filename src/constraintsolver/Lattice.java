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

    private QualifierHierarchy qualHierarchy;
    protected static Lattice latticeInstance;
    public Map<AnnotationMirror, Collection<AnnotationMirror>> subType = AnnotationUtils .createAnnotationMap();
    public Map<AnnotationMirror, Collection<AnnotationMirror>> superType = AnnotationUtils.createAnnotationMap();
    public Map<AnnotationMirror, Collection<AnnotationMirror>> incomparableType = AnnotationUtils.createAnnotationMap();
    public Map<AnnotationMirror, Integer> typeToInt = AnnotationUtils.createAnnotationMap();
    public Map<Integer, AnnotationMirror> intToType = new HashMap<Integer, AnnotationMirror>();
    protected Set<? extends AnnotationMirror> allTypes;
    public AnnotationMirror top;
    public AnnotationMirror bottom;
    public int numTypes;

    public void configure() {
        allTypes = qualHierarchy.getTypeQualifiers();
        top = qualHierarchy.getTopAnnotations().iterator().next();
        bottom = qualHierarchy.getBottomAnnotations().iterator().next();
        numTypes = qualHierarchy.getTypeQualifiers().size();
        getSubSupertype();
        getIncomparable();
    }

    public Lattice() {
    }

    public Lattice(QualifierHierarchy qualHierarchy) {
        this.qualHierarchy = qualHierarchy;
        this.latticeInstance = this;
    }

    protected void getSubSupertype() {
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

    protected void getIncomparable() {
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

    public Set<? extends AnnotationMirror> getAllTypes() {
        return this.allTypes;
    }

    public static Map<AnnotationMirror, Collection<AnnotationMirror>> getSubtype() {
        return latticeInstance.subType;
    }

    public static Map<AnnotationMirror, Collection<AnnotationMirror>> getIncomparableType() {
        return latticeInstance.incomparableType;
    }
}