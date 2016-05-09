package ontology;

import ontology.qual.Ontology;
import ontology.qual.OntologyTop;
import ontology.util.OntologyUtils;

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
import org.checkerframework.javacutil.TreeUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceTreeAnnotator;
import checkers.inference.InferrableAnnotatedTypeFactory;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.VariableAnnotator;
import checkers.inference.model.ConstantSlot;

import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;

public class OntologyAnnotatedTypeFactory extends BaseAnnotatedTypeFactory
        implements InferrableAnnotatedTypeFactory {

    protected final AnnotationMirror ONTOLOGY, ONTOLOGYBOTTOM, ONTOLOGYTOP;
    private ExecutableElement ontologyValue = TreeUtils.getMethod("ontology.qual.Ontology", "typeNames",
            0, processingEnv);

    public OntologyAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        ONTOLOGY = AnnotationUtils.fromClass(elements, Ontology.class);
        ONTOLOGYBOTTOM = OntologyUtils.createOntologyAnnotation(OntologyUtils.convert(""), processingEnv);
        ONTOLOGYTOP = AnnotationUtils.fromClass(elements, OntologyTop.class);
        postInit();
    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new OntologyQualifierHierarchy(factory, ONTOLOGYBOTTOM);
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

    private final class OntologyQualifierHierarchy extends GraphQualifierHierarchy {

        public OntologyQualifierHierarchy(MultiGraphFactory f, AnnotationMirror bottom) {
            super(f, bottom);
        }

        @Override
        public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
            if (AnnotationUtils.areSameIgnoringValues(rhs, ONTOLOGY)
                    && AnnotationUtils.areSameIgnoringValues(lhs, ONTOLOGY)) {
                String[] rhsValue = getOntologyValue(rhs);
                String[] lhsValue = getOntologyValue(lhs);
                Set<String> rSet = new HashSet<String>(Arrays.asList(rhsValue));
                Set<String> lSet = new HashSet<String>(Arrays.asList(lhsValue));
                if (lSet.containsAll(rSet)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                // if (rhs != null && lhs != null)
                if (AnnotationUtils.areSameIgnoringValues(rhs, ONTOLOGY)) {
                    rhs = ONTOLOGY;
                } else if (AnnotationUtils.areSameIgnoringValues(lhs, ONTOLOGY)) {
                    lhs = ONTOLOGY;
                }
                return super.isSubtype(rhs, lhs);
            }
        }

        private String[] getOntologyValue(AnnotationMirror type) {
            @SuppressWarnings("unchecked")
            List<String> allTypesList = ((List<String>) AnnotationUtils
                    .getElementValuesWithDefaults(type).get(ontologyValue).getValue());
            // types in this list is
            // org.checkerframework.framework.util.AnnotationBuilder.
            String[] allTypesInArray = new String[allTypesList.size()];
            int i = 0;
            for (Object o : allTypesList) {
                allTypesInArray[i] = o.toString();
                i++;
                // System.out.println(o.toString());
            }
            return allTypesInArray;
        }
    }

    public class OntologyTreeAnnotator extends TreeAnnotator {
        public OntologyTreeAnnotator() {
            super(OntologyAnnotatedTypeFactory.this);
        }

        @Override
        public Void visitNewClass(NewClassTree node, AnnotatedTypeMirror type) {
            if (OntologyUtils.determineAnnotation(type.getUnderlyingType())) {
                AnnotationMirror dataFlowType = OntologyUtils.genereateOntologyAnnoFromNew(processingEnv);
                type.replaceAnnotation(dataFlowType);
            }
            return super.visitNewClass(node, type);
        }

        @Override
        public Void visitNewArray(final NewArrayTree newArrayTree, final AnnotatedTypeMirror atm) {
            AnnotationMirror anno = OntologyUtils.genereateOntologyAnnoFromNew(processingEnv);
            atm.replaceAnnotation(anno);
            return super.visitNewArray(newArrayTree, atm);
        }
    }

    public class OntologyInferenceTreeAnnotator extends InferenceTreeAnnotator {

        private final VariableAnnotator variableAnnotator;

        public OntologyInferenceTreeAnnotator(InferenceAnnotatedTypeFactory atypeFactory,
                InferrableChecker realChecker, AnnotatedTypeFactory realAnnotatedTypeFactory,
                VariableAnnotator variableAnnotator, SlotManager slotManager) {
            super(atypeFactory, realChecker, realAnnotatedTypeFactory, variableAnnotator, slotManager);
            this.variableAnnotator = variableAnnotator;
        }

        @Override
        public Void visitNewClass(final NewClassTree newClassTree, final AnnotatedTypeMirror atm) {
            if (OntologyUtils.determineAnnotation(atm.getUnderlyingType())) {
                AnnotationMirror anno = OntologyUtils.genereateOntologyAnnoFromNew(processingEnv);
                ConstantSlot cs = variableAnnotator.createConstant(anno, newClassTree);
                atm.replaceAnnotation(cs.getValue());
            }
            variableAnnotator.visit(atm, newClassTree.getIdentifier());
            return null;
        }

        @Override
        public Void visitNewArray(final NewArrayTree newArrayTree, final AnnotatedTypeMirror atm) {
            AnnotationMirror anno = OntologyUtils.genereateOntologyAnnoFromNew(processingEnv);
            ConstantSlot cs = variableAnnotator.createConstant(anno, newArrayTree);
            atm.replaceAnnotation(cs.getValue());
            variableAnnotator.visit(atm, newArrayTree);
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
}
