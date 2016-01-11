package dataflow;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.AnnotationBuilder;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferrableAnnotatedTypeFactory;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.VariableAnnotator;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.PrimitiveTypeTree;

import dataflow.quals.DataFlow;
import dataflow.quals.DataFlowTop;
import dataflow.util.DataflowUtils;



public class DataflowAnnotatedTypeFactory extends BaseAnnotatedTypeFactory
        implements InferrableAnnotatedTypeFactory {
    protected final AnnotationMirror DATAFLOW, DATAFLOWBOTTOM, DATAFLOWTOP;
//  private ProcessingEnvironment processingEnv = checker.getProcessingEnvironment();
    private ExecutableElement dataflowValue = TreeUtils.getMethod("dataflow.quals.DataFlow", "typeNames", 0, processingEnv);
    
    //cannot use DataFlow.class.toString(), the string would be "interface dataflow.quals.DataFlow"
    //private ExecutableElement dataflowValue = TreeUtils.getMethod(DataFlow.class.toString(), "typeNames", 0, processingEnv);
    public DataflowAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        DATAFLOW = AnnotationUtils.fromClass(elements, DataFlow.class);
        DATAFLOWBOTTOM = createDataflowAnnotation(convert(""));
        DATAFLOWTOP = AnnotationUtils.fromClass(elements, DataFlowTop.class);
        postInit();
    }
    
    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
                super.createTreeAnnotator(),
                new DataflowTreeAnnotator()
        );
    }
    
    protected TreeAnnotator getDataflowTreeAnnotator(){
        return new DataflowTreeAnnotator();
    }

    @Override
    public TreeAnnotator getInferenceTreeAnnotator(
            InferenceAnnotatedTypeFactory atypeFactory,
            InferrableChecker realChecker,
            VariableAnnotator variableAnnotator, SlotManager slotManager) {
        return new ListTreeAnnotator(new ImplicitsTreeAnnotator(this),
                new DataflowInferenceTreeAnnotator(atypeFactory, realChecker,
                        this, variableAnnotator, slotManager));
    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new DataFlowQualifierHierarchy(factory, DATAFLOWBOTTOM);
    }
    
    private AnnotationMirror createDataflowAnnotation(String[] dataType) {
        AnnotationBuilder builder =
            new AnnotationBuilder(processingEnv, DataFlow.class);
        builder.setValue("typeNames", dataType);
        return builder.build();
    }
    
    private String[] convert (String... typeName){
        return typeName;
    }
    
    private final class DataFlowQualifierHierarchy extends GraphQualifierHierarchy {

        public DataFlowQualifierHierarchy(MultiGraphFactory f,
                AnnotationMirror bottom) {
            super(f, bottom);
        }

        @Override
        public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
//           System.out.println("left hand side:"+lhs);
//           System.out.println("right hand side:"+rhs);            
            if (AnnotationUtils.areSameIgnoringValues(rhs, DATAFLOW)
                    && AnnotationUtils.areSameIgnoringValues(lhs, DATAFLOW)){
              String[] rhsValue = getDataflowValue(rhs);
              String[] lhsValue = getDataflowValue(lhs);
              Set<String> rSet = new HashSet<String>(Arrays.asList(rhsValue));
              Set<String> lSet = new HashSet<String>(Arrays.asList(lhsValue));
              if (lSet.containsAll(rSet)) {
                  return true;
              } else{
                  return false;
              }                
            } else {
                //if (rhs != null && lhs != null)
                if (AnnotationUtils.areSameIgnoringValues(rhs, DATAFLOW)){
                    rhs = DATAFLOW;
                }
                else if (AnnotationUtils.areSameIgnoringValues(lhs, DATAFLOW)){
                    lhs = DATAFLOW;
                }
                return super.isSubtype(rhs, lhs);
            }
        }
        
        
        private String[] getDataflowValue(AnnotationMirror type) {
            List<String> allTypesList = (List<String>) AnnotationUtils.getElementValuesWithDefaults(type).get(dataflowValue).getValue();
            //types in this list is org.checkerframework.framework.util.AnnotationBuilder.
            String[] allTypesInArray = new String[allTypesList.size()];
            int i = 0;
            for (Object o :allTypesList){
                allTypesInArray[i] = o.toString();
                i++;
                //System.out.println(o.toString()); 
            }
            return allTypesInArray;
        }
    }
 
    public class DataflowTreeAnnotator extends TreeAnnotator {
        
        
        public DataflowTreeAnnotator() {
            super(DataflowAnnotatedTypeFactory.this);
        }

        @Override
        public Void visitNewClass(NewClassTree node, AnnotatedTypeMirror type) {
            AnnotationMirror dataFlowType = DataflowUtils
                    .genereateDataflowAnnoFromNewClass(node, type,
                    processingEnv);
            type.replaceAnnotation(dataFlowType);
            return super.visitNewClass(node, type);                   
        }
        
        @Override
        public Void visitPrimitiveType(PrimitiveTypeTree node, AnnotatedTypeMirror type) {
            String primitiveTypeName = node.getPrimitiveTypeKind().toString();
            AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(convert(primitiveTypeName));
            type.replaceAnnotation(dataFlowType);
            return super.visitPrimitiveType(node, type);
        }

        @Override
        public Void visitLiteral(LiteralTree node, AnnotatedTypeMirror type) {
            AnnotationMirror dataFlowType = DataflowUtils
                    .generateDataflowAnnoFromLiteral(node, type, processingEnv);
            type.replaceAnnotation(dataFlowType);
            return super.visitLiteral(node, type);
        }
    }
}
