package dataflow.quals;

import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultLocation;
import org.checkerframework.framework.qual.ImplicitFor;
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
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.METHOD})
@SubtypeOf({})
//@DefaultFor({DefaultLocation.RETURNS})
@ImplicitFor(
        trees={                
//            Tree.Kind.INT_LITERAL,
//            Tree.Kind.LONG_LITERAL,            
//            Tree.Kind.FLOAT_LITERAL,    
//            Tree.Kind.DOUBLE_LITERAL,   
//            Tree.Kind.BOOLEAN_LITERAL,
//            Tree.Kind.CHAR_LITERAL,
//            Tree.Kind.STRING_LITERAL,
//            Tree.Kind.NEW_CLASS,
//            Tree.Kind.PRIMITIVE_TYPE,
//            Tree.Kind.RETURN,
            Tree.Kind.METHOD,
        })
public @interface DataFlow {
    String typeName() default "";
}