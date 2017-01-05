package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.ConnectionMode;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 14/12/11
 * Time: 9:22
 */
class ZMDProperties extends DlmsProtocolProperties {

    private static final String DEFAULT_MAX_REC_PDU_SIZE = "-1";
    private static final String DEFAULT_ADDRESSING_MODE = "-1";
    private static final String DEFAULT_CLIENT_MAC_ADDRESS = "32";

    private SecurityProvider securityProvider;

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.integer(SECURITY_LEVEL, false),
                UPLPropertySpecFactory.integer(ADDRESSING_MODE, false),
                UPLPropertySpecFactory.integer(CLIENT_MAC_ADDRESS, false),
                UPLPropertySpecFactory.string(SERVER_MAC_ADDRESS, false),
                UPLPropertySpecFactory.integer(CONNECTION, false),
                UPLPropertySpecFactory.integer(PK_DELAY_AFTER_ERROR, false),
                UPLPropertySpecFactory.integer(INFORMATION_FIELD_SIZE, false),
                UPLPropertySpecFactory.integer(MAX_REC_PDU_SIZE, false),
                UPLPropertySpecFactory.integer(PK_RETRIES, false),
                UPLPropertySpecFactory.integer(PK_TIMEOUT, false),
                UPLPropertySpecFactory.integer(ROUND_TRIP_CORRECTION, false),
                UPLPropertySpecFactory.integer(BULK_REQUEST, false),
                UPLPropertySpecFactory.integer(CIPHERING_TYPE, false),
                UPLPropertySpecFactory.integer(INVOKE_ID_AND_PRIORITY, false));
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