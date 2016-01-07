import dataflow.quals.DataFlow;

public class TestStringInvalid {
	//:: error: (assignment.type.incompatible)
	@dataflow.quals.DataFlow(typeNames={"java.lang.Object"}) String stingTesting_invalid = "I am a String!";
}