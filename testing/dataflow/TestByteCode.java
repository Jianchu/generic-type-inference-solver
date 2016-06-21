import dataflow.qual.*;
import java.util.*;
public class TestByteCode {

	int a = 3;

	Object m() {
		if(a > 0) {
			return new Object().toString();
		} else if (a < 0) {
			return new ArrayList<String>();
		} else {
			return 3;
		}
	}

	// @DataFlow(typeNames="java.util.ArrayList<java.lang.String>")
	//Object o = new ArrayList<String>();
	//@DataFlow(typeNameRoots="java.lang.Object")  
	//String s = new Object().toString();
	//@DataFlow(typeNameRoots="java.lang.String", typeNames={"int", "java.util.ArrayList<java.lang.String>"})
	//@DataFlow(typeNameRoots="java.lang.Object")  
}