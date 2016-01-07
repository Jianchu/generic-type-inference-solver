package logicsolver;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.inference.InferenceSolution;
import checkers.inference.InferenceSolver;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;

/**
 * LogicSolver InferenceSolver and return a HashMap contains solved constraint.
 *
 * @author Jianchu Li
 *
 */

public class LogicSolver implements InferenceSolver {

    @Override
    public InferenceSolution solve(
            Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {
        /**
         * result is the HashMap that will be returned. path is the location of
         * LogiqlDebugSolver folder.
         */
        Map<Integer, AnnotationMirror> result = new HashMap<Integer, AnnotationMirror>();
        final String currentPath = new File("").getAbsolutePath();
        File file = new File(currentPath);
        String base = file.getParent().toString();
        String path = base + "/LogicData";

        /**
         * creating a instance of LogiqlConstraintGenerator and running
         * GenerateLogiqlEncoding method, in order to generate the logiql fixed
         * encoding part of current type system.
         */
        LogiqlConstraintGenerator constraintGenerator = new LogiqlConstraintGenerator(
                qualHierarchy, path);
            constraintGenerator.GenerateLogiqlEncoding();

        /**
         * creating a instance of dataGenerator and running GenerateLogiqlData
         * method, in order to generate the logiql encoding for current input
         * program.
         */
        LogiqlDataGenerator dataGenerator = new LogiqlDataGenerator(slots,
                constraints, path);
        dataGenerator.generateLogiqlData();

        /**
         * creating a instance of LogicBloxRunner and running runLogicBlox
         * method, in order to send all the encoding to LogicBlox, and get the
         * result from LogicBlox.
         */
        LogicBloxRunner runLogicBlox = new LogicBloxRunner(path);
            runLogicBlox.runLogicBlox();

        /**
         * creating a instance of DecodeTool and running insertToSource method,
         * in order to decode the result from LogicBlox, and put the result to
         * HashMap result.
         */
        DecodingTool DecodeTool = new DecodingTool(slots, qualHierarchy, path);
        result = DecodeTool.insertToSource();
        for (int i : result.keySet()) {
            System.out.println("Slot ID: " + i + "  Annotation: "
                    + result.get(i).toString());
        }
        return (InferenceSolution) result;
    }
}