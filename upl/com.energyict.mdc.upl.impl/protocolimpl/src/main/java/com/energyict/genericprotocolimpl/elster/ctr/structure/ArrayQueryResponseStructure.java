package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:26:00
 */
public class ArrayQueryResponseStructure extends Data<ArrayQueryResponseStructure> {

    private Index_Q index_A;
    private Counter_Q counter_A;
    private CTRObjectID id;
    private Type type;
    private Coda coda;
    private DataArray data;

    public ArrayQueryResponseStructure(boolean longFrame) {
        super(longFrame);
    }

    @Override
    public byte[] getBytes() {
        return padData(ProtocolTools.concatByteArrays(
                id.getBytes(),
                type.getBytes(),
                index_A.getBytes(),
                counter_A.getBytes(),
                coda.getBytes(),
                data.getBytes()
        ));
    }

    @Override
    public ArrayQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        int ptr = offset;

        byte[] b = ProtocolTools.getSubArray(rawData, ptr, ptr + CTRObjectID.LENGTH);
        id = new CTRObjectID().parse(b, 0);
        ptr += CTRObjectID.LENGTH;

        type = new Type().parse(rawData, ptr);
        ptr += AttributeType.LENGTH;

        index_A = new Index_Q().parse(rawData, ptr);
        ptr += Index_Q.LENGTH;

        counter_A = new Counter_Q().parse(rawData, ptr);
        ptr += Counter_Q.LENGTH;

        coda = new Coda().parse(rawData, ptr);
        ptr += Coda.LENGTH;

        data = new DataArray(rawData.length - ptr).parse(rawData, ptr);

        return this;
    }

    public Index_Q getIndex_A() {
        return index_A;
    }

    public void setIndex_A(Index_Q index_A) {
        this.index_A = index_A;
    }

    public Counter_Q getCounter_A() {
        return counter_A;
    }

    public void setCounter_A(Counter_Q counter_A) {
        this.counter_A = counter_A;
    }

    public CTRObjectID getId() {
        return id;
    }

    public void setId(CTRObjectID id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Coda getCoda() {
        return coda;
    }

    public void setCoda(Coda coda) {
        this.coda = coda;
    }

    public DataArray getData() {
        return data;
    }

    public void setData(DataArray data) {
        this.data = data;
    }
}