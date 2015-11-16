import dataflow.quals.DataFlow;
import java.util.ArrayList;

public class TestNewInvalid{
	//:: error: (assignment.type.incompatible)	
	@dataflow.quals.DataFlow(typeNames={"java.util.List"}) ArrayList newTesing_invalid = new ArrayList();
}