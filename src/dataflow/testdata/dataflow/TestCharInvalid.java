import dataflow.quals.DataFlow;

public class TestCharInvalid{
	//:: error: (assignment.type.incompatible)
	@dataflow.quals.DataFlow(typeNames={"int"}) char charTesting_invalid = 'L';
} 