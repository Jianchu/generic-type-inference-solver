import dataflow.qual.DataFlow;

public class TestUpperBound4 {
    public @DataFlow(typeNames={"java.lang.String"}) Object upperBoundTesting4(int c) {
        if (c > 0) {
            return "I am a String!";
        }
        return "I am a String too!";
    }
}