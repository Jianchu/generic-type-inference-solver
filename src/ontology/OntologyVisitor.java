package ontology;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;

import javax.lang.model.element.AnnotationMirror;

import com.sun.source.tree.VariableTree;

import checkers.inference.ConstraintManager;
import checkers.inference.InferenceChecker;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceVisitor;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.PreferenceConstraint;
import checkers.inference.model.VariableSlot;
import ontology.util.OntologyUtils;

public class OntologyVisitor extends InferenceVisitor<OntologyChecker, BaseAnnotatedTypeFactory> {

    public OntologyVisitor(OntologyChecker checker, InferenceChecker ichecker, BaseAnnotatedTypeFactory factory,
            boolean infer) {
        super(checker, ichecker, factory, infer);
    }

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node);
        ConstraintManager cManager = InferenceMain.getInstance().getConstraintManager();
        SlotManager sManager = InferenceMain.getInstance().getSlotManager();
        VariableSlot vSlot = sManager.getVariableSlot(type);
        AnnotationMirror anno = OntologyUtils.determineAnnotation(elements, type.getUnderlyingType());
        if (anno != null) {
            // JLTODO: Is it good to use 'new ConstantSlot' here instead of the
            // 'variableAnnotator.createConstant' used in the ATF?
            cManager.add(new PreferenceConstraint(vSlot, new ConstantSlot(anno, sManager.nextId()), 50));
        }
        return super.visitVariable(node, p);
    }
}
