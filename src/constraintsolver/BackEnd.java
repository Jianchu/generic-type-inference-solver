package constraintsolver;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.util.Collection;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.inference.InferenceSolution;
import checkers.inference.model.Constraint;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;

public abstract class BackEnd {

    public Map<String, String> configuration;
    public Collection<Slot> slots;
    public Collection<Constraint> constraints;
    public QualifierHierarchy qualHierarchy;
    public ProcessingEnvironment processingEnvironment;
    public Serializer realSerializer;

    public BackEnd(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, Serializer realSerializer) {

        this.configuration = configuration;
        this.slots = slots;
        this.constraints = constraints;
        this.qualHierarchy = qualHierarchy;
        this.processingEnvironment = processingEnvironment;
        this.realSerializer = realSerializer;
    }

    public abstract InferenceSolution solve();
}
