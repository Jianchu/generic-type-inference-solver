package generalconstrainssolver.logiqlsubsolver;

import generalconstrainssolver.DecodingTool;
import generalconstrainssolver.ImpliesLogic;
import generalconstrainssolver.LatticeGenerator;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.DefaultInferenceSolution;
import checkers.inference.InferenceSolution;


public class LogiqlSubSolver {
    private List<ImpliesLogic> allImpliesLogic;
    private LatticeGenerator lattice;

    public LogiqlSubSolver(List<ImpliesLogic> allImpliesLogic,
            LatticeGenerator lattice) {
        this.allImpliesLogic = allImpliesLogic;
        this.lattice = lattice;

    }

    public InferenceSolution logiqlSolve() {
        Map<Integer, Boolean> idToExistence = new HashMap<>();
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        final String currentPath = new File("").getAbsolutePath();
        File file = new File(currentPath);
        String base = file.getParent().toString();
        String path = base + "/src/checkers/inference/solver/LogicData";
        LogiqlPredicateGenerator LogiqlPredicate = new LogiqlPredicateGenerator(lattice.numModifiers,path);
        LogiqlDataGenerator LogiqlData = new LogiqlDataGenerator(allImpliesLogic, path);
        LogicBloxRunner runLogicBlox = new LogicBloxRunner(path);
        DecodingTool decoder = new DecodingTool(path, lattice,LogiqlData.slotRepresentSet);
        result = decoder.result;
        return new DefaultInferenceSolution(result, idToExistence);
    }
}
