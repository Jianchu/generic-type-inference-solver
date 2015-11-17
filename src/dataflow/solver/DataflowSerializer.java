package dataflow.solver;

import checkers.inference.InferenceMain;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstantSlot;
import dataflow.CnfVecIntSerializer;


public class DataflowSerializer extends CnfVecIntSerializer{
    private SlotManager slotManager;
    protected String datatype;

    public DataflowSerializer(String datatype) {
        super(InferenceMain.getInstance().getSlotManager());
        this.slotManager = InferenceMain.getInstance().getSlotManager();
        this.datatype = datatype;
    }

    @Override
    protected boolean isTop(ConstantSlot constantSlot) {
        return false;
    }
}
