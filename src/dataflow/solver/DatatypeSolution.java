package dataflow.solver;

import java.util.HashMap;
import java.util.Map;

public class DatatypeSolution {
    private final Map<Integer, Boolean> result;
    private final String datatype;

    public DatatypeSolution(Map<Integer, Boolean> result, String datatype) {
        this.result = result;
        this.datatype = datatype;
    }

    private DatatypeSolution(String datatype) {
        this(new HashMap<Integer, Boolean>(), datatype);
    }

    public Map<Integer, Boolean> getResult() {
        return result;
    }

    public String getDatatype() {
        return datatype;
    }

    public static DatatypeSolution noSolution(String datatype) {
        return new DatatypeSolution(datatype);
    }

}
