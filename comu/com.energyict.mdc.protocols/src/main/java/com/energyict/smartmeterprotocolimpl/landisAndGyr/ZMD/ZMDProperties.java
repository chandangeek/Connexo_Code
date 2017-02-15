/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;

import com.energyict.dlms.ConnectionMode;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;

import java.util.ArrayList;
import java.util.List;

public class ZMDProperties extends DlmsProtocolProperties {

    private static String DEFAULT_MAX_REC_PDU_SIZE = "-1";
    private static String DEFAULT_ADDRESSING_MODE = "-1";
    private static String DEFAULT_CLIENT_MAC_ADDRESS = "32";

    private SecurityProvider securityProvider;

    public List<String> getOptionalKeys() {
        List result = new ArrayList();
        result.add(TIMEOUT);
        result.add(RETRIES);
        result.add(DELAY_AFTER_ERROR);
        result.add(SERVER_MAC_ADDRESS);
        result.add(SECURITY_LEVEL);
        result.add(CLIENT_MAC_ADDRESS);
        result.add(ADDRESSING_MODE);
        result.add(CONNECTION);
        result.add(CIPHERING_TYPE);
        result.add(INVOKE_ID_AND_PRIORITY);
        result.add(MAX_REC_PDU_SIZE);

        result.add("RequestTimeZone");
        result.add("EventIdIndex");
        return result;
    }

    public List<String> getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        return required;
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {
    }

    @Override
    public int getMaxRecPDUSize() {
        return getIntProperty(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE);
    }

    @Override
    public int getAddressingMode() {
        return getIntProperty(ADDRESSING_MODE, DEFAULT_ADDRESSING_MODE);
    }

    @Override
    public int getClientMacAddress() {
         return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_CLIENT_MAC_ADDRESS);
    }

    @ProtocolProperty
    public int getRequestTimeZone() {
        return getIntProperty("RequestTimeZone","0");
    }

    @ProtocolProperty
    public int getEventIdIndex() {
        return getIntProperty("EventIdIndex","-1");
    }

    public DLMSReference getReference() {
        return DLMSReference.SN;
    }

    @Override
    public long getConformance() {
        if (isSNReference()) {
            return getLongProperty(CONFORMANCE_BLOCK_VALUE, Long.toString(1573408L));
        } else if (isLNReference()) {
            return getLongProperty(CONFORMANCE_BLOCK_VALUE, DEFAULT_CONFORMANCE_BLOCK_VALUE_LN);
        } else {
            return 0;
        }
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (this.securityProvider == null) {
            this.securityProvider = new ZMDSecurityProvider(getProtocolProperties());
        }
        return this.securityProvider;
    }

    @ProtocolProperty
    public String getManufacturer() {
        return getStringValue(MANUFACTURER, "LGZ");
    }

    @Override
    public byte[] getSystemIdentifier() {
        return "".getBytes();
    }

    @Override
    public ConnectionMode getConnectionMode() {
        return ConnectionMode.HDLC;
    }
}