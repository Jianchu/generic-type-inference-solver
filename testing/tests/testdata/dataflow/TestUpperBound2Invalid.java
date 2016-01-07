import dataflow.quals.DataFlow;

public class TestUpperBound2Invalid{
	//:: error: (assignment.type.incompatible)
	public @dataflow.quals.DataFlow(typeNames={"java.lang.Object"}) Object upperBoundTesting2_invalid(int c) {
		return "I am a String!";
	}
}