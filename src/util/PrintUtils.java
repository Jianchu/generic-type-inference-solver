package util;

import org.checkerframework.javacutil.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
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

        final int maxLength = String.valueOf(InferenceMain.getInstance().getSlotManager().getNumberOfSlots()).length();
        StringBuilder printResult = new StringBuilder();
        System.out.println("/***********************results****************************/");
        for (Integer j : result.keySet()) {
            printResult.append("SlotID: ");
            printResult.append(String.valueOf(j));
            for (int i = 0; i < maxLength + 2 - String.valueOf(j).length(); i++) {
                printResult.append(" ");
            }
            printResult.append("Annotation: ");
            printResult.append(result.get(j).toString());
            printResult.append("\n");
        }
        System.out.println(printResult.toString());
        System.out.flush();
        System.out.println("/**********************************************************/");
    }

    public static void printStatistic(Map<StatisticKey, Long> statistic,
            List<Pair<Long, Long>> threadData) {
        System.out.println("/***********************statistic start*************************/");
        for (StatisticKey j : statistic.keySet()) {
            if (statistic.get(j) != (long) 0) {
                System.out.println(j.toString().toLowerCase() + ": " + statistic.get(j));
            }
        }
        if (StatisticPrinter.SAT_PARALLEL_SERIALIZATION_SUM.get() != 0) {
            System.out.println(StatisticPrinter.StatisticKey.SAT_PARALLEL_SERIALIZATION_SUM.toString()
                    .toLowerCase() + "," + StatisticPrinter.SAT_PARALLEL_SERIALIZATION_SUM);
        }
        if (StatisticPrinter.SAT_PARALLEL_SOLVING_SUM.get() != 0) {
            System.out.println(StatisticPrinter.StatisticKey.SAT_PARALLEL_SOLVING_SUM.toString()
                    .toLowerCase() + "," + StatisticPrinter.SAT_PARALLEL_SOLVING_SUM);
        }
        System.out.flush();
        System.out.println("/**********************statistic end****************************/");
    }

    public static void writeStatistic(Map<StatisticKey, Long> statistic,
            List<Pair<Long, Long>> threadData) {
        String writePath = new File(new File("").getAbsolutePath()).toString() + "/statistic.txt";
        StringBuilder sb = new StringBuilder();
        for (StatisticKey j : statistic.keySet()) {
            if (statistic.get(j) != (long) 0) {
                sb.append((j.toString().toLowerCase() + "," + statistic.get(j)) + "\n");
            }
        }


        // Collections.sort(threadData, new Comparator<Pair<Long, Long>>() {
        //
        // @Override
        // public int compare(Pair<Long, Long> o1, Pair<Long, Long> o2) {
        // // TODO Auto-generated method stub
        // return -(int) ((o1.first + o1.second) - (o2.first + o2.second));
        // }
        //
        // });
        // for (Pair<Long, Long> data : threadData) {
        // sb.append(data.first + ", " + data.second + "\n");
        // }

        try {
            File f = new File(writePath);
            PrintWriter pw = new PrintWriter(writePath);
            pw.write(sb.toString());
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeResult(Map<Integer, AnnotationMirror> result) {
        StringBuilder printResult = new StringBuilder();
        final int maxLength = String.valueOf(InferenceMain.getInstance().getSlotManager().getNumberOfSlots())
                .length();

        for (Integer j : result.keySet()) {
            printResult.append("SlotID: ");
            printResult.append(String.valueOf(j));
            for (int i = 0; i < maxLength + 2 - String.valueOf(j).length(); i++) {
                printResult.append(" ");
            }
            printResult.append("Annotation: ");
            printResult.append(result.get(j).toString());
            printResult.append("\n");
        }

        File CNFData = new File(new File("").getAbsolutePath());
        String writePath = CNFData.getAbsolutePath() + "/result" + ".txt";
        File f = new File(writePath);
        PrintWriter pw;
        try {
            pw = new PrintWriter(f);
            pw.write(printResult.toString());
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        printResult = null;
    }

}
