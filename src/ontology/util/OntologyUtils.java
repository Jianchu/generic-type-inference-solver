package ontology.util;

import org.checkerframework.javacutil.TypesUtils;

import javax.lang.model.type.TypeMirror;

public class OntologyUtils {

    // TODO: extend this to handle arrays.
    public static boolean isSequence(TypeMirror type) {
        if (TypesUtils.isDeclaredOfName(type, "java.util.LinkedList")) {
            return true;
        }
        return false;
    }

}
