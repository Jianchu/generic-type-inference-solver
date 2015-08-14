package checkers.inference.solver.LogiqlDebugSolver;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
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
            Map<String, String> configuration,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {
        Map<Integer, AnnotationMirror> result = new HashMap<Integer, AnnotationMirror>();
        final String currentPath = new File("").getAbsolutePath();
        
        LogiqlConstraintGenerator constraintGenerator = new LogiqlConstraintGenerator(qualHierarchy);
        try {
            constraintGenerator.GenerateLogiqlEncoding();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        LogiqlDataGenerator dataGenerator = new LogiqlDataGenerator(slots,constraints);
        dataGenerator.GenerateLogiqlData();

        LogicBloxRunner runLogicBlox = new LogicBloxRunner(currentPath);
        try {
            runLogicBlox.runLogicBlox();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        InsertionTool insertTool = new InsertionTool (slots, qualHierarchy);
        result = insertTool.insertToSource();
        for (int i : result.keySet()){
            System.out.println("Slot ID: "+ i + "  Annotation: " + result.get(i).toString());
        }
        
        return result;
    }

}
