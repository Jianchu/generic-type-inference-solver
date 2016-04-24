package ontology.util;

import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TypesUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import ontology.qual.Sequence;

public class OntologyUtils {

    public static AnnotationMirror determineAnnotation(Elements elements, TypeMirror type) {
        if (TypesUtils.isDeclaredOfName(type, "java.util.LinkedList") ||
                TypesUtils.isDeclaredOfName(type, "java.util.ArrayList") ||
                type.getKind().equals(TypeKind.ARRAY)) {
            AnnotationMirror SEQ = AnnotationUtils.fromClass(elements, Sequence.class);
            return SEQ;
        }
        return null;
    }

}
