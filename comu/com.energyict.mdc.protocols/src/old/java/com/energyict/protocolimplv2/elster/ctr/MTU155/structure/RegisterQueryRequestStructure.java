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
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.NumberOfObjects;

public class RegisterQueryRequestStructure extends Data<RegisterQueryRequestStructure> {

    private CTRAbstractValue<String> pssw;
    private NumberOfObjects numberOfObjects;
    private AttributeType attributeType;
    private CTRObjectID[] id;

    public RegisterQueryRequestStructure(boolean longFrame) {
        super(longFrame);
    }

    @Override
    public byte[] getBytes() {
        byte[] idBytes = null;
        for (CTRObjectID singleId : id) {
            idBytes = ProtocolTools.concatByteArrays(idBytes, singleId.getBytes());
        }
        return padData(ProtocolTools.concatByteArrays(
                pssw.getBytes(),
                numberOfObjects.getBytes(),
                attributeType.getBytes(),
                idBytes
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
    public RegisterQueryRequestStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType(0x00);
        type.setHasValueFields(true);

        int ptr = offset;

        pssw = factory.parse(rawData, ptr, type, "D.0.1").getValue()[0];
        ptr += pssw.getValueLength();

        numberOfObjects = new NumberOfObjects().parse(rawData, ptr);
        ptr += numberOfObjects.getLength();

        attributeType = new AttributeType().parse(rawData, ptr);
        ptr += attributeType.getLength();

        id = new CTRObjectID[numberOfObjects.getNumberOfObjects()];
        for (int i = 0; i < numberOfObjects.getNumberOfObjects(); i++) {
            id[i] = new CTRObjectID().parse(rawData, ptr);
            ptr += id[i].getLength();
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

    public CTRObjectID[] getId() {
        return id;
    }

    public void setId(CTRObjectID[] id) {
        this.id = id;
    }
}