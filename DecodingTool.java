package checkers.inference.solver.LogicSolver;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.model.Slot;

/**
 * DecodingTool decodes the result from LogicBlox, change the form to human
 * readable form and put the result to HashMap result and return it to
 * LogicSolver.
 * 
 * @author Jianchu Li
 *
 */

public class DecodingTool {
    Map<Integer, AnnotationMirror> result = new HashMap<Integer, AnnotationMirror>();
    Map<String, AnnotationMirror> qualifierName = new HashMap<String, AnnotationMirror>();
    Collection<Slot> slots;
    QualifierHierarchy qualHierarchy;
    private final String path;

    public DecodingTool(Collection<Slot> slots,
            QualifierHierarchy qualHierarchy, String path) {
        this.slots = slots;
        this.qualHierarchy = qualHierarchy;
        this.path = path;
    }

    public Map<Integer, AnnotationMirror> insertToSource() {
        setDefault();
        mapSimpleOriginalName();
        try {
            DecodeLogicBloxOutput();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void mapSimpleOriginalName() {
        for (AnnotationMirror modifier : qualHierarchy.getTypeQualifiers()) {
            qualifierName.put(modifier.toString().replaceAll("[.@]", "_"),
                    modifier);
        }
    }

    /**
     * DecodeLogicBloxOutput decodes the LogicBloxOutput, and put it in HashMap
     * result.
     * 
     * @throws FileNotFoundException
     */
    private void DecodeLogicBloxOutput() throws FileNotFoundException {
        String readPath = path + "/logicbloxOutput.txt";
        InputStream in = new FileInputStream(readPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                String[] s = line.replaceAll("\"", "").split(" ");
                int slotID = Integer.parseInt(s[0]);
                AnnotationMirror annotation = qualifierName
                        .get(s[s.length - 1]);
                result.put(slotID, annotation);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * in the beginning, set every slot's modifier be the top modifier of
     * current type system.
     */
    private void setDefault() {
        AnnotationMirror topQualifier = getTopQualifier();
        for (int i = 0; i < slots.size(); i++) {
            result.put(i, topQualifier);
        }
    }

    private AnnotationMirror getTopQualifier() {
        AnnotationMirror topQualifier = null;
        for (AnnotationMirror i : qualHierarchy.getTopAnnotations()) {
            topQualifier = i;
        }
        return topQualifier;
    }
}
