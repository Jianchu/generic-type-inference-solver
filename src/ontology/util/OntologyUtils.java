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
        AnnotationMirror dataFlowType = createOntologyAnnotation(convert(className), processingEnv);
        return dataFlowType;
    }

    // public static AnnotationMirror
    // genereateDataflowAnnoFromNewArray(ProcessingEnvironment processingEnv) {
    // String className = "sequence";
    // AnnotationMirror dataFlowType =
    // createOntologyAnnotation(convert(className), processingEnv);
    // return dataFlowType;
    // }

    public static String[] convert(String... typeName) {
        return typeName;
    }

    public static AnnotationMirror createOntologyAnnotation(Set<String> datatypes,
            ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, Ontology.class);
        return createOntologyAnnotation(datatypes, builder);
    }

    private static AnnotationMirror createOntologyAnnotation(final Set<String> datatypes,
            final AnnotationBuilder builder) {
        String[] datatypesInArray = new String[datatypes.size()];
        int i = 0;
        for (String datatype : datatypes) {
            datatypesInArray[i] = datatype.toString();
            i++;
        }
        builder.setValue("typeNames", datatypesInArray);
        return builder.build();
    }

    public static AnnotationMirror createOntologyAnnotation(String[] dataType,
            ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, Ontology.class);
        builder.setValue("typeNames", dataType);
        return builder.build();
    }
    
    public static String[] getOntologyValue(AnnotationMirror type) {
        List<String> allTypesList = AnnotationUtils.getElementValueArray(type,"typeNames", String.class, true);
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
