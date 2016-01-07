import dataflow.quals.DataFlow;
import java.util.ArrayList;

public class TestNew{
	@dataflow.quals.DataFlow(typeNames={"java.util.ArrayList"}) ArrayList newTesing = new ArrayList();
}