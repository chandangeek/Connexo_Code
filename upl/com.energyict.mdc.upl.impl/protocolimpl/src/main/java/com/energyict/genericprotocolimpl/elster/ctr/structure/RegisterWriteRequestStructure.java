package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.*;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:26:00
 */
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
            objectBytes = ProtocolTools.concatByteArrays(objectBytes, object.getBytes(attributeType));
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

        objects = new AbstractCTRObject[numberOfObjects.getNumberOfObjects()];
        for (int i = 0; i < numberOfObjects.getNumberOfObjects(); i++) {
            objects[i] = factory.parse(rawData, ptr, attributeType);
            ptr += objects[i].getLength(attributeType);
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