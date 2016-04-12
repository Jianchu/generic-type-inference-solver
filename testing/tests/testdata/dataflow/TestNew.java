import dataflow.qual.DataFlow;
import java.util.ArrayList;

public class TestNew {
    @DataFlow(typeNames={"java.util.ArrayList"}) ArrayList newTesing = new ArrayList();
}