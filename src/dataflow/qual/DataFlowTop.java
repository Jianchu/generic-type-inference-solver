package dataflow.quals;

import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.InvisibleQualifier;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeQualifier;

import java.lang.annotation.Target;

@TypeQualifier
@DefaultQualifierInHierarchy
@InvisibleQualifier
@SubtypeOf({})
@Target({})
public @interface DataFlowTop {}