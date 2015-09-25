package GeneralMaxSatSolver;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.util.Collection;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.inference.InferenceMain;
import checkers.inference.InferenceSolution;
import checkers.inference.InferenceSolver;
import checkers.inference.SlotManager;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;

public class GeneralMaxSatSolver implements InferenceSolver{
    private Collection<Constraint> constraints;
    private GeneralCnfVecIntSerializer serializer;
    private SlotManager slotManager;
    private LatticeGenerator lattice;
    @Override
    public InferenceSolution solve(Map<String, String> configuration,
            Collection<Slot> slots, Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {
        
        this.constraints = constraints;
        this.slotManager = InferenceMain.getInstance().getSlotManager();
        this.lattice = new LatticeGenerator(qualHierarchy);
        this.serializer = new GeneralCnfVecIntSerializer(slotManager,lattice);
        return solve();
    }

    private InferenceSolution solve() {
        
                
        return null;
    }

}
