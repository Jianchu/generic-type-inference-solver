package ontology.util;

import org.checkerframework.javacutil.TypesUtils;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class OntologyUtils {

    public static boolean isSequence(TypeMirror type) {
        if (TypesUtils.isDeclaredOfName(type, "java.util.LinkedList")) {
            return true;
        } else if (TypesUtils.isDeclaredOfName(type, "java.util.ArrayList")) {
            return true;
        } else if (type.getKind().equals(TypeKind.ARRAY)) {
            return true;
        }
        return false;
    }

}
