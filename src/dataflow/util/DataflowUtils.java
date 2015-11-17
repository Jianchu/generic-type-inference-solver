package dataflow.util;

import org.checkerframework.javacutil.AnnotationUtils;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;

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
}
