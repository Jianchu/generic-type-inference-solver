package generalconstraintsolver.dataflowsolver.dataflowsatsolver;

import generalconstraintsolver.DecodingTool;
import generalconstraintsolver.dataflowsolver.DataflowGeneralSolver;
import generalconstraintsolver.dataflowsolver.DataflowImpliesLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataflow.solver.DatatypeSolution;
import dataflow.util.DataflowUtils;

public class DataflowSatSolver extends DataflowGeneralSolver {
    // private DataflowGeneralSerializer serializer;
    // private List<ImpliesLogic> clauses;

    @Override
    public List<DatatypeSolution> solveImpliesLogic(
            List<DataflowImpliesLogic> dataflowLogics) {

        List<DatatypeSolution> datatypeSolutions = new ArrayList<>();

        for (DataflowImpliesLogic logic : dataflowLogics) {
            // Map<Integer, Boolean> idToExistence = new HashMap<>();
            Map<Integer, Boolean> result = new HashMap<>();
            // final Map<Integer, Integer> existentialToPotentialIds =
            // logic.getSerializer()
            // .getExistentialToPotentialVar();
            //DatatypeLingelingSatSolver solver = new DatatypeLingelingSatSolver(logic, slotManager);
            DatatypeSatSolver solver = new DatatypeSatSolver(logic, slotManager);
            DecodingTool decoder = new DecodingTool(solver.dataflowsatSolve(),
                    logic.getLattice());
            for (Integer i : decoder.result.keySet()) {
                String[] datatype = DataflowUtils.getTypeNames(decoder.result.get(i));
                if (datatype[0].equals(DataflowUtils.getTypeNames(logic.getLattice().top)[0])) {
                    result.put(i, true);
                    /*Integer potential = existentialToPotentialIds.get(i.intValue());
                    if (potential != null) {
                        idToExistence.put(potential, false);
                    }*/
                }
            }
            datatypeSolutions
                    .add(new DatatypeSolution(result,
 DataflowUtils.getTypeNames(logic
                    .getLattice().top)[0], false));
        }
        return datatypeSolutions;

    }
}
