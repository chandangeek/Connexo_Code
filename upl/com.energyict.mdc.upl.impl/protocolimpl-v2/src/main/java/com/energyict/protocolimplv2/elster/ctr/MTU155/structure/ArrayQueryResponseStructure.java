package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.*;

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

    /**
     * Create a CTR Structure Object representing the given byte array
     * @param rawData: a given byte array
     * @param offset: the start position in the array
     * @return the CTR Structure Object
     * @throws CTRParsingException
     */
    @Override
    public ArrayQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        int ptr = offset;

        id = new CTRObjectID().parse(rawData, ptr);
        ptr += id.getLength();

        type = new Type().parse(rawData, ptr);
        ptr += type.getLength();

        index_A = new Index_Q().parse(rawData, ptr);
        ptr += index_A.getLength();

        counter_A = new Counter_Q().parse(rawData, ptr);
        ptr += counter_A.getLength();

        coda = new Coda().parse(rawData, ptr);
        ptr += coda.getLength();

        data = new DataArray(rawData.length - ptr).parse(rawData, ptr);
        //TODO: parse the data array into objects

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