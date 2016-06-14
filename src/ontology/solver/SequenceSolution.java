package ontology.solver;

import java.util.HashMap;
import java.util.Map;

public class SequenceSolution {
    private final Map<Integer, Boolean> result;
    private final String value;

    public SequenceSolution(Map<Integer, Boolean> result, String value) {
        this.result = result;
        this.value = value;
    }

    private SequenceSolution(String value) {
        this(new HashMap<Integer, Boolean>(), value);
    }

    public Map<Integer, Boolean> getResult() {
        return result;
    }

    public String getValue() {
        return value;
    }

    public static SequenceSolution noSolution(String value) {
        return new SequenceSolution(value);
    }

}
