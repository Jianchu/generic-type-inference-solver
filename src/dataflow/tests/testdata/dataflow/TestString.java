import dataflow.quals.DataFlow;

public class TestString{
	@dataflow.quals.DataFlow(typeNames={"java.lang.String"}) String stingTesting = "I am a String!";
}