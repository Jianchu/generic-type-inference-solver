package dataflow.solver;

import org.checkerframework.javacutil.AnnotationUtils;

import java.util.Arrays;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.InferenceMain;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstantSlot;
import dataflow.CnfVecIntSerializer;
import dataflow.quals.DataFlowTop;
import dataflow.util.DataflowUtils;


public class DataflowSerializer extends CnfVecIntSerializer{
    private SlotManager slotManager;
    protected String datatype;

    public DataflowSerializer(String datatype) {
        super(InferenceMain.getInstance().getSlotManager());
        this.slotManager = InferenceMain.getInstance().getSlotManager();
        this.datatype = datatype;
    }

    @Override
    protected boolean isPresented(ConstantSlot constantSlot) {
        AnnotationMirror anno = constantSlot.getValue();
        return annoIsPresented(anno);
    }
    
    private boolean annoIsPresented(AnnotationMirror anno) {
        if (AnnotationUtils.areSameByClass(anno, DataFlowTop.class)) {
            return true;
        }
            String[] datatypes =  DataflowUtils.getDataflowValue(anno);
        return Arrays.asList(datatypes).contains(datatype);
    }
}
