package ontology.solver;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;

import generalconstraintsolver.DecodingTool;

public class OntologySatSolver extends OntologyGeneralSolver {

    @Override
    public OntologySolution solveImpliesLogic(
            OntologyImpliesLogic logic) {
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        //DatatypeLingelingSatSolver solver = new DatatypeLingelingSatSolver(logic, slotManager);
        OntologySatSubSolver solver = new OntologySatSubSolver(logic, slotManager);
        DecodingTool decoder = new DecodingTool(solver.solve(), logic.getLattice());

        for (Integer i : decoder.result.keySet()) {
            result.put(i.intValue(), decoder.result.get(i));
        }

        return new OntologySolution(result);
    }
}
