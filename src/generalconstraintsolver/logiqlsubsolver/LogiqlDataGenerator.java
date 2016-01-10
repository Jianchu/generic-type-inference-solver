package generalconstraintsolver.logiqlsubsolver;

import generalconstraintsolver.ImpliesLogic;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class LogiqlDataGenerator {
    private String path = "";
    private List<ImpliesLogic> allImpliesLogic;
    public Set<Integer> slotRepresentSet = new HashSet<Integer>();
    public LogiqlDataGenerator(List<ImpliesLogic> allImpliesLogic, String path){
        this.allImpliesLogic = allImpliesLogic;
        this.path = path;
        generateData();
    }
    
    private void generateData(){
        StringBuilder logiqlData = new StringBuilder();
        for (ImpliesLogic res : allImpliesLogic) {
            if (res.singleVariable == true) {
                logiqlData.append("+variable(_" + res.variable + "),+hasvariableName[_"
                        + res.variable + "]=" + res.variable + ",+hasToBeTrue[_" +res.variable + "] = true.\n");
                slotRepresentSet.add(res.variable);
            } else {
                logiqlData.append("+variable(_" + res.leftSide.iterator().next().intValue() + "),+hasvariableName[_"
                        + res.leftSide.iterator().next().intValue() + "]=" + res.leftSide.iterator().next().intValue());
                for (Integer i :res.rightSide){
                    slotRepresentSet.add(i);
                    if (i.intValue() > 0){
                        logiqlData.append(",+variable(_" + i.intValue() + "),+hasvariableName[_"
                                + i.intValue() + "]=" + i.intValue());
                        if(res.rightSide.size() == 1){
                            logiqlData.append(",+rightSide[_"+ i.intValue() +"] = true"+",+implies" + "[_"+res.leftSide.iterator().next().intValue());
                        }
                    }                    
                    else{
                        logiqlData.append(",+variable(_" + Math.abs(i.intValue()) + "),+hasvariableName[_"
                                + Math.abs(i.intValue()) + "]=" + Math.abs(i.intValue()) + ",+rightSide[_"+ Math.abs(i.intValue()) +"] = false");
                    }
                }
                if (res.rightSide.size() > 1){
                    logiqlData.append(",+implies" + res.rightSide.size() + "[_"+res.leftSide.iterator().next().intValue());
                }                
                for (Integer i :res.rightSide){
                    logiqlData.append(",_"+i.intValue());
                }
                logiqlData.append("] = true.\n");
            }
        }
        //System.out.println(logiqlData);
        //System.out.println(path);
        //System.out.println(slotRepresentSet);
        writeFile(logiqlData.toString());
    }
    
    private void writeFile(String output) {
        try {
            String writePath = path + "/data.logic";
            File f = new File(writePath);
            PrintWriter pw = new PrintWriter(f);
            pw.write(output);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
