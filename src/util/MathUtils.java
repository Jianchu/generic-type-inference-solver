package util;

import javax.lang.model.element.AnnotationMirror;

import constraintsolver.Lattice;

public class MathUtils {

    public static int mapIdToMatrixEntry(int id, AnnotationMirror type) {
        int column = Lattice.modifierInt.get(type);
        int row = id - 1;
        int length = Lattice.numModifiers;
        return column + row * length;
    }

    public static int mapIdToMatrixEntry(int id, int type) {
        int column = type;
        int row = id - 1;
        int length = Lattice.numModifiers;
        return column + row * length;
    }
}
