import dataflow.qual.DataFlow;

public class TestUpperBound6 {
    public @DataFlow(typeNames={"int", "double", "java.lang.String", "java.lang.Object"}) Object upperBoundTesting6(int c) {
        if (c == 1) {
            return 3;
        }
        else if (c == 2) {
            return 3.14;
        }
        else if (c == 3) {
            return 3.1415926;
        }
        return "I am a String!";
    }
}