package dataflow.solver;

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
import dataflow.qual.DataFlowTop;
import dataflow.util.DataflowUtils;

public class DataflowSerializer extends CnfVecIntSerializer {
    // private SlotManager slotManager;
    protected final String datatype;
    private final Set<Integer> touchedSlots = new HashSet<Integer>();

    public DataflowSerializer(String datatype) {
        super(InferenceMain.getInstance().getSlotManager());
        // this.slotManager = InferenceMain.getInstance().getSlotManager();
        this.datatype = datatype;
        // System.out.println(datatype);
    }

    @Override
    protected boolean isTop(ConstantSlot constantSlot) {
        AnnotationMirror anno = constantSlot.getValue();
        return annoIsPresented(anno);
    }

    private boolean annoIsPresented(AnnotationMirror anno) {
        if (AnnotationUtils.areSameByClass(anno, DataFlowTop.class)) {
            return true;
        }
        String[] datatypes = DataflowUtils.getDataflowValue(anno);
        return Arrays.asList(datatypes).contains(datatype);
    }

    @Override
    public List<VecInt> convertAll(Iterable<Constraint> constraints,
            List<VecInt> results) {
        for (Constraint constraint : constraints) {
            for (VecInt res : constraint.serialize(this)) {
                if (res.size() != 0) {
                    results.add(res);
                }
            }
        }
        return results;
    }
}
