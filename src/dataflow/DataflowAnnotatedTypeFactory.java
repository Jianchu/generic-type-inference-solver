package dataflow;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.AnnotationBuilder;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.InternalUtils;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.TypesUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Attribute.Array;

import dataflow.quals.DataFlow;
import dataflow.quals.DataFlowTop;



public class DataflowAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
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
        
        /**
         * 
         */
        
        @Override
        public Void visitNewClass(NewClassTree node, AnnotatedTypeMirror type) {         
            TypeMirror tm = InternalUtils.typeOf(node);
            String className = TypesUtils.getQualifiedName((DeclaredType)tm).toString();
            AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(convert(className));
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
            if (node.getKind() == Tree.Kind.STRING_LITERAL){
                AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(convert(String.class.toString().split(" ")[1]));
                type.replaceAnnotation(dataFlowType);
            }
            else if (node.getKind() == Tree.Kind.INT_LITERAL){
                AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(convert(int.class.toString()));
                type.replaceAnnotation(dataFlowType);
            }
            if (node.getKind() == Tree.Kind.LONG_LITERAL){
                AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(convert(long.class.toString()));
                type.replaceAnnotation(dataFlowType);
            }
            else if (node.getKind() == Tree.Kind.FLOAT_LITERAL){
                AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(convert(float.class.toString()));
                type.replaceAnnotation(dataFlowType);
            }
            else if (node.getKind() == Tree.Kind.DOUBLE_LITERAL){
                AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(convert(double.class.toString()));
                type.replaceAnnotation(dataFlowType);
            }
            else if (node.getKind() == Tree.Kind.BOOLEAN_LITERAL){
                AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(convert(boolean.class.toString()));
                type.replaceAnnotation(dataFlowType);
            }
            else if (node.getKind() == Tree.Kind.CHAR_LITERAL){
                AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(convert(char.class.toString()));
                type.replaceAnnotation(dataFlowType);
            }            
            return super.visitLiteral(node, type);
        }      
    }

}
