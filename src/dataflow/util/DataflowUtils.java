package dataflow.util;

import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.util.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;

import com.sun.source.tree.LiteralTree;

import dataflow.qual.DataFlow;

public class DataflowUtils {

    private static String[] getDataflowValue(AnnotationMirror type, String valueName) {
        List<String> allTypesList = AnnotationUtils.getElementValueArray(type,valueName, String.class, true);
        //types in this list is org.checkerframework.framework.util.AnnotationBuilder.
        String[] allTypesInArray = new String[allTypesList.size()];
        int i = 0;
        for (Object o : allTypesList) {
            allTypesInArray[i] = o.toString();
            i++;
        }
        return allTypesInArray;
    }

    public static String[] getTypeNames(AnnotationMirror type) {
        return getDataflowValue(type, "typeNames");
    }

    public static String[] getTypeNameRoots(AnnotationMirror type) {
        return getDataflowValue(type, "typeNameRoots");
    }

    public static AnnotationMirror createDataflowAnnotationForByte(String[] dataType,
            ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, DataFlow.class);
        builder.setValue("typeNameRoots", dataType);
        return builder.build();
    }

    public static String[] convert(String... typeName) {
        return typeName;
    }

    private static AnnotationMirror createDataflowAnnotation(final Set<String> datatypes, final AnnotationBuilder builder) {
        String[] datatypesInArray = new String[datatypes.size()];
        int i = 0;
        for (String datatype : datatypes) {
            datatypesInArray[i] = datatype.toString();
            i++;
        }
        builder.setValue("typeNames", datatypesInArray);
        return builder.build();
    }

    private static AnnotationMirror createDataflowAnnotationWithoutName(final Set<String> roots,
            final AnnotationBuilder builder) {
        String[] datatypesInArray = new String[roots.size()];
        int i = 0;
        for (String datatype : roots) {
            datatypesInArray[i] = datatype.toString();
            i++;
        }
        builder.setValue("typeNameRoots", datatypesInArray);
        return builder.build();
    }

    public static AnnotationMirror createDataflowAnnotation(Set<String> datatypes,
            ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, DataFlow.class);

        return createDataflowAnnotation(datatypes, builder);
    }

    public static AnnotationMirror createDataflowAnnotationWithoutName(Set<String> roots,
            ProcessingEnvironment processingEnv) {

        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, DataFlow.class);
        return createDataflowAnnotationWithoutName(roots, builder);

    }

    public static AnnotationMirror createDataflowAnnotation(String[] dataType,
            ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, DataFlow.class);
        builder.setValue("typeNames", dataType);
        return builder.build();
    }

    public static AnnotationMirror createDataflowAnnotationWithRoots(Set<String> datatypes,
            Set<String> datatypesRoots, ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, DataFlow.class);

        return createDataflowAnnotationWithRoots(datatypes, datatypesRoots, builder);
    }

    private static AnnotationMirror createDataflowAnnotationWithRoots(final Set<String> datatypes,
            final Set<String> datatypesRoots, final AnnotationBuilder builder) {
        String[] datatypesInArray = new String[datatypes.size()];
        int i = 0;
        for (String datatype : datatypes) {
            datatypesInArray[i] = datatype.toString();
            i++;
        }

        String[] datatypesRootInArray = new String[datatypesRoots.size()];
        int j = 0;
        for (String datatypesRoot : datatypesRoots) {
            datatypesRootInArray[j] = datatypesRoot.toString();
            j++;
        }
        if (datatypesRootInArray.length > 0) {
            builder.setValue("typeNameRoots", datatypesRootInArray);
        }
        if (datatypesInArray.length > 0) {
            builder.setValue("typeNames", datatypesInArray);
        }

        return builder.build();
    }

    public static AnnotationMirror genereateDataflowAnnoFromNewClass(AnnotatedTypeMirror type,
            ProcessingEnvironment processingEnv) {
        TypeMirror tm = type.getUnderlyingType();
        String className = tm.toString();
        AnnotationMirror dataFlowType = createDataflowAnnotation(
                convert(className), processingEnv);
        return dataFlowType;
    }

    public static AnnotationMirror genereateDataflowAnnoFromByteCode(AnnotatedTypeMirror type,
            ProcessingEnvironment processingEnv) {
        TypeMirror tm = type.getUnderlyingType();
        String className = tm.toString();
        AnnotationMirror dataFlowType = createDataflowAnnotationForByte(convert(className), processingEnv);
        return dataFlowType;
    }

    public static AnnotationMirror generateDataflowAnnoFromLiteral(AnnotatedTypeMirror type, ProcessingEnvironment processingEnv) {
        String datatypeInArray[] = convert(type.getUnderlyingType().toString());
        AnnotationMirror dataFlowType = createDataflowAnnotation(datatypeInArray, processingEnv);
        return dataFlowType;
    }
    
    // TODO :doc
    public static AnnotationMirror generateDataflowAnnoFromLiteral(LiteralTree node, ProcessingEnvironment processingEnv) {
        String datatypeInArray[] = { "" };
        // String datatypeInArray[] = null;
        switch (node.getKind()) {
        case STRING_LITERAL:
            datatypeInArray = convert(String.class.toString().split(" ")[1]);
            break;
        case INT_LITERAL:
            datatypeInArray = convert(int.class.toString());
            break;
        case LONG_LITERAL:
            datatypeInArray = convert(long.class.toString());
            break;
        case FLOAT_LITERAL:
            datatypeInArray = convert(float.class.toString());
            break;
        case DOUBLE_LITERAL:
            datatypeInArray = convert(double.class.toString());
            break;
        case BOOLEAN_LITERAL:
            datatypeInArray = convert(boolean.class.toString());
            break;
        case CHAR_LITERAL:
            datatypeInArray = convert(char.class.toString());
            break;
        case NULL_LITERAL:
            // JLTODO: this results in "" for null, which is not very useful.
            // We had discussed two modes: not including null, or making it
            // explicit.
            break;
        default:
            // JLTODO: Raise an error?!
            break;
        }
        AnnotationMirror dataFlowType = createDataflowAnnotation(
                    datatypeInArray, processingEnv);
        return dataFlowType;
    }

    public static AnnotationMirror createDataflowAnnotation(String typeName, ProcessingEnvironment processingEnv) {
        Set<String> typeNames = new HashSet<String>();
        typeNames.add(typeName);
        AnnotationMirror am = DataflowUtils.createDataflowAnnotation(typeNames, processingEnv);
        return am;
    }
}