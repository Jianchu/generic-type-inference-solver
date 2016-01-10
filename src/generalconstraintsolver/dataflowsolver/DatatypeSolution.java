package generalconstraintsolver.dataflowsolver;

import java.util.HashMap;
import java.util.Map;

public class DatatypeSolution {
    Map<Integer, Boolean> result;
    Map<Integer, Boolean> idToExistence;
    String datatype;

    public DatatypeSolution(Map<Integer, Boolean> result, Map<Integer, Boolean> idToExistence, String datatype) {
        this.result = result;
        this.idToExistence = idToExistence;
        this.datatype = datatype;
    }
    
    private DatatypeSolution(String datatype) {
        this(new HashMap<Integer, Boolean>(), new HashMap<Integer, Boolean>(), datatype);
    }
    
    
    public Map<Integer, Boolean> getIdToExistence() {
        return idToExistence;
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
