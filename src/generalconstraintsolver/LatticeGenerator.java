package generalconstraintsolver;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import dataflow.util.DataflowUtils;

public class LatticeGenerator {
    
    public ProcessingEnvironment processingEnv;
    public QualifierHierarchy qualHierarchy;
    public Map<AnnotationMirror, Collection<AnnotationMirror>> subType = AnnotationUtils.createAnnotationMap();
    public Map<AnnotationMirror, Collection<AnnotationMirror>> superType = AnnotationUtils.createAnnotationMap();
    public Map<AnnotationMirror, Collection<AnnotationMirror>> notComparableType = AnnotationUtils.createAnnotationMap();
    public Map<AnnotationMirror, Integer> modifierInt = AnnotationUtils.createAnnotationMap();
    public Map<Integer,AnnotationMirror> IntModifier = new HashMap<Integer,AnnotationMirror>();
    public Set<? extends AnnotationMirror> allTypes;
    public AnnotationMirror top;
    public AnnotationMirror bottom;
    public int numModifiers;

    public LatticeGenerator(QualifierHierarchy qualHierarchy) {
        this.qualHierarchy = qualHierarchy;
        this.allTypes = qualHierarchy.getTypeQualifiers();
        this.top = qualHierarchy.getTopAnnotations().iterator().next();
        this.bottom = qualHierarchy.getBottomAnnotations().iterator().next();
        this.numModifiers = qualHierarchy.getTypeQualifiers().size();
        getSubSupertype();
        getNotComparable();
//        for (AnnotationMirror i : subType.keySet()){
//            System.out.println("Key: " + i.toString() + "   Value: "+ subType.get(i).toString());
//        }
        //System.out.println(allTypes.toString());
        
    }
    
    public LatticeGenerator(AnnotationMirror dataflowAnnotation,ProcessingEnvironment processingEnv){
        this.processingEnv = processingEnv;
        this.top = dataflowAnnotation;
        this.bottom = DataflowUtils.createDataflowAnnotation(new HashSet<String>(Arrays.asList("")), processingEnv);
        this.numModifiers = 2;
        addAlltypesFor2();
        getSubSupertypeFor2();
        //TODO:
        
        
    }
    
    private void addAlltypesFor2() {
        Set<AnnotationMirror> all2Types = new HashSet<AnnotationMirror>();
        all2Types.add(this.top);
        all2Types.add(this.bottom);
        this.allTypes = all2Types;
    }

    private void getSubSupertypeFor2(){
        int num = 1;
        for (AnnotationMirror i :allTypes){
            Set<AnnotationMirror> subtypeSet = new HashSet<AnnotationMirror>();
            Set<AnnotationMirror> supertypeSet = new HashSet<AnnotationMirror>();
            if (AnnotationUtils.areSame(i, this.top)){
                subtypeSet.add(this.top);
                subtypeSet.add(this.bottom);
                supertypeSet.add(this.top);
            }
            
            else if (AnnotationUtils.areSame(i, this.bottom)){
                subtypeSet.add(this.bottom);
                supertypeSet.add(this.bottom);
                supertypeSet.add(this.top);
            }
            this.subType.put(i, subtypeSet);
            this.superType.put(i, supertypeSet);
            this.modifierInt.put(i, num);
            this.IntModifier.put(num, i);
            num++;
            
        }        
    }
   
    private void getSubSupertype() {
        int num = 1;
        for (AnnotationMirror i : allTypes) {
            Set<AnnotationMirror> subtypeFori = new HashSet<AnnotationMirror>();
            Set<AnnotationMirror> supertypeFori = new HashSet<AnnotationMirror>();
            for (AnnotationMirror j : allTypes) {
                if (qualHierarchy.isSubtype(j, i)) {
                    subtypeFori.add(j);
                }
                if (qualHierarchy.isSubtype(i, j)) {
                    supertypeFori.add(j);
                }
            }
            subType.put(i, subtypeFori);
            superType.put(i, supertypeFori);
            modifierInt.put(i, num);
            IntModifier.put(num, i);
            num++;
        }
//        for (Integer j: IntModifier.keySet()){
//            System.out.println("final key "+j+ "  " + "final value: " + IntModifier.get(j).toString());
//        }
    }

    private void getNotComparable() {
        for (AnnotationMirror i : allTypes) {
            Set<AnnotationMirror> notComparableFori = new HashSet<AnnotationMirror>();
            for (AnnotationMirror j : allTypes) {
                if (!subType.get(i).contains(j)
                        && !subType.get(j).contains(i)) {
                    notComparableFori.add(j);
                }
            }
            if (!notComparableFori.isEmpty()) {
                notComparableType.put(i, notComparableFori);
            }
        }
    }
}

