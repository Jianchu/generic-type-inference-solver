package ontology.solver;

import java.util.HashMap;
import java.util.Map;

public class SequenceSolution {
    private final Map<Integer, Boolean> result;
    private final String datatype;

    public SequenceSolution(Map<Integer, Boolean> result, String datatype) {
        this.result = result;
        this.datatype = datatype;
    }

    private SequenceSolution(String datatype) {
        this(new HashMap<Integer, Boolean>(), datatype);
    }

    public Map<Integer, Boolean> getResult() {
        return result;
    }

    public String getDatatype() {
        return datatype;
    }

    public static SequenceSolution noSolution(String datatype) {
        return new SequenceSolution(datatype);
    }

}
