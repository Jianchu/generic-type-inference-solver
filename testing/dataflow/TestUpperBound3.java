import dataflow.qual.DataFlow;

public class TestUpperBound3 {
    public @DataFlow(typeNames={"int", "java.lang.String"}) Object upperBoundTesting3(int c) {
        if (c > 0) {
            return 3;
        }
        return "I am a String!";
    }
}