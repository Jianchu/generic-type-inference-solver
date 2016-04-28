package constraintsolver;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.ErrorReporter;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.inference.InferenceSolution;
import checkers.inference.InferenceSolver;
import checkers.inference.model.Constraint;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;

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

    // public enum BackEndType {
    // SAT, LOGIQL, GENERAL
    // }

    @Override
    public InferenceSolution solve(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {
        configure(configuration);
        Serializer<?, ?> defaultSerializer = createSerializer(backEndType);
        realBackEnd = createBackEnd(backEndType, configuration, slots, constraints, qualHierarchy,
                processingEnvironment, defaultSerializer);
        return solve();
    }

    private void configure(Map<String, String> configuration) {
        String backEndName = configuration.get("backEndType");
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
                    Serializer.class);
            backEnd = (BackEnd) cons.newInstance(configuration, slots, constraints, qualHierarchy,
                    processingEnvironment, defaultSerializer);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorReporter.errorAbort("back end is not implemented yet.");
        }

        return backEnd;
    }

    protected Serializer<?, ?> createSerializer(String value) {
        return new ConstraintSerializer<>(value);
    }

    protected InferenceSolution solve() {
        return realBackEnd.solve();
    }
}