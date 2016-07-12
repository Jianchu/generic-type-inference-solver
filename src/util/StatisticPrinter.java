package util;

import java.util.HashMap;
import java.util.Map;

public class StatisticPrinter {

    public enum StatisticKey {
        SLOTS_SIZE,
        CONSTRAINT_SIZE,
        CNF_VARIABLE_SIZE,
        CNF_CLAUSES_SIZE,
        LOGIQL_PREDICATES_SIZE,
        LOGIQL_DATA_SIZE,
        
        CONSTRAINT_GENERATION_TIME,
        GRAPH_GENERATION_TIME,
        JAVAC_INFERENCE_TIME,
        JAVAC_NORMAL_COMPILE_TIME,
        
        SAT_SERIALIZATION_TIME,
        SAT_SOLVING_GRAPH_PARALLEL_TIME,
        SAT_SOLVING_GRAPH_SEQUENTIAL_TIME,
        SAT_SOLVING_WITHOUT_GRAPH_TIME,
        
        LOGIQL_SERIALIZATION_TIME,
        LOGIQL_SOLVING_GRAPH_SEQUENTIAL_TIME,
        LOGIQL_SOLVING_WITHOUT_GRAPH_TIME,
        
        SAT_SOLVING_MEMORY,
        LOGIQL_SOLVING_MEMORY;
    } 
    
    private final static Map<StatisticKey, Long> statistic = new HashMap<StatisticKey, Long>();
    static {
        statistic.put(StatisticKey.SLOTS_SIZE, (long) 0);
        statistic.put(StatisticKey.CONSTRAINT_SIZE, (long) 0);
        statistic.put(StatisticKey.CNF_VARIABLE_SIZE, (long) 0);
        statistic.put(StatisticKey.CNF_CLAUSES_SIZE, (long) 0);
        statistic.put(StatisticKey.LOGIQL_PREDICATES_SIZE, (long) 0);
        statistic.put(StatisticKey.LOGIQL_DATA_SIZE, (long) 0);
        statistic.put(StatisticKey.CONSTRAINT_GENERATION_TIME, (long) 0);
        statistic.put(StatisticKey.GRAPH_GENERATION_TIME, (long) 0);
        statistic.put(StatisticKey.JAVAC_INFERENCE_TIME, (long) 0);
        statistic.put(StatisticKey.JAVAC_NORMAL_COMPILE_TIME, (long) 0);
        statistic.put(StatisticKey.SAT_SERIALIZATION_TIME, (long) 0);
        statistic.put(StatisticKey.SAT_SOLVING_GRAPH_PARALLEL_TIME, (long) 0);
        statistic.put(StatisticKey.SAT_SOLVING_GRAPH_SEQUENTIAL_TIME, (long) 0);
        statistic.put(StatisticKey.SAT_SOLVING_WITHOUT_GRAPH_TIME, (long) 0);
        statistic.put(StatisticKey.LOGIQL_SERIALIZATION_TIME, (long) 0);
        statistic.put(StatisticKey.LOGIQL_SOLVING_GRAPH_SEQUENTIAL_TIME, (long) 0);
        statistic.put(StatisticKey.LOGIQL_SOLVING_WITHOUT_GRAPH_TIME, (long) 0);
        statistic.put(StatisticKey.SAT_SOLVING_MEMORY, (long) 0);
        statistic.put(StatisticKey.LOGIQL_SOLVING_MEMORY, (long) 0);
    }

    public static void record(StatisticKey key, Long value) {
        synchronized (statistic) {
            if (key.equals(StatisticKey.LOGIQL_PREDICATES_SIZE)) {
                statistic.put(key, value);
            } else {
                long oldValue = statistic.get(key);
                statistic.put(key, value + oldValue);
            }
        }
    }

    public static Map<StatisticKey, Long> getStatistic() {
        return statistic;
    }

    public static void printStatistic() {
        System.out.println(statistic);
    }
}
