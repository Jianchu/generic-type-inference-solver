package util;

import javax.lang.model.element.AnnotationMirror;

import constraintsolver.Lattice;

public class MathUtils {

    public static int mapIdToMatrixEntry(int id, AnnotationMirror type) {
        int column = Lattice.getTypeToInt().get(type) + 1;
        int row = id - 1;
        int length = Lattice.getNumTypes();
        return column + row * length;
    }

    public static int mapIdToMatrixEntry(int id, int type) {
        int column = type + 1;
        int row = id - 1;
        int length = Lattice.getNumTypes();
        return column + row * length;
    }

    public static int getSlotId(int var) {
        return (Math.abs(var) / Lattice.getNumTypes() + 1);
    }

    public static int getIntRep(int var) {
        return Math.abs(var) - (Math.abs(var) / Lattice.getNumTypes()) * Lattice.getNumTypes();
    }
}
