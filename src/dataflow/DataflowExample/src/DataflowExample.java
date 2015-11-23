package DataflowExample;
import dataflow.quals.DataFlow;

public class DataflowExample {
	@dataflow.quals.DataFlow(typeNames={"java.lang.String"}) String annotatedString = "I am a String!";
	String insertAnnotationToThis = annotatedString;
}
