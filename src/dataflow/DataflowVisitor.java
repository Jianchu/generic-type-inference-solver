package dataflow;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;

import checkers.inference.InferenceChecker;
import checkers.inference.InferenceVisitor;

public class DataflowVisitor extends InferenceVisitor<DataflowChecker, BaseAnnotatedTypeFactory>{
//    private ProcessingEnvironment processingEnv = checker.getProcessingEnvironment();
//    private ExecutableElement dataflowValue = TreeUtils.getMethod("dataflow.quals.DataFlow", "typeName", 0, processingEnv);
    
    public DataflowVisitor(DataflowChecker checker, InferenceChecker ichecker, BaseAnnotatedTypeFactory factory, boolean infer) {
        super(checker, ichecker, factory, infer);
    }
    
//    @Override
//    public Void visitReturn(ReturnTree node, Void p) {            
//        if (node.getExpression() == null)
//            return super.visitReturn(node, null);
////        AnnotatedTypeMirror expr = getAnnotatedType(tree.getExpression());
//        MethodTree enclosingMethod = TreeUtils.enclosingMethod(getCurrentPath());
//        
//        AnnotatedTypeMirror ret = atypeFactory.getMethodReturnType(enclosingMethod);       
//        AnnotatedTypeMirror treeType = atypeFactory.getAnnotatedType(node);
//        
//        boolean result = checkIsSubtype(treeType, ret);
//        
//        
//        //TODO: inference?
//        return super.visitReturn(node, p);     
//    }
//    
//    
//    private boolean checkIsSubtype(AnnotatedTypeMirror ret, AnnotatedTypeMirror met) {
//        String valueOfret = getDataflowValue(ret);
//        String valueOfmet = getDataflowValue(met);
//        if (valueOfmet.contains(valueOfret)) {
//            return true;
//        }
//        return false;
//        
//               
//    }
//    private String getDataflowValue(AnnotatedTypeMirror type) {
//        return (String) AnnotationUtils.getElementValuesWithDefaults(type.getAnnotation(DataFlow.class)).get(dataflowValue).getValue();
//    }
    
}
