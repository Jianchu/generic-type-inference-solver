package dataflow;

import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceTreeAnnotator;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.VariableAnnotator;
import checkers.inference.model.ConstantSlot;

import com.sun.source.tree.LiteralTree;
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
        AnnotationMirror anno = DataflowUtils.generateDataflowAnnoFromLiteral(literalTree, atm, this.realTypeFactory.getProcessingEnv());
        variableAnnotator.createConstant(anno, literalTree);
        variableAnnotator.visit(atm, literalTree);
        return null;
    }

    @Override
    public Void visitNewClass(final NewClassTree newClassTree, final AnnotatedTypeMirror atm) {
        // System.out.println("New class Tree: " + newClassTree.toString());
        AnnotationMirror anno = DataflowUtils.genereateDataflowAnnoFromNewClass(newClassTree, atm, this.realTypeFactory.getProcessingEnv());
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

}
