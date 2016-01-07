import dataflow.quals.DataFlow;

public class TestIntInvalid {
	//:: error: (assignment.type.incompatible)
	@dataflow.quals.DataFlow(typeNames={"float"}) int intTesting_invalid = 3;
}