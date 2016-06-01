package dataflow;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.AnnotationBuilder;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.TreeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferrableAnnotatedTypeFactory;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.VariableAnnotator;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;

import dataflow.qual.DataFlow;
import dataflow.qual.DataFlowTop;
import dataflow.util.DataflowUtils;

public class DataflowAnnotatedTypeFactory extends BaseAnnotatedTypeFactory
        implements InferrableAnnotatedTypeFactory {
    protected final AnnotationMirror DATAFLOW, DATAFLOWBOTTOM, DATAFLOWTOP;
//  private ProcessingEnvironment processingEnv = checker.getProcessingEnvironment();
    private ExecutableElement dataflowValue = TreeUtils.getMethod(
            "dataflow.qual.DataFlow", "typeNames", 0, processingEnv);

    //cannot use DataFlow.class.toString(), the string would be "interface dataflow.quals.DataFlow"
    //private ExecutableElement dataflowValue = TreeUtils.getMethod(DataFlow.class.toString(), "typeNames", 0, processingEnv);
    public DataflowAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        DATAFLOW = AnnotationUtils.fromClass(elements, DataFlow.class);
        DATAFLOWBOTTOM = createDataflowAnnotation(convert(""));
        DATAFLOWTOP = AnnotationUtils.fromClass(elements, DataFlowTop.class);
        postInit();
    }

    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
                super.createTreeAnnotator(),
                new DataflowTreeAnnotator()
        );
    }

    @Override
    public TreeAnnotator getInferenceTreeAnnotator(
            InferenceAnnotatedTypeFactory atypeFactory,
            InferrableChecker realChecker,
            VariableAnnotator variableAnnotator, SlotManager slotManager) {
        return new ListTreeAnnotator(new ImplicitsTreeAnnotator(this),
                new DataflowInferenceTreeAnnotator(atypeFactory, realChecker,
                        this, variableAnnotator, slotManager));
    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new DataFlowQualifierHierarchy(factory, DATAFLOWBOTTOM);
    }

    private AnnotationMirror createDataflowAnnotation(String[] dataType) {
        AnnotationBuilder builder =
            new AnnotationBuilder(processingEnv, DataFlow.class);
        builder.setValue("typeNames", dataType);
        return builder.build();
    }

    private String[] convert(String... typeName) {
        return typeName;
    }

    private final class DataFlowQualifierHierarchy extends GraphQualifierHierarchy {

        public DataFlowQualifierHierarchy(MultiGraphFactory f,
                AnnotationMirror bottom) {
            super(f, bottom);
        }

        private boolean isSubtypeWithRoots(AnnotationMirror rhs, AnnotationMirror lhs) {

            Set<String> rTypeNamesSet = new HashSet<String>(Arrays.asList(DataflowUtils.getTypeNames(rhs)));
            Set<String> lTypeNamesSet = new HashSet<String>(Arrays.asList(DataflowUtils.getTypeNames(lhs)));
            Set<String> rRootsSet = new HashSet<String>(Arrays.asList(DataflowUtils.getTypeNameRoots(rhs)));
            Set<String> lRootsSet = new HashSet<String>(Arrays.asList(DataflowUtils.getTypeNameRoots(lhs)));
            Set<String> combinedTypeNames = new HashSet<String>();
            combinedTypeNames.addAll(rTypeNamesSet);
            combinedTypeNames.addAll(lTypeNamesSet);
            Set<String> combinedRoots = new HashSet<String>();
            combinedRoots.addAll(rRootsSet);
            combinedRoots.addAll(lRootsSet);

            AnnotationMirror combinedAnno = DataflowUtils.createDataflowAnnotationWithRoots(
                    combinedTypeNames, combinedRoots, processingEnv);
            AnnotationMirror refinedCombinedAnno = refineDataflow(combinedAnno);
            AnnotationMirror refinedLhs = refineDataflow(lhs);

            if (AnnotationUtils.areSame(refinedCombinedAnno, refinedLhs)) {
                return true;
            } else {
                return false;
            }
        }

        private boolean isSubtypeWithoutRoots(AnnotationMirror rhs, AnnotationMirror lhs) {
            Set<String> rTypeNamesSet = new HashSet<String>(Arrays.asList(DataflowUtils.getTypeNames(rhs)));
            Set<String> lTypeNamesSet = new HashSet<String>(Arrays.asList(DataflowUtils.getTypeNames(lhs)));

            if (lTypeNamesSet.containsAll(rTypeNamesSet)) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
            if (AnnotationUtils.areSameIgnoringValues(rhs, DATAFLOW)
                    && AnnotationUtils.areSameIgnoringValues(lhs, DATAFLOW)) {
                return isSubtypeWithRoots(rhs, lhs);
                // return isSubtypeWithoutRoots(rhs, lhs);
            } else {
                //if (rhs != null && lhs != null)
                if (AnnotationUtils.areSameIgnoringValues(rhs, DATAFLOW)) {
                    rhs = DATAFLOW;
                } else if (AnnotationUtils.areSameIgnoringValues(lhs, DATAFLOW)) {
                    lhs = DATAFLOW;
                }
                return super.isSubtype(rhs, lhs);
            }
        }
    }

    public class DataflowTreeAnnotator extends TreeAnnotator {
        public DataflowTreeAnnotator() {
            super(DataflowAnnotatedTypeFactory.this);
        }

        @Override
        public Void visitNewClass(NewClassTree node, AnnotatedTypeMirror type) {
            AnnotationMirror dataFlowType = DataflowUtils.genereateDataflowAnnoFromNewClass(node, type, processingEnv);
            type.replaceAnnotation(dataFlowType);
            return super.visitNewClass(node, type);
        }

        @Override
        public Void visitLiteral(LiteralTree node, AnnotatedTypeMirror type) {
            AnnotationMirror dataFlowType = DataflowUtils.generateDataflowAnnoFromLiteral(node, type, processingEnv);
            type.replaceAnnotation(dataFlowType);

            return super.visitLiteral(node, type);
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, AnnotatedTypeMirror type) {
            ExecutableElement methodElement = TreeUtils.elementFromUse(node);
            boolean isFromStubFile = atypeFactory.isFromStubFile(methodElement);
            boolean isBytecode = ElementUtils.isElementFromByteCode(methodElement);
                   // && atypeFactory.declarationFromElement(methodElement) == null && !isFromStubFile;

            if (isBytecode) {
                AnnotationMirror dataFlowType = DataflowUtils.genereateDataflowAnnoFromByteCode(node,
                        type, processingEnv);
                type.replaceAnnotation(dataFlowType);
            }

            return super.visitMethodInvocation(node, type);
        }
    }

    public AnnotationMirror refineDataflow(AnnotationMirror type) {
        String[] typeNameRoots = DataflowUtils.getTypeNameRoots(type);
        Set<String> refinedRoots = new HashSet<String>();

        if (typeNameRoots.length == 0) {
            
        } else if (typeNameRoots.length == 1) {
            refinedRoots.add(typeNameRoots[0]);
        } else {
            // System.out.println(typeNameRoots[0] + typeNameRoots[1]);
            List<String> rootsList = new ArrayList<String>(Arrays.asList(typeNameRoots));
            while (rootsList.size() != 0) {
                TypeMirror decType = elements.getTypeElement(rootsList.get(0)).asType();
                if (!isComparable(decType, rootsList)) {
                    refinedRoots.add(rootsList.get(0));
                    rootsList.remove(0);
                }
            }
        }
        
        String[] typeNames = DataflowUtils.getTypeNames(type);
        Set<String> refinedtypeNames = new HashSet<String>();

        if (refinedRoots.size() == 0) {
            refinedtypeNames = new HashSet<String>(Arrays.asList(typeNames));
            return DataflowUtils.createDataflowAnnotation(refinedtypeNames, processingEnv);
        } else {
            for (String typeName : typeNames) {
                TypeMirror decType = elements.getTypeElement(typeName).asType();
                if (shouldPresent(decType, refinedRoots)) {
                    refinedtypeNames.add(typeName);
                }
            }
        }

        return DataflowUtils.createDataflowAnnotationWithRoots(refinedtypeNames, refinedRoots, processingEnv);
    }

    private boolean isComparable(TypeMirror decType, List<String> rootsList) {
        for (int i = 1; i < rootsList.size(); i++) {
            TypeMirror comparedDecType = elements.getTypeElement(rootsList.get(i)).asType();
            if (this.types.isSubtype(comparedDecType, decType)) {
                rootsList.remove(i);
                return true;
            } else if (this.types.isSubtype(decType, comparedDecType)) {
                rootsList.remove(0);
                return true;
            }
        }

        return false;
    }
    
    private boolean shouldPresent(TypeMirror decType, Set<String> refinedRoots) {
        for (String refinedRoot : refinedRoots) {
            TypeMirror comparedDecType = elements.getTypeElement(refinedRoot).asType();
            if (this.types.isSubtype(decType, comparedDecType)) {
                return false;
            } else if (this.types.isSubtype(comparedDecType, decType)) {
                return true;
            }
        }
        return true;
    }
}
