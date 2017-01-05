package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.common.UkHubSecurityProvider;

import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY;
import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_ENCRYPTIONKEY;

/**
 * Provides property information for the InHomeDisplay
 */
public class InHomeDisplayProperties extends DlmsProtocolProperties {

    private static final String DEFAULT_IHD_CLIENT_MAC_ADDRESS = "64";

    /**
     * Uses the same logical device address as the HUB!!
     */
    private static final String DEFAULT_IHD_LOGICAL_DEVICE_ADDRESS = "1";

    private SecurityProvider securityProvider;

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.integer(SECURITY_LEVEL, true),
                UPLPropertySpecFactory.integer(ADDRESSING_MODE, false),
                UPLPropertySpecFactory.integer(CLIENT_MAC_ADDRESS, false),
                UPLPropertySpecFactory.string(SERVER_MAC_ADDRESS, false),
                UPLPropertySpecFactory.integer(CONNECTION, false),
                UPLPropertySpecFactory.integer(PK_FORCED_DELAY, false),
                UPLPropertySpecFactory.integer(PK_DELAY_AFTER_ERROR, false),
                UPLPropertySpecFactory.integer(INFORMATION_FIELD_SIZE, false),
                UPLPropertySpecFactory.integer(MAX_REC_PDU_SIZE, false),
                UPLPropertySpecFactory.integer(PK_RETRIES, false),
                UPLPropertySpecFactory.integer(PK_TIMEOUT, false),
                UPLPropertySpecFactory.integer(ROUND_TRIP_CORRECTION, false),
                UPLPropertySpecFactory.integer(BULK_REQUEST, false),
                UPLPropertySpecFactory.integer(CIPHERING_TYPE, false),
                UPLPropertySpecFactory.integer(NTA_SIMULATION_TOOL, false),
                UPLPropertySpecFactory.hexString(DATATRANSPORT_AUTHENTICATIONKEY, false),
                UPLPropertySpecFactory.hexString(DATATRANSPORT_ENCRYPTIONKEY, false));
    }

    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_IHD_CLIENT_MAC_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, DEFAULT_IHD_LOGICAL_DEVICE_ADDRESS);
    }

    public void setSecurityProvider(final UkHubSecurityProvider ukHubSecurityProvider) {
        this.securityProvider = ukHubSecurityProvider;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (this.securityProvider == null) {
            this.securityProvider = new UkHubSecurityProvider(getProtocolProperties());
        }
        return this.securityProvider;
    }

}