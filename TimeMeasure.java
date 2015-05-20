import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TimeMeasure {
    private final String baseCommand;
    private final String testPath;
    private final String writePath;
    private String csvOutPut;
    private int measureTimes;
    private Map<String, Integer> linesOfCode = new HashMap<String, Integer>();
    private Map<String, Integer> timeResult = new LinkedHashMap<String, Integer>();
    private ArrayList<String> fileNames;

    public TimeMeasure() {
        baseCommand = "./inference.py --checker ostrusted.OsTrustedChecker --solver checkers.inference.solver.LogiqlDebugSolver.LogiqlDebugSolverCalTime testcase/";
        testPath = new File("").getAbsolutePath() + "/testcase";
        measureTimes = 1;
        writePath = new File("").getAbsolutePath();
    }

    public ArrayList<String> getFileName(TimeMeasure m) {
        ArrayList<String> result = new ArrayList<String>();
        File testcase = new File(m.testPath);
        File[] listOfTestCase = testcase.listFiles();
        for (int i = 0; i < listOfTestCase.length; i++) {
            if (listOfTestCase[i].isFile()) {
                result.add(listOfTestCase[i].getName());
            }
            if (listOfTestCase[i].isDirectory()) {
                result.add(listOfTestCase[i].getName());
            }
        }
        return result;
    }

    public void getLinesOfCode(ArrayList<String> fileName, TimeMeasure m)
            throws IOException {
        int count = 0;
        for (int i = 0; i < fileName.size(); i++) {
            count = 0;
            if (fileName.get(i).contains("java")) {
                InputStream is = new BufferedInputStream(new FileInputStream(
                        "testcase/" + fileName.get(i)));
                try {
                    byte[] c = new byte[1024];
                    int readChars = 0;
                    while ((readChars = is.read(c)) != -1) {
                        for (int j = 0; j < readChars; ++j) {
                            if (c[j] == '\n') {
                                ++count;
                            }
                        }
                    }
                } finally {
                    is.close();
                }
                m.linesOfCode.put(fileName.get(i), count);
            }
        }
    }

    public void runJavac(String fileName, TimeMeasure m) throws IOException {
        String command = "javac testcase/" + fileName;

        String key = "Time for compiling code with javac";
        long javacStart = 0;
        long javacEnd = 0;
        javacStart = System.currentTimeMillis();
        Process p = Runtime.getRuntime().exec(command);
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        javacEnd = System.currentTimeMillis();
        if (!m.timeResult.containsKey(key)) {
            m.timeResult.put(key, (int) (javacEnd - javacStart));
        } else if (m.timeResult.containsKey(key)) {
            m.timeResult.put(key, m.timeResult.get(key)
                    + (int) (javacEnd - javacStart));
        }
    }

    public void runScripts(String fileName, TimeMeasure m) throws IOException {
        String command = "";
        String s = "";
        long inferenceStart = 0;
        long inferenceEnd = 0;
        String key = "Time for running the whole process";
        command = m.baseCommand + fileName;
        inferenceStart = System.currentTimeMillis();
        Process p = Runtime.getRuntime().exec(command);
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        inferenceEnd = System.currentTimeMillis();
        if (!m.timeResult.containsKey(key)) {
            m.timeResult.put(key, (int) (inferenceEnd - inferenceStart));
        } else if (m.timeResult.containsKey(key)) {
            m.timeResult.put(key, m.timeResult.get(key)
                    + (int) (inferenceEnd - inferenceStart));
        }

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                p.getInputStream()));
        while ((s = stdInput.readLine()) != null) {
            if (s.contains("Total time") || s.contains("generating")
                    || s.contains("Logiql solver:")
                    || s.contains("The number of slots:")
                    || s.contains("decoding the")
                    || s.contains("number of constraints")) {
                String[] parts = s.split(": ");
                if (!m.timeResult.containsKey(parts[0])) {
                    m.timeResult.put(parts[0], Integer.parseInt(parts[1]));
                } else if (m.timeResult.containsKey(parts[0])) {
                    m.timeResult.put(parts[0], m.timeResult.get(parts[0])
                            + Integer.parseInt(parts[1]));
                }
            }
        }
    }

    public void print(TimeMeasure m, int times) {
        for (Entry<String, Integer> entry : m.timeResult.entrySet()) {
            if (entry.getKey().contains("Time")
                    || entry.getKey().contains("time")) {
                System.out.println(entry.getKey() + ": " + entry.getValue()
                        / times + " ms");
            } else
                System.out.println(entry.getKey() + ": " + entry.getValue()
                        / times);
        }
    }

    public void generateCsv(String fileName, TimeMeasure m, int times) {
        m.csvOutPut = m.csvOutPut + fileName + ","
                + m.linesOfCode.get(fileName);
        for (Entry<String, Integer> entry : m.timeResult.entrySet()) {
            m.csvOutPut = m.csvOutPut + "," + entry.getValue() / times;
        }
        m.csvOutPut = m.csvOutPut + "\n";
    }

    public void writeCsvtitle(TimeMeasure m) {
        m.csvOutPut = "File_name,SLOC,Time_whole_process,Number_slots,Number_constraints,Time_LogiqlSolver,Time_variables,Time_constraints,Time_logicfile,Time_decoding,Time_javac\n";
    }

    public void writeCsvFile(TimeMeasure m) throws FileNotFoundException {
        String filePath = writePath + "/csv";
        File f = new File(filePath);
        PrintWriter pw = new PrintWriter(f);
        pw.write(m.csvOutPut);
        pw.close();
    }

    public void deleteClass(TimeMeasure m) {
        File testcase = new File(m.testPath);
        File[] listOfTestCase = testcase.listFiles();
        for (int i = 0; i < listOfTestCase.length; i++) {
            if (listOfTestCase[i].getName().contains(".class")) {
                listOfTestCase[i].delete();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println();
        int times = 1;
        TimeMeasure m = new TimeMeasure();
        m.writeCsvtitle(m);
        if (args.length > 0) {
            times = Integer.parseInt(args[0]);
            m.measureTimes = Integer.parseInt(args[0]);
        }
        System.out.println("Every result is the average by tesing "
                + m.measureTimes + " times.\n");
        m.fileNames = m.getFileName(m);
        m.getLinesOfCode(m.fileNames, m);

        for (int i = 0; i < m.fileNames.size(); i++) {
            if (args.length > 0) {
                m.measureTimes = Integer.parseInt(args[0]);
            } else {
                m.measureTimes = 1;
            }
            if (m.fileNames.get(i).contains("java")) {
                System.out
                        .println("The result for " + m.fileNames.get(i) + ":");
                System.out.println("The number of lines of the code: "
                        + m.linesOfCode.get(m.fileNames.get(i)));
                while (m.measureTimes != 0) {
                    m.runScripts(m.fileNames.get(i), m);
                    m.runJavac(m.fileNames.get(i), m);
                    m.measureTimes--;
                }
                m.print(m, times);
                System.out.println();
                if (args.length > 1) {
                    m.generateCsv(m.fileNames.get(i), m, times);
                }
                m.timeResult.clear();
            }
        }
        m.deleteClass(m);
        m.writeCsvFile(m);
    }
}
