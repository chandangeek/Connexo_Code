package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.attributes.CRLManagementICAttributes;
import com.energyict.dlms.cosem.methods.CRLManagementICMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Created by cisac on 5/9/2017.
 */
public class CRLManagementIC extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.144.96.144.0.255");

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public CRLManagementIC(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.CRL_MANAGEMENT_IC.getClassId();
    }

    public Array getCrlList() throws IOException {
        return new Array(getResponseData(CRLManagementICAttributes.CRL_LIST), 0, 0);
    }

    public void updateCRL(OctetString derEncodedCRL) throws IOException {
        methodInvoke(CRLManagementICMethods.UPDATE_CRL, derEncodedCRL);
    }

    public void removeCRL(OctetString nameOfCRLissuer) throws IOException {
        methodInvoke(CRLManagementICMethods.REMOVE_CRL, nameOfCRLissuer);
    }
}
