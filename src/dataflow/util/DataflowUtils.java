package dataflow.util;

import org.checkerframework.framework.util.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import dataflow.quals.DataFlow;

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
}