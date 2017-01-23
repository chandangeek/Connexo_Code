package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.attributes.WebPortalPasswordAttributes;
import com.energyict.dlms.cosem.methods.WebPortalPasswordMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 9/27/12
 * Time: 10:51 AM
 */
public class WebPortalPasswordConfig extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.128.0.13.255");

    /**
     * Creates a new instance of AbstractCosemObject
     */
    public WebPortalPasswordConfig(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.WEB_PORTAL_PASSWORDS.getClassId();
    }

    public void changeUser1Password(String newPassword) throws IOException {
        methodInvoke(WebPortalPasswordMethods.CHANGE_USER_1_PASSWORD, OctetString.fromString(newPassword));
    }

    public void changeUser2Password(String newPassword) throws IOException {
        methodInvoke(WebPortalPasswordMethods.CHANGE_USER_2_PASSWORD, OctetString.fromString(newPassword));
    }

    public AbstractDataType readLoginUser1() throws IOException {
        return readDataType(WebPortalPasswordAttributes.LOGIN_USER_1, OctetString.class);
    }

    public AbstractDataType readLoginUser2() throws IOException {
        return readDataType(WebPortalPasswordAttributes.LOGIN_USER_2, OctetString.class);
    }
}