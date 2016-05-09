package checkers.inference;

import ontology.solver.OntologySolver;

import org.checkerframework.framework.test.TestUtilities;
import org.checkerframework.javacutil.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;

import checkers.inference.test.CFInferenceTest;

public class OntologyTest extends CFInferenceTest {

    public OntologyTest(File testFile) {
        super(testFile,  ontology.OntologyChecker.class, "ontology",
              "-Anomsgtext", "-d", "tests/build/outputdir");
    }

    @Override
    public Pair<String, List<String>> getSolverNameAndOptions() {
        return Pair.<String, List<String>> of(OntologySolver.class.getCanonicalName(), new ArrayList<String>());
    }

    @Parameters
    public static List<File> getTestFiles(){
        List<File> testfiles = new ArrayList<>(); //InferenceTestUtilities.findAllSystemTests();
        testfiles.addAll(TestUtilities.findRelativeNestedJavaFiles("testdata", "ontology"));
        return testfiles;
    }
}
