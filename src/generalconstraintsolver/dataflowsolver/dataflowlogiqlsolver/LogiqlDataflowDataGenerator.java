package generalconstraintsolver.dataflowsolver.dataflowlogiqlsolver;

import generalconstraintsolver.dataflowsolver.DataflowImpliesLogic;
import generalconstraintsolver.logiqlsubsolver.LogiqlDataGenerator;

import java.io.File;
import java.io.PrintWriter;

import dataflow.util.DataflowUtils;

public class LogiqlDataflowDataGenerator extends LogiqlDataGenerator {

    private String path;
    private DataflowImpliesLogic logic;
    public LogiqlDataflowDataGenerator(DataflowImpliesLogic logic,
            String path) {
        super(logic.getLogics(), path);
        this.path = path;
        this.logic = logic;
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void writeFile(String output) {
        try {
            String writePath = path + "/"
                    + DataflowUtils.getDataflowValue(logic.getLattice().top)[0]
                    + ".logic";
            File f = new File(writePath);
            PrintWriter pw = new PrintWriter(f);
            pw.write(output);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
