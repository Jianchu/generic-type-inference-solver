package util;

import org.sat4j.core.VecInt;

public class VectorUtils {

    public static VecInt asVec(int... result) {
        return new VecInt(result);
    }

    public static VecInt[] asVecArray(int... vars) {
        return new VecInt[] { new VecInt(vars) };
    }
}
