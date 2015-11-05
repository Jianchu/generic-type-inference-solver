package dataflow;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.AnnotationBuilder;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.TypesUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Attribute.Array;

import dataflow.quals.DataFlow;



public class DataflowAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    protected final AnnotationMirror DATAFLOW;
    protected final AnnotationMirror DATAFLOWBOTTOM;
    private ProcessingEnvironment processingEnv = checker.getProcessingEnvironment();
    private ExecutableElement dataflowValue = TreeUtils.getMethod("dataflow.quals.DataFlow", "typeName", 0, processingEnv);
    public DataflowAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        DATAFLOW = AnnotationUtils.fromClass(elements, DataFlow.class);
        DATAFLOWBOTTOM = createDataflowAnnotation("");
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
    
    private AnnotationMirror createDataflowAnnotation(String dataType) {
        AnnotationBuilder builder =
            new AnnotationBuilder(processingEnv, DataFlow.class);
        builder.setValue("typeName", dataType);
        return builder.build();
    }       
    
    private final class DataFlowQualifierHierarchy extends GraphQualifierHierarchy {

        public DataFlowQualifierHierarchy(MultiGraphFactory f,
                AnnotationMirror bottom) {
            super(f, bottom);
        }

        @Override
        public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
            String[] rhsValue = getDataflowValue(rhs);
            String[] lhsValue = getDataflowValue(lhs);
            Set<String> rSet = new HashSet(Arrays.asList(rhsValue));
            Set<String> lSet = new HashSet(Arrays.asList(lhsValue));

            if (lSet.containsAll(rSet)) {
                return super.isSubtype(rhs, lhs);  
            }
            return false;
        }
        
        private String[] getDataflowValue(AnnotationMirror type) {
            return (String[]) AnnotationUtils.getElementValuesWithDefaults(((AnnotatedTypeMirror) type).getAnnotation(DataFlow.class)).get(dataflowValue).getValue();
        }
    }
 
    public class DataflowTreeAnnotator extends TreeAnnotator {
        
        
        public DataflowTreeAnnotator() {
            super(DataflowAnnotatedTypeFactory.this);
        }
        
        /**
         * If the return type is base case: new, string, primitive:
         */
        
        @Override
        public Void visitNewClass(NewClassTree node, AnnotatedTypeMirror type) {
            AnnotatedTypeMirror t = this.atypeFactory.getAnnotatedType(node);
            String className = TypesUtils.getQualifiedName((DeclaredType)t.getUnderlyingType()).toString();
            //String className = node.getClassBody().getSimpleName().toString();
            AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(className);
            type.replaceAnnotation(dataFlowType);
            return super.visitNewClass(node, type);
                   
        }
        
        @Override
        public Void visitPrimitiveType(PrimitiveTypeTree node, AnnotatedTypeMirror type) {
            String primitiveTypeName = node.getPrimitiveTypeKind().toString();
            AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(primitiveTypeName);
            type.replaceAnnotation(dataFlowType);
            return super.visitPrimitiveType(node, type);
        }
        
        @Override
        public Void visitLiteral(LiteralTree node, AnnotatedTypeMirror type) {
            if (node.getKind() == Tree.Kind.STRING_LITERAL){
                AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(String.class.toString());
                type.replaceAnnotation(dataFlowType);
            }
            else if (node.getKind() == Tree.Kind.INT_LITERAL){
                AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(int.class.toString());
                type.replaceAnnotation(dataFlowType);
            }
            else if (node.getKind() == Tree.Kind.LONG_LITERAL){
                AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(long.class.toString());
                type.replaceAnnotation(dataFlowType);
            }
            else if (node.getKind() == Tree.Kind.FLOAT_LITERAL){
                AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(float.class.toString());
                type.replaceAnnotation(dataFlowType);
            }
            else if (node.getKind() == Tree.Kind.DOUBLE_LITERAL){
                AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(double.class.toString());
                type.replaceAnnotation(dataFlowType);
            }
            else if (node.getKind() == Tree.Kind.BOOLEAN_LITERAL){
                AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(boolean.class.toString());
                type.replaceAnnotation(dataFlowType);
            }
            else if (node.getKind() == Tree.Kind.CHAR_LITERAL){
                AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(char.class.toString());
                type.replaceAnnotation(dataFlowType);
            }            
            return super.visitLiteral(node, type);
        }
        
        @Override
        public Void visitNewArray(NewArrayTree node, AnnotatedTypeMirror type) {
            String arrayType = node.getType().getKind().toString();
            AnnotationMirror dataFlowType = (AnnotationMirror) createDataflowAnnotation(Array.class.toString()+"["+arrayType+"]");
            type.replaceAnnotation(dataFlowType);
            return super.visitNewArray(node, type);
        }                
    }

}
