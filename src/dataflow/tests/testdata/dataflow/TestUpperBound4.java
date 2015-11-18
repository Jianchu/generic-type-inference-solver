import dataflow.quals.DataFlow;

public class TestUpperBound4{
	public @dataflow.quals.DataFlow(typeNames={"java.lang.String"}) Object upperBoundTesting4(int c) {
		if (c > 0){
			return "I am a String!";
		}
		return "I am a String too!";
	}
}