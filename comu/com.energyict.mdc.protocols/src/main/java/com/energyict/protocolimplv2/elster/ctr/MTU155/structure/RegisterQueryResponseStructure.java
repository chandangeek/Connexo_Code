package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.NumberOfObjects;

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
            objectBytes = ProtocolTools.concatByteArrays(objectBytes, obj.getBytes());
        }
        return padData(ProtocolTools.concatByteArrays(
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
    public RegisterQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();

        int ptr = offset;
        
        numberOfObjects = new NumberOfObjects().parse(rawData, ptr);
        ptr += numberOfObjects.getLength();

        attributeType = new AttributeType().parse(rawData, ptr);
        ptr += attributeType.getLength();
        attributeType.setHasQualifier(true);  //The meter always sends a qlf
        attributeType.setHasIdentifier(true);
        attributeType.setRegisterQuery(true); //Skip the value if qlf is 0x0FF

        objects = new AbstractCTRObject[numberOfObjects.getNumberOfObjects()];
        for (int i = 0; i < numberOfObjects.getNumberOfObjects(); i++) {
            objects[i] = factory.parse(rawData, ptr, attributeType);
            ptr += objects[i].getLength();
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