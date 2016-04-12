import dataflow.qual.DataFlow;

public class TestUpperBound1 {
    public @DataFlow(typeNames={"int"}) int upperBoundTesting1(int c) {
        return 3;
    }
}