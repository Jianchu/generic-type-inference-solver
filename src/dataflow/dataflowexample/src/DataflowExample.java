package DataflowExample;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

public class DataflowExample {
    int thisIsInt = 3;

    char thisIsChar = 'c';

    String thisIsString = "String!";
    Object thisShouldbeString = thisIsString;

    Date date = new Date();

    public Object testingUpperBound(int intpara) {

        if (intpara == 1) {
            return 3;
        } else if (intpara == 2) {
            return 3.14;
        } else if (intpara == 3) {
            return 3.1415926;
        }
        return "I am a String!";
    }
}
