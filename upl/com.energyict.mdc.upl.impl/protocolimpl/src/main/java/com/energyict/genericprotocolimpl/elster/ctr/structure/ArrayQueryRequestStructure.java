package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.Counter_Q;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.Index_Q;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:26:00
 */
public class ArrayQueryRequestStructure extends Data<ArrayQueryRequestStructure> {

    private CTRAbstractValue<String> pssw;
    private Index_Q index_Q;
    private Counter_Q counter_Q;
    private CTRObjectID id;

    public ArrayQueryRequestStructure(boolean longFrame) {
        super(longFrame);
    }

    @Override
    public byte[] getBytes() {
        return padData(ProtocolTools.concatByteArrays(
                pssw.getBytes(),
                id.getBytes(),
                index_Q.getBytes(),
                counter_Q.getBytes()
        ));
    }

    @Override
    public ArrayQueryRequestStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType(0x00);
        type.setHasValueFields(true);

        int ptr = offset;

        pssw = factory.parse(rawData, ptr, type, "D.0.1").getValue()[0];
        ptr += pssw.getValueLength();

        byte[] b = ProtocolTools.getSubArray(rawData, ptr, ptr + CTRObjectID.LENGTH);
        id = new CTRObjectID().parse(b, 0);
        ptr += CTRObjectID.LENGTH;

        index_Q = new Index_Q().parse(rawData, ptr);
        ptr += Index_Q.LENGTH;

        counter_Q = new Counter_Q().parse(rawData, ptr);
        ptr += Counter_Q.LENGTH;

        return this;
    }

    public CTRAbstractValue<String> getPssw() {
        return pssw;
    }

    public void setPssw(CTRAbstractValue<String> pssw) {
        this.pssw = pssw;
    }

    public Index_Q getIndex_Q() {
        return index_Q;
    }

    public void setIndex_Q(Index_Q index_Q) {
        this.index_Q = index_Q;
    }

    public Counter_Q getCounter_Q() {
        return counter_Q;
    }

    public void setCounter_Q(Counter_Q counter_Q) {
        this.counter_Q = counter_Q;
    }

    public CTRObjectID getId() {
        return id;
    }

    public void setId(CTRObjectID id) {
        this.id = id;
    }
}