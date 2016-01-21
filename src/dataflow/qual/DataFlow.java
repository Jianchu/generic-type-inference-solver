package dataflow.qual;

import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import dataflow.qual.DataFlowTop;

@Documented
@Target({ElementType.TYPE_USE,ElementType.TYPE_PARAMETER})
//@Target({ElementType.TYPE_USE})
@SubtypeOf({DataFlowTop.class})

public @interface DataFlow {
    String[] typeNames() default {};
}