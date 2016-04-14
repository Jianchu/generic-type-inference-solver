package util;

import javax.lang.model.element.AnnotationMirror;

import constraintsolver.LatticeGenerator;

public class MathUtils {

    public static int mapIdToMatrixEntry(int id, LatticeGenerator lattice, AnnotationMirror type) {
        int column = lattice.modifierInt.get(type);
        int row = id - 1;
        int length = lattice.numModifiers;
        return column + row * length;
    }

    public static int mapIdToMatrixEntry(int id, LatticeGenerator lattice, int type) {
        int column = type;
        int row = id - 1;
        int length = lattice.numModifiers;
        return column + row * length;
    }
}
