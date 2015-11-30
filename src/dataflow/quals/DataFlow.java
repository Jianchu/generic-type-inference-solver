package dataflow.quals;

import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeQualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Documented
@TypeQualifier
@Target({ElementType.TYPE_USE, ElementType.METHOD,ElementType.TYPE_PARAMETER,ElementType.LOCAL_VARIABLE})
@SubtypeOf({DataFlowTop.class})
public @interface DataFlow {
    String[] typeNames() default {};
}