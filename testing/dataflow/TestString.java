import dataflow.qual.DataFlow;

public class TestString {
    @DataFlow(typeNames={"java.lang.String"}) String stingTesting = "I am a String!";
}