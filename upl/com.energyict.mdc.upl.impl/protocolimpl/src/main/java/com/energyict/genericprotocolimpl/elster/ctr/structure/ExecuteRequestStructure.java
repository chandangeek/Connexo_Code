package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.ReferenceDate;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.WriteDataBlock;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:26:00
 */
public class ExecuteRequestStructure extends Data<ExecuteRequestStructure> {

    private CTRAbstractValue<String> pssw;
    private ReferenceDate validityDate;
    private WriteDataBlock wdb;
    private CTRObjectID id;
    private byte[] data;


    public ExecuteRequestStructure(boolean longFrame) {
        super(longFrame);
    }

    @Override
    public byte[] getBytes() {

        return padData(ProtocolTools.concatByteArrays(
                pssw.getBytes(),
                validityDate.getBytes(),
                wdb.getBytes(),
                id.getBytes(),
                data
        ));
    }

    public CTRObjectID getId() {
        return id;
    }

    public void setId(CTRObjectID id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Create a CTR Structure Object representing the given byte array
     * @param rawData: a given byte array
     * @param offset: the start position in the array
     * @return the CTR Structure Object
     * @throws CTRParsingException
     */
    @Override
    public ExecuteRequestStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType(0x00);
        type.setHasValueFields(true);

        int ptr = offset;

        pssw = factory.parse(rawData, ptr, type, "D.0.1").getValue()[0];
        ptr += pssw.getValueLength();

        validityDate = new ReferenceDate().parse(rawData, ptr);
        ptr += validityDate.getLength();

        wdb = new WriteDataBlock().parse(rawData, ptr);
        ptr += wdb.getLength();

        id = new CTRObjectID().parse(rawData, ptr);
        ptr += id.getLength();

        data = ProtocolTools.getSubArray(rawData, ptr);

        return this;
    }

    public CTRAbstractValue<String> getPssw() {
        return pssw;
    }

    public void setPssw(CTRAbstractValue<String> pssw) {
        this.pssw = pssw;
    }

    public ReferenceDate getValidityDate() {
        return validityDate;
    }

    public void setValidityDate(ReferenceDate validityDate) {
        this.validityDate = validityDate;
    }

    public WriteDataBlock getWdb() {
        return wdb;
    }

    public void setWdb(WriteDataBlock wdb) {
        this.wdb = wdb;
    }
}