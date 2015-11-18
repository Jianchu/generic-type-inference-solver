import dataflow.quals.DataFlow;

public class TestDoubleInvalid{
	//:: error: (assignment.type.incompatible)
	@dataflow.quals.DataFlow(typeNames={"int"}) double floatTesting_invalid = 3.14;
}