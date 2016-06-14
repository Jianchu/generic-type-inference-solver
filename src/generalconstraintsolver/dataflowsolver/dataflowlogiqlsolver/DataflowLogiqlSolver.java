package generalconstraintsolver.dataflowsolver.dataflowlogiqlsolver;

import generalconstraintsolver.dataflowsolver.DataflowGeneralSolver;
import generalconstraintsolver.dataflowsolver.DataflowImpliesLogic;
import generalconstraintsolver.logiqlsubsolver.LogiqlPredicateGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataflow.solver.DatatypeSolution;
import dataflow.util.DataflowUtils;

public class DataflowLogiqlSolver extends DataflowGeneralSolver {
    // private DataflowGeneralSerializer serializer;
    private Map<String, DataflowImpliesLogic> fileName_logic = new HashMap<String, DataflowImpliesLogic>();

    @Override
    public List<DatatypeSolution> solveImpliesLogic(
            List<DataflowImpliesLogic> dataflowLogics) {
        // generate the path of logiql files
        final String currentPath = new File("").getAbsolutePath();
        File file = new File(currentPath);
        String base = file.getParent().toString();
        String path = base
                + "/src/generalconstraintsolver/dataflowsolver/logicdata";
        String dataPath = path + "/data";
        String logibloxoutputPath = path + "/logibloxoutput";
        createDir(path);
        createDir(dataPath);
        createDir(logibloxoutputPath);
        // generate logiql tables

        // LogiqlPredicateGenerator LogiqlPredicate =
        new LogiqlPredicateGenerator(2, path);
        for (DataflowImpliesLogic logic : dataflowLogics) {
            // generate logiql data
            LogiqlDataflowDataGenerator LogiqlData = new LogiqlDataflowDataGenerator(
                    logic, path + "/data");
            LogiqlData.generateData();
            fileName_logic.put(
DataflowUtils.getTypeNames(logic.getLattice().top)[0],
                    logic);
        }
        runLogicBlox(path);
        return decodeLogiqlResult(path);
    }

    private void createDir(String path) {
        File dirPath = new File(path);
        dirPath.mkdir();
    }

    private List<DatatypeSolution> decodeLogiqlResult(String path) {
        final File folder = new File(path + "/logibloxoutput");
        List<DatatypeSolution> Datatypesolutions = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            // Map<Integer, Boolean> idToExistence = new HashMap<>();
            Map<Integer, Boolean> result = new HashMap<>();
            /*
            final Map<Integer, Integer> existentialToPotentialIds = fileName_logic
                    .get(fileEntry.getName().toString()).getSerializer()
                    .getExistentialToPotentialVar();
             */
            if (fileEntry.isFile()) {
                DataflowDecodingTool decoder = new DataflowDecodingTool(
                        fileEntry.getAbsolutePath(),
                        fileName_logic.get(fileEntry.getName().toString()).getLattice());
                try {
                    decoder.decodeLogicBloxResult();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                for (Integer i : decoder.result.keySet()) {
                    String[] datatype = DataflowUtils
.getTypeNames(decoder.result.get(i));
                    if (datatype[0].equals(fileEntry.getName().toString())) {
                        result.put(i.intValue(), true);
                        /*
                        Integer potential = existentialToPotentialIds.get(i.intValue());
                        if (potential != null) {
                            idToExistence.put(potential, false);
                        }*/
                    }
                }
                Datatypesolutions
                        .add(new DatatypeSolution(result, fileEntry.getName().toString(), false));
            }
        }
        return Datatypesolutions;
    }

    private void runLogicBlox(String path) {
        LogicBloxRunnerForDataflow runner = new LogicBloxRunnerForDataflow(path);
        try {
            runner.logiqlSolve();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
