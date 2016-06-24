package dataflow;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedPrimitiveType;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;

import checkers.inference.ConstraintManager;
import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceChecker;
import checkers.inference.InferenceMain;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstantSlot;
import dataflow.util.DataflowUtils;

public class DataflowInferenceAnnotatedTypeFactory extends InferenceAnnotatedTypeFactory {

    public DataflowInferenceAnnotatedTypeFactory(InferenceChecker inferenceChecker,
            boolean withCombineConstraints, BaseAnnotatedTypeFactory realTypeFactory,
            InferrableChecker realChecker, SlotManager slotManager, ConstraintManager constraintManager) {
        super(inferenceChecker, withCombineConstraints, realTypeFactory, realChecker, slotManager,
                constraintManager);
    }

    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(new ImplicitsTreeAnnotator(this),
                new DataflowInferenceTreeAnnotator(this, realChecker, realTypeFactory, variableAnnotator,
                        slotManager));
    }

    @Override
    public AnnotatedDeclaredType getBoxedType(AnnotatedPrimitiveType type) {
        TypeElement typeElt = types.boxedClass(type.getUnderlyingType());
        AnnotationMirror am = createDataflow(typeElt.asType().toString(), this.processingEnv);
        AnnotatedDeclaredType dt = fromElement(typeElt);
        ConstantSlot cs = new ConstantSlot(am, InferenceMain.getInstance().getSlotManager().nextId());
        InferenceMain.getInstance().getSlotManager().addVariable(cs);
        dt.addAnnotation(InferenceMain.getInstance().getSlotManager().getAnnotation(cs));
        dt.addAnnotation(cs.getValue());
        return dt;
    }

    @Override
    public AnnotatedPrimitiveType getUnboxedType(AnnotatedDeclaredType type)
            throws IllegalArgumentException {
        PrimitiveType primitiveType = types.unboxedType(type.getUnderlyingType());
        AnnotationMirror am = createDataflow(primitiveType.toString(), this.processingEnv);
        AnnotatedPrimitiveType pt = (AnnotatedPrimitiveType) AnnotatedTypeMirror.createType(
                primitiveType, this, false);
        ConstantSlot cs = new ConstantSlot(am, InferenceMain.getInstance().getSlotManager().nextId());
        InferenceMain.getInstance().getSlotManager().addVariable(cs);
        pt.addAnnotation(InferenceMain.getInstance().getSlotManager().getAnnotation(cs));
        pt.addAnnotation(cs.getValue());
        return pt;
    }

    private AnnotationMirror createDataflow(String typeName, ProcessingEnvironment processingEnv) {
        Set<String> typeNames = new HashSet<String>();
        typeNames.add(typeName);
        AnnotationMirror am = DataflowUtils.createDataflowAnnotation(typeNames, processingEnv);
        return am;
    }

}
