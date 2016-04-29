package ontology;

import ontology.util.OntologyUtils;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.InferenceChecker;
import checkers.inference.InferenceVisitor;

import com.sun.source.tree.VariableTree;

public class OntologyVisitor extends InferenceVisitor<OntologyChecker, BaseAnnotatedTypeFactory> {

    public OntologyVisitor(OntologyChecker checker, InferenceChecker ichecker, BaseAnnotatedTypeFactory factory,
            boolean infer) {
        super(checker, ichecker, factory, infer);
    }

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        int weight = 50;
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node);
        AnnotationMirror anno = OntologyUtils.determineAnnotation(elements, type.getUnderlyingType());
        if (anno != null) {
            this.addPreference(type, anno, weight);
        }
        return super.visitVariable(node, p);
    }
}
