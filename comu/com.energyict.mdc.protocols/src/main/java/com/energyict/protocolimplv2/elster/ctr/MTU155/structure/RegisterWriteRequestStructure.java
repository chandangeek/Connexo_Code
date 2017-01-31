/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRAbstractValue;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.NumberOfObjects;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.P_Session;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ReferenceDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;

public class RegisterWriteRequestStructure extends Data<RegisterWriteRequestStructure> {

    private CTRAbstractValue<String> pssw;
    private ReferenceDate validityDate;
    private WriteDataBlock wdb;
    private P_Session p_session;
    private NumberOfObjects numberOfObjects;
    private AttributeType attributeType;
    private AbstractCTRObject[] objects;

    public RegisterWriteRequestStructure(boolean longFrame) {
        super(longFrame);
    }

    @Override
    public byte[] getBytes() {

        byte[] objectBytes = new byte[]{};
        for (AbstractCTRObject object : objects) {
            objectBytes = ProtocolTools.concatByteArrays(objectBytes, object.getBytes());
        }

        return padData(ProtocolTools.concatByteArrays(
                pssw.getBytes(),
                validityDate.getBytes(),
                wdb.getBytes(),
                p_session.getBytes(),
                numberOfObjects.getBytes(),
                attributeType.getBytes(),
                objectBytes
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
    public RegisterWriteRequestStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType(0x00);
        type.setHasValueFields(true);

        int ptr = offset;

        pssw = factory.parse(rawData, ptr, type, "D.0.1").getValue()[0];
        ptr += pssw.getValueLength();

        validityDate = new ReferenceDate().parse(rawData, ptr);
        ptr+= validityDate.getLength();

        wdb = new WriteDataBlock().parse(rawData, ptr);
        ptr+= wdb.getLength();

        p_session = new P_Session().parse(rawData, ptr);
        ptr+= p_session.getLength();

        numberOfObjects = new NumberOfObjects().parse(rawData, ptr);
        ptr += numberOfObjects.getLength();

        attributeType = new AttributeType().parse(rawData, ptr);
        ptr += attributeType.getLength();
        attributeType.setHasIdentifier(true);      //The ID is always sent with the object, in this case 

        objects = new AbstractCTRObject[numberOfObjects.getNumberOfObjects()];
        for (int i = 0; i < numberOfObjects.getNumberOfObjects(); i++) {
            objects[i] = factory.parse(rawData, ptr, attributeType);
            ptr += objects[i].getLength();
        }

        return this;
    }

    public CTRAbstractValue<String> getPssw() {
        return pssw;
    }

    public void setPssw(CTRAbstractValue<String> pssw) {
        this.pssw = pssw;
    }

    public NumberOfObjects getNumberOfObjects() {
        return numberOfObjects;
    }

    public void setNumberOfObjects(NumberOfObjects numberOfObjects) {
        this.numberOfObjects = numberOfObjects;
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
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

    public P_Session getP_session() {
        return p_session;
    }

    public void setP_session(P_Session p_session) {
        this.p_session = p_session;
    }

    public AbstractCTRObject[] getObjects() {
        return objects;
    }

    public void setObjects(AbstractCTRObject[] objects) {
        this.objects = objects;
    }
}