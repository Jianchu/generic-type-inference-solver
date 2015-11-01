package dataflow;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.qual.TypeQualifiers;
import org.checkerframework.javacutil.AnnotationUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

import checkers.inference.BaseInferrableChecker;
import checkers.inference.InferenceChecker;
import checkers.inference.dataflow.InferenceAnalysis;
import checkers.inference.dataflow.InferenceTransfer;
import dataflow.quals.DataFlow;

@TypeQualifiers({ DataFlow.class})
public class DataflowChecker extends BaseInferrableChecker {
    public AnnotationMirror DATAFLOW;
    
    @Override
    public void initChecker() {
        super.initChecker();
        setAnnotations();
    }
    
    protected void setAnnotations() {
        final Elements elements = processingEnv.getElementUtils();
        DATAFLOW = AnnotationUtils.fromClass(elements, DataFlow.class);
    }
    
    @Override
    public DataflowVisitor createVisitor(InferenceChecker ichecker, BaseAnnotatedTypeFactory factory, boolean infer)  {
        return new DataflowVisitor(this, ichecker, factory, infer);
    }

    @Override
    public DataflowAnnotatedTypeFactory createRealTypeFactory() {
        return new DataflowAnnotatedTypeFactory(this);
    }

    @Override
    public CFTransfer createInferenceTransferFunction(InferenceAnalysis analysis) {
        return new InferenceTransfer(analysis);
    }

}
