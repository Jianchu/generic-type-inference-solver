package generalconstraintsolver;

import java.util.HashSet;
import java.util.Set;

//JLTODO: Documentation!
public class ImpliesLogic {
    public Set<Integer> leftSide = new HashSet<Integer>();
    public Set<Integer> rightSide = new HashSet<Integer>();

    // true for and, false for or
    public boolean insideLogic;

    public boolean singleVariable = false;

    public int variable = 0;

    public ImpliesLogic(int num_clause) {
        // JLTODO: the result of this call is not used. Remove?
        createImpliesArray(num_clause);
    }

    public ImpliesLogic(int variable, boolean singleVariable) {
        this.singleVariable = singleVariable;
        this.variable = variable;
    }

    public ImpliesLogic(int leftVariable, int rightVariable) {
        this.leftSide.add(leftVariable);
        this.rightSide.add(rightVariable);
    }

    public ImpliesLogic(int[] leftVariable, int[] rightVariable, String insideLogic) {
        addAll(leftVariable, this.leftSide);
        addAll(rightVariable, this.rightSide);
        if (insideLogic.equals("right-false")) {
            this.insideLogic = false;
        }
    }

    private ImpliesLogic[] createImpliesArray(int num_clause) {
        ImpliesLogic[] emptyArray = new ImpliesLogic[num_clause];
        return emptyArray;
    }

    private void addAll(int[] oneSide, Set<Integer> thisOneSide) {
        for (int i = 0; i < oneSide.length; i++) {
            thisOneSide.add(oneSide[i]);
        }
    }

    public int size() {
        return leftSide.size() + rightSide.size() + variable;
    }
}
