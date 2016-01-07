import dataflow.quals.DataFlow;

public class TestUpperBound3Invalid{
	//:: error: (assignment.type.incompatible)
	public @dataflow.quals.DataFlow(typeNames={"float","java.lang.String"}) Object upperBoundTesting3_invalid(int c) {
		if (c > 0){
			return 3;
		}
		return "I am a String!";
	}
}