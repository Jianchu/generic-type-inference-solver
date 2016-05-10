package ontology.util;

import ontology.qual.Ontology;

import org.checkerframework.framework.util.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TypesUtils;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class OntologyUtils {

    public static boolean determineAnnotation(TypeMirror type) {
        if (TypesUtils.isDeclaredOfName(type, "java.util.LinkedList")
                || TypesUtils.isDeclaredOfName(type, "java.util.ArrayList")
                || type.getKind().equals(TypeKind.ARRAY)) {
            return true;
        }
        return false;
    }

    public static AnnotationMirror genereateOntologyAnnoFromNew(ProcessingEnvironment processingEnv) {
        String className = "sequence";
        AnnotationMirror ontologyValue = createOntologyAnnotation(convert(className), processingEnv);
        return ontologyValue;
    }

    public static String[] convert(String... typeName) {
        return typeName;
    }

    public static AnnotationMirror createOntologyAnnotation(Set<String> values,
            ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, Ontology.class);
        return createOntologyAnnotation(values, builder);
    }

    private static AnnotationMirror createOntologyAnnotation(final Set<String> values,
            final AnnotationBuilder builder) {
        String[] valuesInArray = new String[values.size()];
        int i = 0;
        for (String value : values) {
            valuesInArray[i] = value.toString();
            i++;
        }
        builder.setValue("values", valuesInArray);
        return builder.build();
    }

    public static AnnotationMirror createOntologyAnnotation(String[] value,
            ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, Ontology.class);
        builder.setValue("values", value);
        return builder.build();
    }
    
    public static String[] getOntologyValue(AnnotationMirror type) {
        List<String> allTypesList = AnnotationUtils.getElementValueArray(type, "values", String.class, true);
        //types in this list is org.checkerframework.framework.util.AnnotationBuilder.
        String[] allTypesInArray = new String[allTypesList.size()];
        int i = 0;
        for (Object o : allTypesList) {
            allTypesInArray[i] = o.toString();
            i++;
        }
        return allTypesInArray;
    }
}
