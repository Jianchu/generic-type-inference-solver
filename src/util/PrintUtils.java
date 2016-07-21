package util;

import java.util.Map;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.InferenceMain;

public class PrintUtils {

    /**
     * print result from sat solver for testing.
     *
     * @param result
     */
    public static void printResult(Map<Integer, AnnotationMirror> result) {

        final int maxLength = String.valueOf(InferenceMain.getInstance().getSlotManager().getNumberOfSlots()).length();

        System.out.println("/***********************results****************************/");
        for (Integer j : result.keySet()) {
            String resultStr = "SlotID: " + j;
            for (int i = 0; i < maxLength + 2 - String.valueOf(j).length(); i++) {
                resultStr = resultStr + " ";
            }
            resultStr = resultStr + "Annotation: " + result.get(j).toString();
            System.out.println(resultStr);
        }
        System.out.flush();
        System.out.println("/**********************************************************/");
    }
}
