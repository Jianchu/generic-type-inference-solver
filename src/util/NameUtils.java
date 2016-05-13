package util;

import org.checkerframework.javacutil.AnnotationUtils;

import javax.lang.model.element.AnnotationMirror;

public class NameUtils {

    public static String getSimpleName(AnnotationMirror annoMirror) {
        return AnnotationUtils.annotationSimpleName(annoMirror).toString();
    }
}
