package util;

import org.checkerframework.javacutil.AnnotationUtils;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.model.ComparableConstraint;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.EqualityConstraint;
import checkers.inference.model.InequalityConstraint;
import checkers.inference.model.SubtypeConstraint;

public class ConstantUtils {

    public static boolean checkConstant(ConstantSlot constant1, ConstantSlot constant2, Constraint constraint) {
        if (constraint instanceof SubtypeConstraint) {

        } else if (constraint instanceof EqualityConstraint) {

        } else if (constraint instanceof InequalityConstraint) {

        } else if (constraint instanceof ComparableConstraint) {

        }
        return true;
    }

    public static boolean areSameType(AnnotationMirror m1, AnnotationMirror m2) {
        return AnnotationUtils.areSameIgnoringValues(m1, m2);
    }
}
