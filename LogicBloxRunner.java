package checkers.inference.solver.LogiqlDebugSolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * 
 *
 * @author Jianchu Li
 *
 */

public class LogicBloxRunner {

    private String inReply = "";
    private final String path;

    public LogicBloxRunner(String path) {
        this.path = path;
    }

    public void runLogicBlox() throws IOException, InterruptedException {
        String[] command = new String[7];
        command[0] = "lb create pltest";
        command[1] = "lb addblock pltest -f" + path + "/LogiqlEncoding.logic";
        command[2] = "lb exec pltest -f" + path + "/data.logic";
        command[3] = "lb print pltest orderedAnnotationOf";
        command[4] = "lb delete pltest";
        for (int i = 0; i < 5; i++) {
            getOutPut_Error(command[i], i);
        }
        writeFile(inReply);
    }

    private void getOutPut_Error(String command, final int i)
            throws IOException, InterruptedException {
        final Process p = Runtime.getRuntime().exec(command);
        Thread getOutPut = new Thread() {
            public void run() {
                String s = "";
                BufferedReader stdInput = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));
                try {
                    while ((s = stdInput.readLine()) != null) {
                        if (i == 3) {
                            inReply = inReply + s + "\n";
                            // System.out.println(s);
                        }
                    }
                } catch (NumberFormatException | IOException e) {
                    e.printStackTrace();
                }
            }
        };
        getOutPut.start();

        Thread getError = new Thread() {
            public void run() {
                String s = "";
                String errReply = "";
                BufferedReader stdError = new BufferedReader(
                        new InputStreamReader(p.getErrorStream()));
                try {
                    while ((s = stdError.readLine()) != null) {
                        errReply = errReply + s;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        getError.start();
        getOutPut.join();
        getError.join();
        p.waitFor();
    }

    private void writeFile(String output) {
        try {
            String writePath = path + "/logicbloxOutput.txt";
            File f = new File(writePath);
            PrintWriter pw = new PrintWriter(f);
            pw.write(output);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
