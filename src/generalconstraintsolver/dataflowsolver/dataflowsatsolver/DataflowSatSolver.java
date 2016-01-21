package generalconstraintsolver.dataflowsolver.dataflowsatsolver;

import generalconstraintsolver.DecodingTool;
import generalconstraintsolver.ImpliesLogic;
import generalconstraintsolver.dataflowsolver.DataflowGeneralSerializer;
import generalconstraintsolver.dataflowsolver.DataflowGeneralSolver;
import generalconstraintsolver.dataflowsolver.DataflowImpliesLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataflow.solver.DatatypeSolution;
import dataflow.util.DataflowUtils;

public class DataflowSatSolver extends DataflowGeneralSolver {
    private DataflowGeneralSerializer serializer;
    List<ImpliesLogic> clauses;

    @Override
    public List<DatatypeSolution> solveImpliesLogic(
            List<DataflowImpliesLogic> dataflowLogics) {

        List<DatatypeSolution> Datatypesolutions = new ArrayList<>();

        for (DataflowImpliesLogic logic : dataflowLogics) {
            Map<Integer, Boolean> idToExistence = new HashMap<>();
            Map<Integer, Boolean> result = new HashMap<>();
            final Map<Integer, Integer> existentialToPotentialIds = logic
                    .getSerializer().getExistentialToPotentialVar();
            DatatypeSatSolver solver = new DatatypeSatSolver(logic, slotManager);
            DecodingTool decoder = new DecodingTool(solver.dataflowsatSolve(),
                    logic.getLattice());
            for (Integer i : decoder.result.keySet()) {
                String[] datatype = DataflowUtils
                        .getDataflowValue(decoder.result
                        .get(i));
                if (datatype[0].equals(DataflowUtils.getDataflowValue(logic
                        .getLattice().top)[0])) {
                    result.put(i.intValue(), true);
                    Integer potential = existentialToPotentialIds.get(i
                            .intValue());
                    if (potential != null) {
                        idToExistence.put(potential, false);
                    }
                }
            }
            Datatypesolutions.add(new DatatypeSolution(result, idToExistence,
                    DataflowUtils.getDataflowValue(logic.getLattice().top)[0]));
        }
        return Datatypesolutions;

    }
}
