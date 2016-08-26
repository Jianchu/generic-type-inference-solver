import java.util.*;

public class TestStrToType {
	
	Object o;

	void bar() {
		if(true) {
			o = new Integer[1];
		} else {
			o = new Object().toString();
		}
	}
}