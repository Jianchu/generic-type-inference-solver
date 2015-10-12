package checkers.inference.solver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.WeightedMaxSatDecorator;

import checkers.inference.SlotManager;

public class SatSubSolver {
    private List<ImpliesLogic> allImpliesLogic;
    private SlotManager slotManager;
    private LatticeGenerator lattice;
    Map<Integer, Collection<Integer>> typeForSlot = new HashMap<Integer, Collection<Integer>>();
    
    public SatSubSolver(List<ImpliesLogic> allImpliesLogic, SlotManager slotManager, LatticeGenerator lattice){
        this.allImpliesLogic = allImpliesLogic;
        this.slotManager = slotManager;
        this.lattice = lattice;
        satSolve();
    }
    
    VecInt asVec(int... result) {
        return new VecInt(result);
    }

    VecInt[] asVecArray(int... vars) {
        return new VecInt[] { new VecInt(vars) };
    }
        
    public List<VecInt> convertImpliesToClauses(){
        List<VecInt> result = new ArrayList<VecInt>();
        for (ImpliesLogic res :allImpliesLogic ){
            if (res.singleVariable == true){
                result.add(asVec(res.variable));
                //System.out.println("just: " + res.variable);
            }
            else{
                int[] toBevecArray= new int[res.leftSide.size()+res.rightSide.size()];
                toBevecArray[0] = -res.leftSide.iterator().next().intValue();
                int i = 1;
                for (Integer imp : res.rightSide){
                    if (res.insideLogic == false){
                        toBevecArray[i] = imp.intValue();
                        i++;
                    }
                }
                result.add(asVec(toBevecArray));
                //System.out.println("left: " + res.leftSide.toString()+ " ---> " + "right: " + res.rightSide.toString());
            }
        }       
        return result;
    }

    private void satSolve(){
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        List<VecInt> clauses = convertImpliesToClauses();        
        final int totalVars = (slotManager.nextId() * lattice.numModifiers);
        final int totalClauses = clauses.size();
        final WeightedMaxSatDecorator solver = new WeightedMaxSatDecorator(
                org.sat4j.pb.SolverFactory.newBoth());
        
        solver.newVar(totalVars);
        solver.setExpectedNumberOfClauses(totalClauses);
        solver.setTimeoutMs(1000000);
        VecInt lastClause = null;       
        try {
            for (VecInt clause : clauses) {
                //System.out.println(clause);
                lastClause = clause;
                solver.addHardClause(clause);
            }
            if (solver.isSatisfiable()) {
                int[] solution = solver.model();
                DecodingTool decoder = new DecodingTool(solution, lattice);
                result = decoder.result;
                System.out.println("/*****************result from Sat Solver*******************/");
                for (Integer j: result.keySet()){
                    System.out.println("SlotID: "+j+ "  " + "Annotation: " + result.get(j).toString());
                }
                System.out.flush();
                System.out.println("/**********************************************************/");
                
            } else {
                System.out.println("Not solvable!");
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
