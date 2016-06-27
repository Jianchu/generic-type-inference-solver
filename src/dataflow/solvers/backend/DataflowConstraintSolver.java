package dataflow.solvers.backend;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import util.PrintUtils;
import checkers.inference.DefaultInferenceSolution;
import checkers.inference.InferenceSolution;
import checkers.inference.model.Constraint;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import constraintgraph.ConstraintGraph;
import constraintgraph.Vertex;
import constraintsolver.BackEnd;
import constraintsolver.ConstraintSolver;
import constraintsolver.TwoQualifiersLattice;
import dataflow.qual.DataFlow;
import dataflow.util.DataflowUtils;

public class DataflowConstraintSolver extends ConstraintSolver {

    private AnnotationMirror DATAFLOW;
    private AnnotationMirror DATAFLOWBOTTOM;

    @Override
    protected InferenceSolution graphSolve(ConstraintGraph constraintGraph,
            Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, Serializer<?, ?> defaultSerializer) {

        DATAFLOW = AnnotationUtils.fromClass(processingEnvironment.getElementUtils(), DataFlow.class);
        DATAFLOWBOTTOM = DataflowUtils.createDataflowAnnotation(DataflowUtils.convert(""),
                processingEnvironment);
        List<BackEnd> backEnds = new ArrayList<BackEnd>();
        List<Map<Integer, AnnotationMirror>> inferenceSolutionMaps = new LinkedList<Map<Integer, AnnotationMirror>>();

        for (Map.Entry<Vertex, Set<Constraint>> entry : constraintGraph.getIndependentPath().entrySet()) {
            AnnotationMirror anno = entry.getKey().getValue();
            if (AnnotationUtils.areSameIgnoringValues(anno, DATAFLOW)) {
                String[] dataflowValues = DataflowUtils.getTypeNames(anno);
                String[] dataflowRoots = DataflowUtils.getTypeNameRoots(anno);
                if (dataflowValues.length == 1) {
                    AnnotationMirror DATAFLOWTOP = DataflowUtils.createDataflowAnnotation(DataflowUtils.convert(dataflowValues[0]), processingEnvironment);
                    TwoQualifiersLattice latticeFor2 = new TwoQualifiersLattice(DATAFLOWTOP, DATAFLOWBOTTOM);
                    Serializer<?, ?> serializer = createSerializer(backEndType, latticeFor2);
                    backEnds.add(createBackEnd(backEndType, configuration, slots, entry.getValue(),
                            qualHierarchy, processingEnvironment, serializer));
                } else if (dataflowRoots.length == 1) {
                    AnnotationMirror DATAFLOWTOP = DataflowUtils.createDataflowAnnotationForByte(DataflowUtils.convert(dataflowRoots), processingEnvironment);
                    TwoQualifiersLattice latticeFor2 = new TwoQualifiersLattice(DATAFLOWTOP, DATAFLOWBOTTOM);
                    Serializer<?, ?> serializer = createSerializer(backEndType, latticeFor2);
                    backEnds.add(createBackEnd(backEndType, configuration, slots, entry.getValue(),
                            qualHierarchy, processingEnvironment, serializer));
                }
            }
        }
        try {
            if (backEnds.size() > 0) {
                inferenceSolutionMaps = solveInparallel(backEnds);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return mergeSolution(inferenceSolutionMaps);
    }

    // TODO: change to dataflow merge
    @Override
    protected InferenceSolution mergeSolution(List<Map<Integer, AnnotationMirror>> inferenceSolutionMaps) {
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        for (Map<Integer, AnnotationMirror> inferenceSolutionMap : inferenceSolutionMaps) {
            result.putAll(inferenceSolutionMap);
        }
        PrintUtils.printResult(result);
        return new DefaultInferenceSolution(result);
    }

}
