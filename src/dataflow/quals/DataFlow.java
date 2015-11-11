package dataflow.quals;

import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultLocation;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.InvisibleQualifier;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeQualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sun.source.tree.Tree;

@Documented
@TypeQualifier
//@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.METHOD,ElementType.TYPE_PARAMETER,ElementType.LOCAL_VARIABLE})
@SubtypeOf({DataFlowTop.class})
//@DefaultFor({DefaultLocation.RETURNS})
//@ImplicitFor(
//        trees={                
//            Tree.Kind.METHOD,
//        })

public @interface DataFlow {
//    String typeName(); 
    String[] typeNames() default {};//array
}