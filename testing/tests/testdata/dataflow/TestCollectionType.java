import dataflow.quals.DataFlow;
import java.util.ArrayList;

public class TestCollectionType{
	@dataflow.quals.DataFlow(typeNames={"java.util.ArrayList<java.lang.String>"}) ArrayList<String> collectionTypeTesing = new ArrayList<String>();
}