import dataflow.quals.DataFlow;

public class TestUpperBound3{
	public @dataflow.quals.DataFlow(typeNames={"int","java.lang.String"}) Object upperBoundTesting3(int c) {
		if (c > 0){
			return 3;
		}
		return "I am a String!";
	}
}