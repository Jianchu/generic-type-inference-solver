package util;

import org.checkerframework.javacutil.AnnotationUtils;

import java.util.Collection;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.model.ComparableConstraint;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.EqualityConstraint;
import checkers.inference.model.InequalityConstraint;
import checkers.inference.model.SubtypeConstraint;
import constraintsolver.Lattice;

public class ConstantUtils {

    public static boolean checkConstant(ConstantSlot constant1, ConstantSlot constant2, Constraint constraint) {

        AnnotationMirror annoMirror1 = constant1.getValue();
        AnnotationMirror annoMirror2 = constant2.getValue();

        if (constraint instanceof SubtypeConstraint) {
            Collection<AnnotationMirror> subtypeOfConstant2 = Lattice.subType.get(annoMirror2);
            if (!subtypeOfConstant2.contains(annoMirror1)) {
                return false;
            }
        } else if (constraint instanceof EqualityConstraint) {
            if (!areSameType(annoMirror1, annoMirror2)) {
                return false;
            }
        } else if (constraint instanceof InequalityConstraint) {
            if (areSameType(annoMirror1, annoMirror2)) {
                return false;
            }
        } else if (constraint instanceof ComparableConstraint) {
            Collection<AnnotationMirror> incomparableOfConstant2 = Lattice.incomparableType.get(annoMirror2);
            if (incomparableOfConstant2.contains(annoMirror1)) {
                return false;
            }
        }
        return true;
    }

    public static boolean areSameType(AnnotationMirror m1, AnnotationMirror m2) {
        return AnnotationUtils.areSameIgnoringValues(m1, m2);
    }
}
