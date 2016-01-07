import dataflow.quals.DataFlow;

public class TestUpperBound1{
	public @dataflow.quals.DataFlow(typeNames={"int"}) int upperBoundTesting1(int c) {
		return 3;
	}	
}