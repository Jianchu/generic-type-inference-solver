import dataflow.qual.DataFlow;
import java.util.ArrayList;

public class TestCollectionType {
    @DataFlow(typeNames={"java.util.ArrayList<java.lang.String>"}) ArrayList<String> collectionTypeTesing = new ArrayList<String>();
}