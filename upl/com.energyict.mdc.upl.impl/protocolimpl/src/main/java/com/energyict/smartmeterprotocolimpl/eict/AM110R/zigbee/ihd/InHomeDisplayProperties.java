package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.ihd;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.AM110RSecurityProvider;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.SmsWakeUpDlmsProtocolProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.energyict.smartmeterprotocolimpl.eict.AM110R.common.AM110RSecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY;
import static com.energyict.smartmeterprotocolimpl.eict.AM110R.common.AM110RSecurityProvider.DATATRANSPORT_ENCRYPTIONKEY;
import static com.energyict.smartmeterprotocolimpl.eict.AM110R.common.AM110RSecurityProvider.NEW_DATATRANSPORT_AUTHENTICATION_KEY;
import static com.energyict.smartmeterprotocolimpl.eict.AM110R.common.AM110RSecurityProvider.NEW_DATATRANSPORT_ENCRYPTION_KEY;
import static com.energyict.smartmeterprotocolimpl.eict.AM110R.common.AM110RSecurityProvider.NEW_HLS_SECRET;


/**
 * Provides property information for the InHomeDisplay
 */
public class InHomeDisplayProperties extends SmsWakeUpDlmsProtocolProperties {

    private static final String ZIGBEE_MAC = "ZigbeeMAC";
    private static final String ZIGBEE_PCLK = "ZigbeePCLK";

    private static final String DEFAULT_IHD_CLIENT_MAC_ADDRESS = "64";

    /**
     * Uses the same logical device address as the HUB!!
     */
    private static final String DEFAULT_IHD_LOGICAL_DEVICE_ADDRESS = "1";

    private SecurityProvider securityProvider;

    public InHomeDisplayProperties() {
        super(propertySpecService);
    }


    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(this.getSmsWakeUpPropertySpecs(false));
        Stream.of(
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
                UPLPropertySpecFactory.hexString(DATATRANSPORT_AUTHENTICATIONKEY, false),
                UPLPropertySpecFactory.hexString(DATATRANSPORT_ENCRYPTIONKEY, false),
                UPLPropertySpecFactory.hexString(NEW_DATATRANSPORT_AUTHENTICATION_KEY, false),
                UPLPropertySpecFactory.hexString(NEW_DATATRANSPORT_ENCRYPTION_KEY, false),
                UPLPropertySpecFactory.string(NEW_HLS_SECRET, false),
                UPLPropertySpecFactory.string(ZIGBEE_MAC, false),
                UPLPropertySpecFactory.string(ZIGBEE_PCLK, false),
                UPLPropertySpecFactory.integer(NTA_SIMULATION_TOOL, false),
                UPLPropertySpecFactory.integer(CIPHERING_TYPE, false),
                UPLPropertySpecFactory.integer(BULK_REQUEST, false))
            .forEach(propertySpecs::add);
        return propertySpecs;
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

    public String getZigbeePclk() {
        return getStringValue(ZIGBEE_PCLK, "");
    }

    public String getZigbeeMac() {
        return getStringValue(ZIGBEE_MAC, "");
    }

    public void setSecurityProvider(final AM110RSecurityProvider ukHubSecurityProvider) {
        this.securityProvider = ukHubSecurityProvider;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (this.securityProvider == null) {
            this.securityProvider = new AM110RSecurityProvider(getProtocolProperties());
        }
        return this.securityProvider;
    }

}