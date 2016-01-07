import dataflow.quals.DataFlow;
import java.util.ArrayList;

public class TestCollectionTypeInvalid{
	//:: error: (assignment.type.incompatible)
	@dataflow.quals.DataFlow(typeNames={"java.util.ArrayList<Object>"}) ArrayList collectionTypeTesing_invalid = new ArrayList<String>();
}