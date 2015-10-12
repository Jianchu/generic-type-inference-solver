package checkers.inference.solver;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;

public class LogiqlSubSolver {
    private List<ImpliesLogic> allImpliesLogic;
    private LatticeGenerator lattice;

    public LogiqlSubSolver(List<ImpliesLogic> allImpliesLogic,
            LatticeGenerator lattice) {
        this.allImpliesLogic = allImpliesLogic;
        this.lattice = lattice;
        logiqlSolve();
    }

    private void logiqlSolve() {
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
        System.out.println("/**************result from Logiql Solver*******************/");
        for (Integer j: result.keySet()){
            System.out.println("SlotID: "+j+ "  " + "Annotation: " + result.get(j).toString());
        }
        System.out.flush();
        System.out.println("/**********************************************************/");
    }
}
