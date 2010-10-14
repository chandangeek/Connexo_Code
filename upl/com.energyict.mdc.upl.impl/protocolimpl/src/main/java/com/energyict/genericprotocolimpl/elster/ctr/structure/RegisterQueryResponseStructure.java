package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.NumberOfObjects;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 11-okt-2010
 * Time: 9:50:05
 */
public class RegisterQueryResponseStructure extends Data<RegisterQueryResponseStructure> {

    private AbstractCTRObject[] objects;
    private NumberOfObjects numberOfObjects;
    private AttributeType attributeType;

    public RegisterQueryResponseStructure(boolean longFrame) {
        super(longFrame);
    }

    @Override
    public byte[] getBytes() {
        byte[] objectBytes = null;
        for (AbstractCTRObject obj : objects) {
            objectBytes = ProtocolTools.concatByteArrays(objectBytes, obj.getBytes(attributeType));
        }
        return padData(ProtocolTools.concatByteArrays(
                numberOfObjects.getBytes(),
                attributeType.getBytes(),
                objectBytes
        ));
    }

    @Override
    public RegisterQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();

        int ptr = offset;
        
        numberOfObjects = new NumberOfObjects().parse(rawData, ptr);
        ptr += NumberOfObjects.LENGTH;

        attributeType = new AttributeType().parse(rawData, ptr);
        ptr += AttributeType.LENGTH;

        objects = new AbstractCTRObject[numberOfObjects.getNumberOfObjects()];
        for (int i = 0; i < numberOfObjects.getNumberOfObjects(); i++) {
            objects[i] = factory.parse(rawData, ptr, attributeType);
            ptr += objects[i].getBytes(attributeType).length;
        }

        return this;
    }

    public AbstractCTRObject[] getObjects() {
        return objects;
    }

    public void setObjects(AbstractCTRObject[] objects) {
        this.objects = objects;
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
}
