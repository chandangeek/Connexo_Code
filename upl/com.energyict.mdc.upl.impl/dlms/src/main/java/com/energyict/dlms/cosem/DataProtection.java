package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.attributeobjects.dataprotection.RequiredProtection;
import com.energyict.dlms.cosem.attributes.DataProtectionAttributes;
import com.energyict.dlms.cosem.methods.DataProtectionMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Created by cisac on 12/14/2016.
 */
public class DataProtection extends AbstractCosemObject{

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.43.2.0.255");

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public DataProtection(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.DATA_PROTECTION.getClassId();
    }

    public OctetString getProtectionBufferAttribute() throws IOException {
        return new OctetString(getResponseData(DataProtectionAttributes.PROTECTION_BUFFER));
    }

    public void writeProtectionBufferAttribute(OctetString protectionBuffer) throws IOException {
        write(DataProtectionAttributes.PROTECTION_BUFFER, protectionBuffer.getBEREncodedByteArray());
    }

    public Array getProtectionObjectListAttribute() throws IOException {
        return new Array(getResponseData(DataProtectionAttributes.PROTECTION_OBJECT_LIST), 0, 0);
    }

    public void writeProtectionBufferAttribute(Array protectionObjectList) throws IOException {
        write(DataProtectionAttributes.PROTECTION_OBJECT_LIST, protectionObjectList.getBEREncodedByteArray());
    }

    public Array getProtectionParametersGetAttribute() throws IOException {
        return new Array(getResponseData(DataProtectionAttributes.PROTECTION_PARAMETERS_GET), 0, 0);
    }

    public void writeProtectionParametersGetAttribute(Array protectionObjectList) throws IOException {
        write(DataProtectionAttributes.PROTECTION_PARAMETERS_GET, protectionObjectList.getBEREncodedByteArray());
    }

    public Array getProtectionParametersSetAttribute() throws IOException {
        return new Array(getResponseData(DataProtectionAttributes.PROTECTION_PARAMETERS_SET), 0, 0);
    }

    public void writeProtectionParametersSetAttribute(Array protectionObjectList) throws IOException {
        write(DataProtectionAttributes.PROTECTION_PARAMETERS_SET, protectionObjectList.getBEREncodedByteArray());
    }

    public TypeEnum getRequiredProtectionAttribute() throws IOException {
        return new TypeEnum(getResponseData(DataProtectionAttributes.REQUIRED_PROTECTION), 0);
    }

    public void writeRequiredProtectionAttribute(RequiredProtection requiredProtection) throws IOException {
        write(DataProtectionAttributes.REQUIRED_PROTECTION, requiredProtection.getTypeEnum().getBEREncodedByteArray());
    }

    public Structure getProtectedAttributes(Structure data) throws IOException {
        return new Structure(methodInvoke(DataProtectionMethods.GET_PROTECTED_ATTRIBUTES, data), 0, 0);
    }

    public void setProtectedAttributes(Structure data) throws IOException {
        methodInvoke(DataProtectionMethods.SET_PROTECTED_ATTRIBUTES, data);
    }

    public Structure invokeProtectedMethod(Structure data) throws IOException {
        return new Structure(methodInvoke(DataProtectionMethods.INVOKE_PROTECTED_METHOD, data), 0, 0);
    }

}
