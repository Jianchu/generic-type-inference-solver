import dataflow.qual.DataFlow;

public class TestUpperBound1Invalid {
    //:: error: (assignment.type.incompatible)
    public @DataFlow(typeNames={"float"}) int upperBoundTesting1_invalid(int c) {
        return 3;
    }
}