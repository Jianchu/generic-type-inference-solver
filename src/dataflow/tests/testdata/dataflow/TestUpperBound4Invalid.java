import dataflow.quals.DataFlow;

public class TestUpperBound4Invalid{
	//:: error: (assignment.type.incompatible)
	public @dataflow.quals.DataFlow(typeNames={"java.lang.Object"}) Object upperBoundTesting4_invalid(int c) {
		if (c > 0){
			return "I am a String!";
		}
		return "I am a String too!";
	}
}