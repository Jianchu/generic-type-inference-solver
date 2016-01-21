package generalconstraintsolver.dataflowsolver.dataflowlogiqlsolver;

import generalconstraintsolver.logiqlsubsolver.LogicBloxRunner;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class LogicBloxRunnerForDataflow extends LogicBloxRunner {
    private String path;
    String[] command;
    String currentFile;

    public LogicBloxRunnerForDataflow(String path) {
        super(path);
        this.path = path;
        this.command = new String[7];
    }

    protected void logiqlSolve() throws IOException, InterruptedException {
        final File folder = new File(path + "/data");
        // command[0] = "lb create pltest";
        String createTableCommand = "lb addblock pltest -f" + path
                + "/LogiqlEncoding.logic";
        getOutPut_Error(createTableCommand, 0);
        String deleteDataCommand = "lb exec pltest -f" + path
                + "/deleteData.logic";
        for (final File fileEntry : folder.listFiles()) {
            int pointPosition = fileEntry.getName().toString().lastIndexOf('.');
            currentFile = fileEntry.getName().toString()
                    .substring(0, pointPosition);
            if (fileEntry.isFile()) {
                command[2] = "lb exec pltest -f" + path + "/data/"
                        + fileEntry.getName().toString();
                command[3] = "lb print pltest orderedAnnotationOf";
                runLogicBlox();
            }
            getOutPut_Error(deleteDataCommand, 0);
        }
    }

    @Override
    public void runLogicBlox() throws IOException {
        for (int i = 2; i < 4; i++) {
            try {
                getOutPut_Error(command[i], i);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        writeFile(inReply);
        inReply = "";
    }

    @Override
    protected void writeFile(String output) {
        try {
            String writePath = path + "/logibloxoutput/" + currentFile;
            File f = new File(writePath);
            PrintWriter pw = new PrintWriter(f);
            pw.write(output);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
