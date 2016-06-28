package util;

import javax.lang.model.element.AnnotationMirror;

import constraintsolver.Lattice;

public class MathUtils {

    public static int mapIdToMatrixEntry(int id, AnnotationMirror type, Lattice lattice) {
        int column = lattice.typeToInt.get(type) + 1;
        int row = id - 1;
        int length = lattice.numTypes;
        return column + row * length;
    }

    public static int mapIdToMatrixEntry(int id, int type, Lattice lattice) {
        int column = type + 1;
        int row = id - 1;
        int length = lattice.numTypes;
        return column + row * length;
    }

    public static int getSlotId(int var, Lattice lattice) {
        return (Math.abs(var) / lattice.numTypes + 1);
    }

    public static int getIntRep(int var, Lattice lattice) {
        return Math.abs(var) - (Math.abs(var) / lattice.numTypes) * lattice.numTypes;
    }
}
