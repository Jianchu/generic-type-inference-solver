package generalconstraintsolver.logiqlsubsolver;

import java.io.File;
import java.io.PrintWriter;

public class LogiqlPredicateGenerator {
    private final int max_Clauses;
    private final String path;

    public LogiqlPredicateGenerator(int max_Clauses, String path) {
        this.max_Clauses = max_Clauses;
        this.path = path;
        // JLTODO: it's not really nice that the constructor already does
        // everything. The two invocations of the constructor assigned the
        // result to a variable, which was then never used. It would be cleaner
        // to move this out.
        generatePredicate();
    }

    private void generateBasicPredicate(StringBuilder basicPredicate) {
        basicPredicate
                .append("variable(v),hasvariableName(v:i)->int(i)."
                        + "\nisAnnotated[v] = i -> variable(v), boolean(i)."
                        +"\nvariableOrder(v)->int(v)."
                        +"\nvariableOrder(v)<-variable(v)."
                        +"\norderVariable[o]=v -> int(o),int(v)."
                        +"\norderVariable[o]=v <- seq<<o=v>>variableOrder(v)."
                        +"\norderedAnnotationOf[v]= a -> int(v),boolean(a)."
                        +"\norderedAnnotationOf[v]= a <- isAnnotated[v]=a,orderVariable[_]=v."
                        + "\nhasToBeTrue[v] = h -> variable(v), boolean(h)."
                        + "\nset_1_variable[v] = s -> variable(v), boolean(s)."
                        + "\nset_1_variable[v] = true <- hasToBeTrue[v] = true."
                        + "\nimplies[v1,v2] = e -> variable(v1), variable(v2), boolean(e)."
                        + "\nrightSide[v] = r -> variable(v), boolean(r)."
                        + "\nset_1_variable[v2] = true <- implies[v1,v2] = true, set_1_variable[v1] = true, rightSide[v2] = true."
                        + "\nset_1_variable[v2] = false <- implies[v1,v2] = true, set_1_variable[v1] = true, rightSide[v2] = false."
                        + "\nisAnnotated[v] = true <- set_1_variable[v] = true."
                        + "\nisAnnotated[v] = false <- set_1_variable[v] = false.");
    }

    private void generateRightSide(StringBuilder rightSide) {
        StringBuilder impliesTable = new StringBuilder();
        StringBuilder set_num_table = new StringBuilder();
        StringBuilder set_num_table_condition = new StringBuilder();
        for (int i = 2; i <= max_Clauses + 1; i++) {
            impliesTable.append("implies" + i + "[v1,v2");
            set_num_table.append("set_"+i+"_variable[v1");
            set_num_table_condition.append("set_"+i+"_variable[v2");
            for (int j = 2; j <= i; j++) {
                impliesTable.append(",v"+(j+1));
                set_num_table.append(",v"+j);
                set_num_table_condition.append(",v"+(j+1));
            }
            impliesTable.append("] = e -> variable(v1), variable(v2)");
            set_num_table.append("] = e ->variable(v1)");
            set_num_table_condition.append("] = true <- implies" + i + "[v1,v2");
            for (int j = 2; j <= i; j++) {
                impliesTable.append(",variable(v"+(j+1)+")");
                set_num_table.append(",variable(v"+j+")");
                set_num_table_condition.append(",v"+(j+1));
            }
            impliesTable.append(", boolean(e).\n");
            set_num_table.append(", boolean(e).\n");
            set_num_table_condition.append("] = true, set_1_variable[v1] = true; isAnnotated[v1] = true, implies" + i + "[v1,v2");
            for (int j = 2; j <= i; j++) {
                set_num_table_condition.append(",v"+(j+1));
            }
            set_num_table_condition.append("] = true.\n");
        }
        rightSide.append(impliesTable).append(set_num_table).append(set_num_table_condition);
        for (int i = 2; i <= max_Clauses + 1; i++) {
            for (int j = 1; j <= i; j++) {
                StringBuilder isAnnotated_condition = new StringBuilder();
                    isAnnotated_condition.append("isAnnotated[v"+j+"] = true <- set_" + (i)+ "_variable[v1");
                for (int k = 2; k <= j; k++) {
                    isAnnotated_condition.append(",v"+k);
                }
                for (int k = 0; k < i - j; k++) {
                    isAnnotated_condition.append(",_");
                }
                isAnnotated_condition.append("] = true");
                for (int k = 0; k < j - 1; k++) {
                    isAnnotated_condition.append(", isAnnotated[v"+(k+1)+"] = false");
                }
                isAnnotated_condition.append(",!set_1_variable[v" + j + "] = false.\n");
                rightSide.append(isAnnotated_condition);
            }
        }
    }

    private void generatePredicate() {
        StringBuilder basicPredicate = new StringBuilder();
        generateBasicPredicate(basicPredicate);
        StringBuilder rightSide = new StringBuilder();
        generateRightSide(rightSide);
        StringBuilder finalPredicate = new StringBuilder();
        finalPredicate.append(basicPredicate).append(rightSide);
        //System.out.println(finalPredicate.toString());
        //System.out.println(path);
        writeFile(finalPredicate.toString());
    }

    private void writeFile(String output) {
        try {
            String writePath = path + "/LogiqlEncoding.logic";
            String writeDeletePath = path + "/deleteData.logic";
            String deleteContent = "-variable(v) <- variable@prev(v).";
            File f = new File(writePath);
            PrintWriter pw = new PrintWriter(f);
            pw.write(output);
            pw.close();
            File df = new File(writeDeletePath);
            PrintWriter dpw = new PrintWriter(df);
            dpw.write(deleteContent);
            dpw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
