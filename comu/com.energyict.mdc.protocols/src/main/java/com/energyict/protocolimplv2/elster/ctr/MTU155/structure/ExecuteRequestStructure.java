/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRAbstractValue;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ReferenceDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;

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