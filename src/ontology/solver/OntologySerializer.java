package ontology.solver;

import ontology.qual.OntologyTop;
import ontology.util.OntologyUtils;

import org.checkerframework.javacutil.AnnotationUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;

import checkers.inference.InferenceMain;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.serialization.CnfVecIntSerializer;

public class OntologySerializer extends CnfVecIntSerializer {

    protected final String value;
    private final Set<Integer> touchedSlots = new HashSet<Integer>();

    public OntologySerializer(String value) {
        super(InferenceMain.getInstance().getSlotManager());
        this.value = value;
    }

    @Override
    protected boolean isTop(ConstantSlot constantSlot) {
        AnnotationMirror anno = constantSlot.getValue();
        return annoIsPresented(anno);
    }

    private boolean annoIsPresented(AnnotationMirror anno) {
        if (AnnotationUtils.areSameByClass(anno, OntologyTop.class)) {
            return true;
        }
        String[] values = OntologyUtils.getOntologyValue(anno);
        return Arrays.asList(values).contains(value);
    }

    @Override
    public List<VecInt> convertAll(Iterable<Constraint> constraints,
            List<VecInt> results) {
        for (Constraint constraint : constraints) {
                for (VecInt res : constraint.serialize(this)) {
                    results.add(res);
                }
        }
        return results;
    }
}
