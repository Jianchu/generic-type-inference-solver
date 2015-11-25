package checkers.inference.solver.generalconstrainssolver.satsubsolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.WeightedMaxSatDecorator;

import checkers.inference.DefaultInferenceSolution;
import checkers.inference.InferenceSolution;
import checkers.inference.SlotManager;
import checkers.inference.solver.generalconstrainssolver.DecodingTool;
import checkers.inference.solver.generalconstrainssolver.ImpliesLogic;
import checkers.inference.solver.generalconstrainssolver.LatticeGenerator;

public class SatSubSolver {
    private List<ImpliesLogic> allImpliesLogic;
    private SlotManager slotManager;
    private LatticeGenerator lattice;
    private Set<Integer> slotRepresentSet = new HashSet<Integer>();

    public SatSubSolver(List<ImpliesLogic> allImpliesLogic,
            SlotManager slotManager, LatticeGenerator lattice) {
        this.allImpliesLogic = allImpliesLogic;
        this.slotManager = slotManager;
        this.lattice = lattice;
    }

    private VecInt asVec(int... result) {
        return new VecInt(result);
    }

    private boolean isLast(Integer var) {
        return (Math.abs(var.intValue()) % lattice.numModifiers == 0);
    }

    private int findSlotId(Integer var) {
        return (Math.abs(var.intValue()) / lattice.numModifiers + 1);

    }

    public List<VecInt> convertImpliesToClauses() {
        List<VecInt> result = new ArrayList<VecInt>();
        for (ImpliesLogic res : allImpliesLogic) {
            if (res.singleVariable == true) {
                result.add(asVec(res.variable));
                slotRepresentSet.add(res.variable);
                // System.out.println("just: " + res.variable);
            } else {
                int[] toBevecArray = new int[res.leftSide.size()
                        + res.rightSide.size()];
                toBevecArray[0] = -res.leftSide.iterator().next().intValue();
                int i = 1;
                for (Integer imp : res.rightSide) {
                    slotRepresentSet.add(imp);
                    if (res.insideLogic == false) {
                        toBevecArray[i] = imp.intValue();
                        i++;
                    }
                }
                result.add(asVec(toBevecArray));
                // System.out.println("left: " + res.leftSide.toString()+
                // " ---> " + "right: " + res.rightSide.toString());
            }
        }
        return result;
    }

    private void becomeWellForm(List<VecInt> clauses) {
        Set<Integer> slotId = new HashSet<Integer>();
        for (Integer slotRep : slotRepresentSet) {
            if (isLast(slotRep.intValue())) {
                slotId.add(Math.abs(slotRep) / lattice.numModifiers);
            } else {
                slotId.add(findSlotId(slotRep));
            }
        }
        for (Integer id : slotId) {
            int[] wellFormFirst = new int[lattice.numModifiers];
            int j = 0;
            for (Integer i : lattice.IntModifier.keySet()) {
                wellFormFirst[j] = lattice.numModifiers * (id - 1)
                        + i.intValue();
                j++;
            }
            //clauses.add(asVec(wellFormFirst));
            int[] wellFormFollow = new int[2];
            Iterator<Integer> intRep1 = lattice.IntModifier.keySet().iterator();
            Set<Integer> intRep2 = lattice.IntModifier.keySet();
            while(intRep1.hasNext()) {
                Integer int1= intRep1.next();
                for (Integer int2: intRep2) {
                    if (int2.intValue() != int1.intValue()) {
                        wellFormFollow[0] = -(lattice.numModifiers * (id - 1) + int1
                                .intValue());
                        wellFormFollow[1] = -(lattice.numModifiers * (id - 1) + int2
                                .intValue());
                        clauses.add(asVec(wellFormFollow));
                    }
                }
            }
        }
    }

    public InferenceSolution satSolve() {
        Map<Integer, Boolean> idToExistence = new HashMap<>();
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        List<VecInt> clauses = convertImpliesToClauses();
        becomeWellForm(clauses);
        final int totalVars = (slotManager.nextId() * lattice.numModifiers);
        final int totalClauses = clauses.size() + slotManager.nextId() * (1+(lattice.numModifiers*(lattice.numModifiers-1)/2));
        final WeightedMaxSatDecorator solver = new WeightedMaxSatDecorator(
                org.sat4j.pb.SolverFactory.newBoth());

        solver.newVar(totalVars);
        solver.setExpectedNumberOfClauses(totalClauses);
        solver.setTimeoutMs(1000000);
        VecInt lastClause = null;
        try {
            for (VecInt clause : clauses) {
                // System.out.println(clause);
                lastClause = clause;
                solver.addHardClause(clause);
            }
            if (solver.isSatisfiable()) {
                int[] solution = solver.model();
                DecodingTool decoder = new DecodingTool(solution, lattice);
                result = decoder.result;
            } else {
                System.out.println("Not solvable!");
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return new DefaultInferenceSolution(result, idToExistence);
    }
    
}
