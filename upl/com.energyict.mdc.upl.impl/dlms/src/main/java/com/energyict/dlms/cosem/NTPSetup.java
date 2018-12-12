package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.attributes.NTPSetupAttributes;
import com.energyict.dlms.cosem.methods.NTPSetupMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Created by H245796 on 18.12.2017.
 */
public class NTPSetup extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.25.10.0.255");

    /**
     * Creates a new instance of AbstractCosemObject
     */
    public NTPSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.NTP_SETUP.getClassId();
    }

    public AbstractDataType readLogicalName() throws IOException {
        return readDataType(NTPSetupAttributes.LOGICAL_NAME, OctetString.class);
    }

    public AbstractDataType readActivated() throws IOException {
        return readDataType(NTPSetupAttributes.ACTIVATED, BooleanObject.class);
    }

    public AbstractDataType readServerAddress() throws IOException {
        return readDataType(NTPSetupAttributes.SERVER_ADDRESS, OctetString.class);
    }

    public AbstractDataType readServerPort() throws IOException {
        return readDataType(NTPSetupAttributes.SERVER_PORT, Unsigned16.class);
    }

    public void writeNTPAttribute(NTPSetupAttributes attribute, AbstractDataType data) throws IOException {
        write(attribute, data);
    }

    public void invokeNTPMethod(NTPSetupMethods ntpSetupMethod, AbstractDataType data) throws IOException {
        methodInvoke(ntpSetupMethod, data);
    }

}