import dataflow.qual.DataFlow;

public class TestUpperBound3Invalid {
    //:: error: (assignment.type.incompatible)
    public @DataFlow(typeNames={"float", "java.lang.String"}) Object upperBoundTesting3_invalid(int c) {
        if (c > 0) {
            return 3;
        }
        return "I am a String!";
    }
}