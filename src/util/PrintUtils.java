package util;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;

import util.StatisticPrinter.StatisticKey;
import checkers.inference.InferenceMain;

public class PrintUtils {

    /**
     * print result from sat solver for testing.
     *
     * @param result
     */
    public static void printResult(Map<Integer, AnnotationMirror> result) {

        final int maxLength = String.valueOf(InferenceMain.getInstance().getSlotManager().nextId()).length();

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

    public static void printStatistic(Map<StatisticKey, Long> statistic) {
        System.out.println("/***********************statistic start*************************/");
        for (StatisticKey j : statistic.keySet()) {
            if (statistic.get(j) != (long) 0) {
                System.out.println(j.toString().toLowerCase() + ": " + statistic.get(j));
            }
        }
        System.out.flush();
        System.out.println("/**********************statistic end****************************/");
    }

    public static void writeStatistic(Map<StatisticKey, Long> statistic) {
        String writePath = new File(new File("").getAbsolutePath()).toString() + "/statistic.txt";
        StringBuilder sb = new StringBuilder();
        for (StatisticKey j : statistic.keySet()) {
            if (statistic.get(j) != (long) 0) {
                sb.append((j.toString().toLowerCase() + "," + statistic.get(j)) + "\n");
            }
        }
        try {
            File f = new File(writePath);
            PrintWriter pw = new PrintWriter(writePath);
            pw.write(sb.toString());
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
