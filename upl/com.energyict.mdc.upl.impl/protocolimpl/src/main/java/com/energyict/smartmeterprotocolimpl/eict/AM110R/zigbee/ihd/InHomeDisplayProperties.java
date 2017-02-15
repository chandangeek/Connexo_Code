package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.ihd;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
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

    public InHomeDisplayProperties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(this.getSmsWakeUpPropertySpecs(false));
        Stream.of(
                this.integerSpec(SECURITY_LEVEL, true, PropertyTranslationKeys.EICT_SECURITY_LEVEL),
                this.integerSpec(ADDRESSING_MODE, false, PropertyTranslationKeys.EICT_ADDRESSING_MODE),
                this.integerSpec(CLIENT_MAC_ADDRESS, false, PropertyTranslationKeys.EICT_CLIENT_MAC_ADDRESS),
                this.stringSpec(SERVER_MAC_ADDRESS, false, PropertyTranslationKeys.EICT_SERVER_MAC_ADDRESS),
                this.integerSpec(CONNECTION, false, PropertyTranslationKeys.EICT_CONNECTION),
                this.integerSpec(PK_FORCED_DELAY, false, PropertyTranslationKeys.EICT_FORCED_DELAY),
                this.integerSpec(PK_DELAY_AFTER_ERROR, false, PropertyTranslationKeys.EICT_DELAY_AFTER_ERROR),
                this.integerSpec(INFORMATION_FIELD_SIZE, false, PropertyTranslationKeys.EICT_INFORMATION_FIELD_SIZE),
                this.integerSpec(MAX_REC_PDU_SIZE, false, PropertyTranslationKeys.EICT_MAX_REC_PDU_SIZE),
                this.integerSpec(PK_RETRIES, false, PropertyTranslationKeys.EICT_RETRIES),
                this.integerSpec(PK_TIMEOUT, false, PropertyTranslationKeys.EICT_TIMEOUT),
                this.integerSpec(ROUND_TRIP_CORRECTION, false, PropertyTranslationKeys.EICT_ROUND_TRIP_CORRECTION),
                this.hexStringSpec(DATATRANSPORT_AUTHENTICATIONKEY, false, PropertyTranslationKeys.EICT_DATATRANSPORT_AUTHENTICATIONKEY),
                this.hexStringSpec(DATATRANSPORT_ENCRYPTIONKEY, false, PropertyTranslationKeys.EICT_DATATRANSPORT_ENCRYPTIONKEY),
                this.hexStringSpec(NEW_DATATRANSPORT_AUTHENTICATION_KEY, false, PropertyTranslationKeys.EICT_NEW_DATATRANSPORT_AUTHENTICATIONKEY),
                this.hexStringSpec(NEW_DATATRANSPORT_ENCRYPTION_KEY, false, PropertyTranslationKeys.EICT_NEW_DATATRANSPORT_ENCRYPTIONKEY),
                this.stringSpec(NEW_HLS_SECRET, false, PropertyTranslationKeys.EICT_NEW_HLS_SECRET),
                this.stringSpec(ZIGBEE_MAC, false, PropertyTranslationKeys.EICT_ZIGBEE_MAC),
                this.stringSpec(ZIGBEE_PCLK, false, PropertyTranslationKeys.EICT_ZIGBEE_PCLK),
                this.integerSpec(NTA_SIMULATION_TOOL, false, PropertyTranslationKeys.EICT_NTA_SIMULATION_TOOL),
                this.integerSpec(CIPHERING_TYPE, false, PropertyTranslationKeys.EICT_CIPHERING_TYPE),
                this.integerSpec(BULK_REQUEST, false, PropertyTranslationKeys.EICT_BULK_REQUEST))
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