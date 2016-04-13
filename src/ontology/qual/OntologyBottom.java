package ontology.qual;

import org.checkerframework.framework.qual.InvisibleQualifier;
import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@InvisibleQualifier
@SubtypeOf({}) // changed programmatically
@Target({ ElementType.TYPE_USE })
// @TargetLocations({ TypeUseLocation.EXPLICIT_UPPER_BOUND })
public @interface OntologyBottom {
}
