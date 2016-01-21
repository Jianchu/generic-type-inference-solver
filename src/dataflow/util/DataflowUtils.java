package dataflow.util;

import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.util.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.NewClassTree;

import dataflow.qual.DataFlow;

public class DataflowUtils {
   
    
    public static String[] getDataflowValue(AnnotationMirror type) {
        List<String> allTypesList = AnnotationUtils.getElementValueArray(type,"typeNames", String.class, true);
        //types in this list is org.checkerframework.framework.util.AnnotationBuilder.
        String[] allTypesInArray = new String[allTypesList.size()];
        int i = 0;
        for (Object o :allTypesList){
            allTypesInArray[i] = o.toString();
            i++;
        }
        return allTypesInArray;
    }

    public static AnnotationMirror createDataflowAnnotation(Set<String> datatypes, ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder =
            new AnnotationBuilder(processingEnv, DataFlow.class);
        
        return createDataflowAnnotation(datatypes,builder);

    }
    
    private static AnnotationMirror createDataflowAnnotation(String[] dataType, ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder =
            new AnnotationBuilder(processingEnv, DataFlow.class);
        // System.out.println("ddddddddddddddddddddddddddddddddddddddddddddd"
        // + Arrays.toString(dataType));
        // if (Arrays.toString(dataType) != null) {
        // builder.setValue("typeNames", dataType);
        // }
        builder.setValue("typeNames", dataType);
        return builder.build();
    }
    
    private static String[] convert (String... typeName){
        return typeName;
    }
    
    private static AnnotationMirror createDataflowAnnotation(
            final Set<String> datatypes, final AnnotationBuilder builder) {
        String[] datatypesInArray = new String[datatypes.size()];
        int i = 0;
        for (String datatype : datatypes) {
            datatypesInArray[i] = datatype.toString();
            i++;
        }
        builder.setValue("typeNames", datatypesInArray);
        return builder.build();        
    }

    public static AnnotationMirror genereateDataflowAnnoFromNewClass(
            NewClassTree node, AnnotatedTypeMirror type,
            ProcessingEnvironment processingEnv) {
        TypeMirror tm = type.getUnderlyingType();
        String className = tm.toString();
        AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(
                convert(className), processingEnv);
        return dataFlowType;

    }

    // TODO :doc
    public static AnnotationMirror generateDataflowAnnoFromLiteral(
            LiteralTree node, AnnotatedTypeMirror type,
            ProcessingEnvironment processingEnv) {
        String datatypeInArray[] = { "" };
        // String datatypeInArray[] = null;
        switch(node.getKind()){
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
            break;
        default:
            break;
        }
        AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(
                    datatypeInArray, processingEnv);
        return dataFlowType;
    }
}