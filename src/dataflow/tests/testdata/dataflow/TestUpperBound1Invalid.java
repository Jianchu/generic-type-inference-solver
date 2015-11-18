import dataflow.quals.DataFlow;

public class TestUpperBound1Invalid{
	//:: error: (assignment.type.incompatible)
	public @dataflow.quals.DataFlow(typeNames={"float"}) int upperBoundTesting1_invalid(int c) {
		return 3;
	}
}