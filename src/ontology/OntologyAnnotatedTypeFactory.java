package ontology;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationUtils;

import javax.lang.model.element.AnnotationMirror;

import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;

import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceTreeAnnotator;
import checkers.inference.InferrableAnnotatedTypeFactory;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.VariableAnnotator;
import checkers.inference.model.ConstantSlot;
import ontology.qual.OntologyBottom;
import ontology.qual.OntologyTop;
import ontology.qual.Sequence;
import ontology.qual.SortedSequence;
import ontology.util.OntologyUtils;

public class OntologyAnnotatedTypeFactory extends BaseAnnotatedTypeFactory
        implements InferrableAnnotatedTypeFactory {

    protected final AnnotationMirror SEQ, SORTEDSEQ, ONTTOP, ONTBOT;

    public OntologyAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        SEQ = AnnotationUtils.fromClass(elements, Sequence.class);
        SORTEDSEQ = AnnotationUtils.fromClass(elements, SortedSequence.class);
        ONTTOP = AnnotationUtils.fromClass(elements, OntologyTop.class);
        ONTBOT = AnnotationUtils.fromClass(elements, OntologyBottom.class);
        postInit();
    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new GraphQualifierHierarchy(factory, ONTBOT);
    }

    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(super.createTreeAnnotator(), new OntologyTreeAnnotator());
    }

    @Override
    public TreeAnnotator getInferenceTreeAnnotator(
            InferenceAnnotatedTypeFactory atypeFactory,
            InferrableChecker realChecker,
            VariableAnnotator variableAnnotator, SlotManager slotManager) {
        return new ListTreeAnnotator(new ImplicitsTreeAnnotator(this),
                new OntologyInferenceTreeAnnotator(atypeFactory, realChecker,
                        this, variableAnnotator, slotManager));
    }

    public class OntologyTreeAnnotator extends TreeAnnotator {

        public OntologyTreeAnnotator() {
            super(OntologyAnnotatedTypeFactory.this);
        }

        @Override
        public Void visitNewClass(NewClassTree newClassTree, AnnotatedTypeMirror atm) {
            AnnotationMirror anno = OntologyUtils.determineAnnotation(elements, atm.getUnderlyingType());
            if (anno != null) {
                atm.replaceAnnotation(anno);
            }
            return super.visitNewClass(newClassTree, atm);
        }

        @Override
        public Void visitNewArray(final NewArrayTree newArrayTree, final AnnotatedTypeMirror atm) {
            atm.replaceAnnotation(SEQ);
            return super.visitNewArray(newArrayTree, atm);
        }
    }

    public class OntologyInferenceTreeAnnotator extends InferenceTreeAnnotator {

        // private final SlotManager slotManager;
        private final VariableAnnotator variableAnnotator;
        // private final AnnotatedTypeFactory realTypeFactory;
        // private final InferrableChecker realChecker;

        //TODO : override visit variable tree... if it's sequence, then add preference constraints.
        public OntologyInferenceTreeAnnotator(InferenceAnnotatedTypeFactory atypeFactory, InferrableChecker realChecker,
                AnnotatedTypeFactory realAnnotatedTypeFactory, VariableAnnotator variableAnnotator,
                SlotManager slotManager) {
            super(atypeFactory, realChecker, realAnnotatedTypeFactory, variableAnnotator, slotManager);

            // this.slotManager = slotManager;
            this.variableAnnotator = variableAnnotator;
            // this.realTypeFactory = realAnnotatedTypeFactory;
            // this.realChecker = realChecker;
        }

        @Override
        public Void visitNewClass(final NewClassTree newClassTree,
                final AnnotatedTypeMirror atm) {
            AnnotationMirror anno = OntologyUtils.determineAnnotation(elements, atm.getUnderlyingType());
            if (anno != null) {
                ConstantSlot cs = variableAnnotator.createConstant(anno, newClassTree);
                atm.replaceAnnotation(cs.getValue());
                variableAnnotator.visit(atm, newClassTree.getIdentifier());
            }
            return null;
        }

        @Override
        public Void visitNewArray(final NewArrayTree newArrayTree, final AnnotatedTypeMirror atm) {
            ConstantSlot cs = variableAnnotator.createConstant(SEQ, newArrayTree);
            atm.replaceAnnotation(cs.getValue());
            variableAnnotator.visit(atm, newArrayTree);
            return null;
        }
    }
}
