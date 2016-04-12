package ontology.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A meta-annotation to specify the Programmer's Assistant Ontology to which the
 * given qualifier maps.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface OntologyMapping {
    /**
     * The corresponding Programmer's Assistant Ontology.
     */
    String[] value();
}
