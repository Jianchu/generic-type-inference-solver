package ontology;

import ontology.util.OntologyUtils;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;

import checkers.inference.ConstraintManager;
import checkers.inference.InferenceChecker;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceVisitor;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.PreferenceConstraint;
import checkers.inference.model.VariableSlot;

import com.sun.source.tree.VariableTree;

public class OntologyVisitor extends InferenceVisitor<OntologyChecker, BaseAnnotatedTypeFactory> {

    private final OntologyAnnotatedTypeFactory ontologyATF;
    
    public OntologyVisitor(OntologyChecker checker, InferenceChecker ichecker, BaseAnnotatedTypeFactory factory,
            boolean infer) {
        super(checker, ichecker, factory, infer);
        this.ontologyATF = (OntologyAnnotatedTypeFactory) InferenceMain.getInstance().getRealTypeFactory();
    }

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node);
        ConstraintManager cManager = InferenceMain.getInstance().getConstraintManager();
        SlotManager sManager = InferenceMain.getInstance().getSlotManager();
        VariableSlot vSlot = sManager.getVariableSlot(type);
        if (OntologyUtils.isSequence(type.getUnderlyingType())) {
            cManager.add(new PreferenceConstraint(vSlot, new ConstantSlot(ontologyATF.SEQ, sManager.nextId()), 50));
        }
        return super.visitVariable(node, p);
    }
}
