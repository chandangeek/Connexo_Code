package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.NumberOfObjects;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:26:00
 */
public class RegisterQueryRequestStructure extends Data<RegisterQueryRequestStructure> {

    private CTRAbstractValue<String> pssw;
    private NumberOfObjects numberOfObjects;
    private AttributeType attributeType;
    private CTRObjectID[] id;

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

    @Override
    public RegisterQueryRequestStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType(0x00);
        type.setHasValueFields(true);

        int ptr = offset;

        pssw = factory.parse(rawData, ptr, type, "D.0.1").getValue()[0];
        ptr += pssw.getValueLength();

        numberOfObjects = new NumberOfObjects().parse(rawData, ptr);
        ptr += NumberOfObjects.LENGTH;

        attributeType = new AttributeType().parse(rawData, ptr);
        ptr += AttributeType.LENGTH;

        id = new CTRObjectID[numberOfObjects.getNumberOfObjects()];
        for (int i = 0; i < numberOfObjects.getNumberOfObjects(); i++) {
            byte[] b = ProtocolTools.getSubArray(rawData, ptr, ptr + CTRObjectID.LENGTH);
            id[i] = new CTRObjectID().parse(b, 0);
            ptr += CTRObjectID.LENGTH;
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