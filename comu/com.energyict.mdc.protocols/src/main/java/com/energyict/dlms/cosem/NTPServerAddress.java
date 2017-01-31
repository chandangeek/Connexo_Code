/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.attributes.NPTServerAddressAttributes;
import com.energyict.dlms.cosem.methods.NTPServerAddressMethods;

import java.io.IOException;

public class NTPServerAddress extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.128.0.10.255");

    /**
     * Creates a new instance of AbstractCosemObject
     */
    public NTPServerAddress(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.NTP_SERVER_ADDRESS.getClassId();
    }

    public void ntpSync() throws IOException {
        methodInvoke(NTPServerAddressMethods.NTP_SYNC, new NullData().getBEREncodedByteArray());
    }

    public AbstractDataType readNTPServerName() throws IOException {
        return readDataType(NPTServerAddressAttributes.NTP_SERVER_NAME, OctetString.class);
    }

    public void writeNTPServerName(String serverName) throws IOException {
        write(NPTServerAddressAttributes.NTP_SERVER_NAME, OctetString.fromString(serverName));
    }
}