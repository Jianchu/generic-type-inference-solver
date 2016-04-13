package ontology;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;

import checkers.inference.InferenceChecker;
import checkers.inference.InferenceVisitor;

public class OntologyVisitor extends InferenceVisitor<OntologyChecker, BaseAnnotatedTypeFactory> {

    public OntologyVisitor(OntologyChecker checker, InferenceChecker ichecker, BaseAnnotatedTypeFactory factory,
            boolean infer) {
        super(checker, ichecker, factory, infer);
    }
}
