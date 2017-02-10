package util;

import org.checkerframework.javacutil.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class StatisticPrinter {

    public static AtomicInteger SAT_PARALLEL_SERIALIZATION_SUM = new AtomicInteger(0);
    public static AtomicInteger SAT_PARALLEL_SOLVING_SUM = new AtomicInteger(0);


    public static synchronized void recordSerializationSingleThread(long value) {
        SAT_PARALLEL_SERIALIZATION_SUM.addAndGet((int) value);
    }

    public static synchronized void recordSolvingSingleThread(long value) {
        SAT_PARALLEL_SOLVING_SUM.addAndGet((int) value);
    }

    public static void record(StatisticKey key, Long value) {
        synchronized (statistic) {
            if (key.equals(StatisticKey.LOGIQL_PREDICATES_SIZE)) {
                statistic.put(key, value);
            } else if (key.equals(StatisticKey.SAT_SOLVING_GRAPH_PARALLEL_TIME)
                    || key.equals(StatisticKey.SAT_SERIALIZATION_TIME)) {
                long oldValue = statistic.get(key);
                if (value > oldValue) {
                    statistic.put(key, value);
                }
            } else {
                long oldValue = statistic.get(key);
                statistic.put(key, value + oldValue);
            }
        }
    }

    public enum StatisticKey {
        SLOTS_SIZE,
        CONSTRAINT_SIZE,
        CNF_VARIABLE_SIZE,
        CNF_CLAUSES_SIZE,
        LOGIQL_PREDICATES_SIZE,
        LOGIQL_DATA_SIZE,
        NUMBER_ANNOTATOIN,
        
        CONSTRAINT_GENERATION_TIME,
        GRAPH_GENERATION_TIME,
        JAVAC_INFERENCE_TIME,
        JAVAC_NORMAL_COMPILE_TIME,
        
        SAT_SERIALIZATION_TIME,
        SAT_SOLVING_GRAPH_PARALLEL_TIME,
        SAT_SOLVING_GRAPH_SEQUENTIAL_TIME,
        SAT_SOLVING_WITHOUT_GRAPH_TIME,
        SAT_SOLVING_WITHOUT_GRAPH_TIME_LL,
        SAT_SOLVING_GRAPH_SEQUENTIAL_TIME_LL,
        SAT_PARALLEL_SERIALIZATION_SUM,
        SAT_PARALLEL_SOLVING_SUM,
        NEW_SAT_NO_GRAPH_SOLVING,
        
        LOGIQL_SERIALIZATION_TIME,
        LOGIQL_SOLVING_GRAPH_SEQUENTIAL_TIME,
        LOGIQL_SOLVING_WITHOUT_GRAPH_TIME,
        
        SAT_SOLVING_MEMORY,
        LOGIQL_SOLVING_MEMORY,
        GRAPH_SIZE;
    } 
    
    private final static Map<StatisticKey, Long> statistic = new HashMap<StatisticKey, Long>();


    private final static List<Pair<Long, Long>> threadsData = new ArrayList<Pair<Long, Long>>();

    // private final static Map<Long, Pair<Long, Long>> threadsData = new
    // TreeMap<Long, Pair<Long, Long>>(
    // new Comparator<Long>() {
    // @Override
    // public int compare(Long o1, Long o2) {
    // return (int) (o1 - o2);
    // }
    //
    // });

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
        statistic.put(StatisticKey.SAT_SOLVING_WITHOUT_GRAPH_TIME_LL, (long) 0);
        statistic.put(StatisticKey.SAT_SOLVING_GRAPH_SEQUENTIAL_TIME_LL, (long) 0);
        statistic.put(StatisticKey.SAT_PARALLEL_SERIALIZATION_SUM, (long) 0);
        statistic.put(StatisticKey.SAT_PARALLEL_SOLVING_SUM, (long) 0);
        statistic.put(StatisticKey.GRAPH_SIZE, (long) 0);
        statistic.put(StatisticKey.NUMBER_ANNOTATOIN, (long) 0);
        statistic.put(StatisticKey.NEW_SAT_NO_GRAPH_SOLVING, (long) 0);

    }



    public static void recordSingleThread(Pair<Long, Long> value) {
        synchronized (threadsData) {
            threadsData.add(value);
        }
    }

    public static Map<StatisticKey, Long> getStatistic() {
        return statistic;
    }

    public static List<Pair<Long, Long>> getThreadsData() {
        return threadsData;
    }

    public static void printStatistic() {
        System.out.println(statistic);
    }
}
