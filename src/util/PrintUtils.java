package util;

import java.util.Map;

import javax.lang.model.element.AnnotationMirror;

public class PrintUtils {

    /**
     * print result from sat solver for testing.
     *
     * @param result
     */
    public static void printResult(Map<Integer, AnnotationMirror> result) {
        System.out.println("/***********************result*****************************/");
        for (Integer j : result.keySet()) {
            System.out.println("SlotID: " + j + "  " + "Annotation: " + result.get(j).toString());
        }
        System.out.flush();
        System.out.println("/**********************************************************/");
    }
}
