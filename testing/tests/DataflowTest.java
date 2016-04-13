package checkers.inference;

import checkers.inference.test.CFInferenceTest;

import org.checkerframework.framework.test.TestUtilities;
import org.checkerframework.javacutil.Pair;

import org.junit.runners.Parameterized.Parameters;

import dataflow.solver.DataflowSolver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataflowTest extends CFInferenceTest {

    public DataflowTest(File testFile) {
        super(testFile,  dataflow.DataflowChecker.class, "dataflow",
              "-Anomsgtext", "-d", "tests/build/outputdir");
    }

    @Override
    public Pair<String, List<String>> getSolverNameAndOptions() {
        return Pair.<String, List<String>>of(DataflowSolver.class.getCanonicalName(), new ArrayList<String>());
    }

    @Parameters
    public static List<File> getTestFiles(){
        List<File> testfiles = new ArrayList<>();//InferenceTestUtilities.findAllSystemTests();
        testfiles.addAll(TestUtilities.findRelativeNestedJavaFiles("testdata", "dataflow"));
        return testfiles;
    }
}
