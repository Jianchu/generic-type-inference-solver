package checkers.inference.solver.LogiqlDebugSolver;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.inference.InferenceSolver;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;

/**
 * 
 *
 * @author Jianchu Li
 *
 */

public class LogicSolver implements InferenceSolver {

    @Override
    public Map<Integer, AnnotationMirror> solve(
            Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {
        Map<Integer, AnnotationMirror> result = new HashMap<Integer, AnnotationMirror>();
        final String currentPath = new File("").getAbsolutePath();
        File file = new File(currentPath);
        String base = file.getParent().toString();
        String path = base + "/src/checkers/inference/solver/LogiqlDebugSolver";

        LogiqlConstraintGenerator constraintGenerator = new LogiqlConstraintGenerator(
                qualHierarchy, path);
        try {
            constraintGenerator.GenerateLogiqlEncoding();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LogiqlDataGenerator dataGenerator = new LogiqlDataGenerator(slots,
                constraints, path);
        dataGenerator.GenerateLogiqlData();

        LogicBloxRunner runLogicBlox = new LogicBloxRunner(path);
        try {
            runLogicBlox.runLogicBlox();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DecodingTool DecodeTool = new DecodingTool(slots, qualHierarchy, path);
        result = DecodeTool.insertToSource();
        for (int i : result.keySet()) {
            System.out.println("Slot ID: " + i + "  Annotation: "
                    + result.get(i).toString());
        }
        return result;
    }

}

