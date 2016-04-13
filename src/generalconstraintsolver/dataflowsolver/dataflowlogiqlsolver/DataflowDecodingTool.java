package generalconstraintsolver.dataflowsolver.dataflowlogiqlsolver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import generalconstraintsolver.DecodingTool;
import generalconstraintsolver.LatticeGenerator;

public class DataflowDecodingTool extends DecodingTool {

    public DataflowDecodingTool(String path, LatticeGenerator lattice) {
        super(path, lattice);
    }

    @Override
    public void decodeLogicBloxResult() throws FileNotFoundException {
        String readPath = path;
        InputStream in = new FileInputStream(readPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                String[] s = line.split(" ");
                if (s[1].equals("true")) {
                    int var = Integer.parseInt(s[0]);
                    mapSlot_ModifierRep(var);
                }
            }
            decodeSolverResult(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
