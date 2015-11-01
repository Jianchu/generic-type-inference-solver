package dataflow;

import org.checkerframework.checker.regex.classic.qual.PartialRegex;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

import trusted.TrustedChecker;

import com.sun.source.tree.ReturnTree;

import dataflow.quals.DataFlow;



public class DataflowAnnotatedTypeFactory extends BaseAnnotatedTypeFactory{

    private final ExecutableElement dataflowValue;
    
    public DataflowAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        dataflowValue = TreeUtils.getMethod("dataflow.quals.DataFlow", "typeName", 0, processingEnv);
        postInit();
        // TODO Auto-generated constructor stub
    }
    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
                super.createTreeAnnotator(),
                new DataflowTreeAnnotator()
        );
    }
 
    public class DataflowTreeAnnotator extends TreeAnnotator{
        
        
        public DataflowTreeAnnotator() {
            super(DataflowAnnotatedTypeFactory.this);
        }
        
        /**
         * If the return type is base case: new, string, primitive:
         */
        
        @Override
        public Void visitReturn(ReturnTree tree, AnnotatedTypeMirror type){            
            if (tree.getExpression() == null)
                return super.visitReturn(tree, null);
            AnnotatedTypeMirror expr = getAnnotatedType(tree.getExpression());
            final DataflowChecker dataflowChecker = (DataflowChecker) checker;
            
            
            
            return DEFAULT_VALUE; 
            
        }
        private String getDataflowValue(AnnotatedTypeMirror type) {
            return (String) AnnotationUtils.getElementValuesWithDefaults(type.getAnnotation(DataFlow.class)).get(dataflowValue).getValue();
        }
        
        private AnnotationMirror createDataflowAnnotation(String dataType) {
            AnnotationBuilder builder =
                new AnnotationBuilder(processingEnv, PartialRegex.class);
            builder.setValue("typeName", dataType);
            return builder.build();
        }       
    }

}
