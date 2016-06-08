package dataflow;

import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.TreeUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceTreeAnnotator;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.VariableAnnotator;
import checkers.inference.model.ConstantSlot;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;

import dataflow.util.DataflowUtils;

public class DataflowInferenceTreeAnnotator extends InferenceTreeAnnotator {

    // private final SlotManager slotManager;
    private final VariableAnnotator variableAnnotator;
    private final AnnotatedTypeFactory realTypeFactory;
    // private final InferrableChecker realChecker;

    public DataflowInferenceTreeAnnotator(
            InferenceAnnotatedTypeFactory atypeFactory,
            InferrableChecker realChecker,
            AnnotatedTypeFactory realAnnotatedTypeFactory,
            VariableAnnotator variableAnnotator, SlotManager slotManager) {
        super(atypeFactory, realChecker, realAnnotatedTypeFactory,
                variableAnnotator, slotManager);

        // this.slotManager = slotManager;
        this.variableAnnotator = variableAnnotator;
        this.realTypeFactory = realAnnotatedTypeFactory;
        // this.realChecker = realChecker;
    }

    @Override
    public Void visitLiteral(final LiteralTree literalTree, final AnnotatedTypeMirror atm) {
        AnnotationMirror anno = DataflowUtils.generateDataflowAnnoFromLiteral(literalTree, this.realTypeFactory.getProcessingEnv());
        variableAnnotator.createConstant(anno, literalTree);
        variableAnnotator.visit(atm, literalTree);
        return null;
    }

    @Override
    public Void visitNewClass(final NewClassTree newClassTree, final AnnotatedTypeMirror atm) {
        // System.out.println("New class Tree: " + newClassTree.toString());
        TypeMirror tm = atm.getUnderlyingType();
        ((DataflowAnnotatedTypeFactory) this.realTypeFactory).getTypeNameMap().put(tm.toString(), tm);
        AnnotationMirror anno = DataflowUtils.genereateDataflowAnnoFromNewClass(atm, this.realTypeFactory.getProcessingEnv());
        ConstantSlot cs = variableAnnotator.createConstant(anno, newClassTree);
        atm.replaceAnnotation(cs.getValue());
        variableAnnotator.visit(atm, newClassTree.getIdentifier());
        return null;
    }

    @Override
    public Void visitParameterizedType(final ParameterizedTypeTree param, final AnnotatedTypeMirror atm) {
        TreePath path = atypeFactory.getPath(param);
        if (path != null) {
            final TreePath parentPath = path.getParentPath();
            final Tree parentNode = parentPath.getLeaf();
            if (!parentNode.getKind().equals(Kind.NEW_CLASS)) {
                variableAnnotator.visit(atm, param);
            }
        }
        return null;
    }
    
    @Override
    public Void visitMethodInvocation(MethodInvocationTree methodInvocationTree, final AnnotatedTypeMirror atm) {
        ExecutableElement methodElement = TreeUtils.elementFromUse(methodInvocationTree);
        boolean isBytecode = ElementUtils.isElementFromByteCode(methodElement);
        if (isBytecode) {
            AnnotationMirror anno = DataflowUtils.genereateDataflowAnnoFromByteCode(atm, this.realTypeFactory.getProcessingEnv());
            ConstantSlot cs = variableAnnotator.createConstant(anno, methodInvocationTree);
            atm.replaceAnnotation(cs.getValue());
            variableAnnotator.visit(atm, methodInvocationTree.getMethodSelect());
            return null;
        } else {
            return super.visitMethodInvocation(methodInvocationTree, atm);
        }
    }

}
