package ontology.solver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;

import generalconstraintsolver.DecodingTool;
import generalconstraintsolver.ImpliesLogic;
import generalconstraintsolver.LatticeGenerator;

public class OntologySatSolver extends OntologyGeneralSolver {

    @Override
    public OntologySolution solveImpliesLogic(LatticeGenerator lattice,
            List<ImpliesLogic> logic) {
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        //DatatypeLingelingSatSolver solver = new DatatypeLingelingSatSolver(logic, slotManager);
        OntologySatSubSolver solver = new OntologySatSubSolver(lattice, logic, slotManager);
        DecodingTool decoder = new DecodingTool(solver.solve(), lattice);

        for (Integer i : decoder.result.keySet()) {
            result.put(i.intValue(), decoder.result.get(i));
        }

        return new OntologySolution(result);
    }
}
