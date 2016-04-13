package generalconstraintsolver.logiqlsubsolver;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.DefaultInferenceSolution;
import checkers.inference.InferenceSolution;
import generalconstraintsolver.DecodingTool;
import generalconstraintsolver.ImpliesLogic;
import generalconstraintsolver.LatticeGenerator;


public class LogiqlSubSolver {
    private final List<ImpliesLogic> allImpliesLogic;
    private final LatticeGenerator lattice;

    public LogiqlSubSolver(List<ImpliesLogic> allImpliesLogic,
            LatticeGenerator lattice) {
        this.allImpliesLogic = allImpliesLogic;
        this.lattice = lattice;
    }

    public InferenceSolution logiqlSolve() {
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        final String currentPath = new File("").getAbsolutePath();
        File file = new File(currentPath);
        String base = file.getParent().toString();
        String path = base
                + "/src/generalconstraintsolver/logiqlsubsolver/logicdata";
        // LogiqlPredicateGenerator logiqlPredicate =
        new LogiqlPredicateGenerator(lattice.numModifiers, path);
        LogiqlDataGenerator logiqlData = new LogiqlDataGenerator(allImpliesLogic, path);
        logiqlData.generateData();
        LogicBloxRunner runLogicBlox = new LogicBloxRunner(path);
        try {
            runLogicBlox.runLogicBlox();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DecodingTool decoder = new DecodingTool(path, lattice, logiqlData.slotRepresentSet);
        result = decoder.result;
        return new DefaultInferenceSolution(result);
    }
}
