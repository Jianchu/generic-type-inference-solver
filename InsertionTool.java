package checkers.inference.solver.LogiqlDebugSolver;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.io.BufferedReader;
import java.io.File;
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
 * 
 *
 * @author Jianchu Li
 *
 */

public class InsertionTool{
    Map<Integer, AnnotationMirror> result = new HashMap<Integer, AnnotationMirror>();
    Map<String, AnnotationMirror> qualifierName = new HashMap<String, AnnotationMirror>();
    Collection<Slot> slots;
    QualifierHierarchy qualHierarchy;
    private final String currentPath = new File("").getAbsolutePath();     
    
    public InsertionTool(Collection<Slot> slots,QualifierHierarchy qualHierarchy){
        this.slots = slots;
        this.qualHierarchy = qualHierarchy;
    }
    
    public  Map<Integer, AnnotationMirror> insertToSource() {
        setDefault();
        mapSimpleOriginalName();
        try {
            DecodeLogicBloxOutput();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    private void mapSimpleOriginalName(){
        for (AnnotationMirror modifier : qualHierarchy.getTypeQualifiers()){
            qualifierName.put(modifier.toString().replaceAll("[.@]", "_"), modifier);
        }
    }
    
    private void DecodeLogicBloxOutput() throws FileNotFoundException  {
        File file = new File(currentPath);
        String Base = file.getParent().toString();
        String Path = Base + "/src/checkers/inference/solver/LogiqlDebugSolver";
        String readPath = Path + "/logicbloxOutput.txt";
        InputStream in = new FileInputStream(readPath);
        BufferedReader reader = new BufferedReader (new InputStreamReader(in));
        String line = null;
        try {
            while ((line = reader.readLine()) != null){
                String[] s = line.replaceAll("\"", "").split(" ");
                int slotID = Integer.parseInt(s[0]);
                AnnotationMirror annotation =qualifierName.get(s[s.length-1]);
                result.put(slotID, annotation);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }                       
    }

    private void setDefault(){
        AnnotationMirror topQualifier = getTopQualifier();
        for (int i = 0; i < slots.size(); i++) {
            result.put(i, topQualifier);
        }
    }
    
    private AnnotationMirror getTopQualifier(){
        AnnotationMirror topQualifier = null;
        for (AnnotationMirror i : qualHierarchy.getTopAnnotations()){
             topQualifier = i;
        }
        return topQualifier; 
    }
}