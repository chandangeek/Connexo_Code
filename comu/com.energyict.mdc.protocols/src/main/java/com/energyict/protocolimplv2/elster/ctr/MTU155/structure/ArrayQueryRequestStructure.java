package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRAbstractValue;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Counter_Q;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Index_Q;

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

    /**
     * Create a CTR Structure Object representing the given byte array
     * @param rawData: a given byte array
     * @param offset: the start position in the array
     * @return the CTR Structure Object
     * @throws CTRParsingException
     */
    @Override
    public ArrayQueryRequestStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType(0x00);
        type.setHasValueFields(true);

        int ptr = offset;

        pssw = factory.parse(rawData, ptr, type, "D.0.1").getValue()[0];
        ptr += pssw.getValueLength();

        id = new CTRObjectID().parse(rawData, ptr);
        ptr += id.getLength();

        index_Q = new Index_Q().parse(rawData, ptr);
        ptr += index_Q.getLength();

        counter_Q = new Counter_Q().parse(rawData, ptr);
        ptr += counter_Q.getLength();

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