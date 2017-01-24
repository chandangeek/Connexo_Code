package com.energyict.protocolimpl.dlms.as220.objects;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.AbstractCosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ObjectReference;
import com.energyict.dlms.cosem.attributeobjects.Repeater;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 31-mei-2010
 * Time: 14:48:16
 */
public class PLCObject extends AbstractCosemObject {

    private static final byte[] LN = ObisCode.fromString("0.0.96.128.0.255").getLN();

    public static ObisCode getDefaultObisCode() {
        return ObisCode.fromByteArray(LN);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.DATA.getClassId();
    }

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public PLCObject(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    /**
     *
     * @param cosemObjectFactory
     * @throws IOException
     */
    public PLCObject(CosemObjectFactory cosemObjectFactory) throws IOException {
        super(cosemObjectFactory.getProtocolLink(), cosemObjectFactory.getObjectReference(PLCObject.getDefaultObisCode()));
    }

    /**
     * Get the logicalname of the object. Identifies the object instance.
     *
     * @return
     */
    public OctetString getLogicalName() {
        try {
            return new OctetString(getResponseData(PLCObjectAttribute.LOGICAL_NAME), 0);
        } catch (IOException e) {
            return null;
        }
    }

    public Repeater getRepeater() {
        try {
            return new Repeater(getResponseData(PLCObjectAttribute.REPEATER), 0);
        } catch (IOException e) {
            return null;
        }
    }

    public void setRepeater(Repeater repeater) throws IOException {
        write(PLCObjectAttribute.REPEATER, repeater.getBEREncodedByteArray());
    }

    @Override
    public String toString() {
        final String crlf = "\r\n";

        TypeEnum rep = getRepeater();

        StringBuffer sb = new StringBuffer();
        sb.append("SFSKPhyMacSetup").append(crlf);
        sb.append(" > repeater = ").append(rep != null ? rep.getValue() : null).append(crlf);
        return sb.toString();
    }


}
