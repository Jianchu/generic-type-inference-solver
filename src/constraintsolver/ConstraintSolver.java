package constraintsolver;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.ErrorReporter;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import util.PrintUtils;
import checkers.inference.DefaultInferenceSolution;
import checkers.inference.InferenceSolution;
import checkers.inference.InferenceSolver;
import checkers.inference.model.Constraint;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import constraintgraph.ConstraintGraph;
import constraintgraph.GraphBuilder;
import constraintgraph.Vertex;

/**
 * The default solver that could be called if there is no view adaptation
 * constraint in current type system.
 *
 * @author jianchu
 *
 */
public class ConstraintSolver implements InferenceSolver {

    public BackEnd realBackEnd;
    public String backEndType;
    public boolean useGraph;
    protected Lattice lattice;
    // public enum BackEndType {
    // SAT, LOGIQL, GENERAL
    // }

    @Override
    public InferenceSolution solve(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {
        configure(configuration);
        this.lattice = new Lattice(qualHierarchy);
        lattice.configure();
        Serializer<?, ?> defaultSerializer = createSerializer(backEndType);
        if (useGraph) {
            GraphBuilder graphBuilder = new GraphBuilder(slots, constraints);
            ConstraintGraph constraintGraph = graphBuilder.buildGraph();
            return graphSolve(constraintGraph, configuration, slots, constraints, qualHierarchy,
                    processingEnvironment, defaultSerializer);
        } else {
            realBackEnd = createBackEnd(backEndType, configuration, slots, constraints, qualHierarchy,
                    processingEnvironment, defaultSerializer);
            return solve();
        }
    }

    protected InferenceSolution graphSolve(ConstraintGraph constraintGraph,
            Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, Serializer<?, ?> defaultSerializer) {
        List<Map<Integer, AnnotationMirror>> inferenceSolutionMaps = new LinkedList<Map<Integer, AnnotationMirror>>();
        for (Map.Entry<Vertex, Set<Constraint>> entry : constraintGraph.getIndependentPath().entrySet()) {
            if (realBackEnd == null) {
                realBackEnd = createBackEnd(backEndType, configuration, slots, entry.getValue(),
                        qualHierarchy, processingEnvironment, defaultSerializer);
            } else {
                realBackEnd.setConstraint(entry.getValue());
            }
            inferenceSolutionMaps.add(realBackEnd.solve());
        }
        return mergeSolution(inferenceSolutionMaps);
    }

    protected InferenceSolution mergeSolution(List<Map<Integer, AnnotationMirror>> inferenceSolutionMaps) {
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        for (Map<Integer, AnnotationMirror> inferenceSolutionMap : inferenceSolutionMaps) {
            result.putAll(inferenceSolutionMap);
        }
        PrintUtils.printResult(result);
        return new DefaultInferenceSolution(result);
    }

    private void configure(Map<String, String> configuration) {
        String backEndName = configuration.get("backEndType");
        String useGraph = configuration.get("useGraph");
        if (backEndName == null) {
            this.backEndType = "maxsatbackend.MaxSat";
            // TODO: warning
            // ErrorReporter.errorAbort("not found back end.");
        } else {
            if (backEndName.equals("maxsatbackend.MaxSat") || backEndName.equals("logiqlbackend.LogiQL")
                    || backEndName.equals("General")) {
                this.backEndType = backEndName;
            } else {
                ErrorReporter.errorAbort("back end is not implemented yet.");
            }
        }

        if (useGraph == null || useGraph.equals("true")) {
            this.useGraph = true;
        } else {
            this.useGraph = false;
        }
    }

    protected BackEnd createBackEnd(String backEndType, Map<String, String> configuration,
            Collection<Slot> slots, Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy, ProcessingEnvironment processingEnvironment,
            Serializer<?, ?> defaultSerializer) {
        BackEnd backEnd = null;
        try {
            Class<?> backEndClass = Class.forName(backEndType + "BackEnd");
            Constructor<?> cons = backEndClass.getConstructor(Map.class, Collection.class,
                    Collection.class, QualifierHierarchy.class, ProcessingEnvironment.class,
                    Serializer.class, Lattice.class);
            backEnd = (BackEnd) cons.newInstance(configuration, slots, constraints, qualHierarchy,
                    processingEnvironment, defaultSerializer, lattice);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorReporter.errorAbort("back end is not implemented yet.");
        }

        return backEnd;
    }

    protected Serializer<?, ?> createSerializer(String value) {
        return new ConstraintSerializer<>(value, lattice);
    }

    protected InferenceSolution solve() {
        PrintUtils.printResult(realBackEnd.solve());
        return new DefaultInferenceSolution(realBackEnd.solve());
    }
}