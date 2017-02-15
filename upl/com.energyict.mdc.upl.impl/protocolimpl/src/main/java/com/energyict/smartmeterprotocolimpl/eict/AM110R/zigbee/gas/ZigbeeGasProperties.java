package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas;

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

import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY;
import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_ENCRYPTIONKEY;

/**
 * Copyrights EnergyICT
 * Date: 20-jul-2011
 * Time: 13:29:29
 */
public class ZigbeeGasProperties extends SmsWakeUpDlmsProtocolProperties {

    private static final String DEFAULT_ZIGBEE_GAS_CLIENT_MAC_ADDRESS = "64";
    public static final int FIRMWARE_CLIENT = 0x50;

    private static final String LOGBOOK_SELECTOR = "LogbookSelector";
    private static final String DEFAULT_LOGBOOK_SELECTOR = "-1";

    private static final String VERIFY_FIRMWARE_VERSION = "VerifyFirmwareVersion";
    private static final String DEFAULT_VERIFY_FIRMWARE_VERSION = "0";

    private static final String ZIGBEE_MAC = "ZigbeeMAC";
    private static final String ZIGBEE_PCLK = "ZigbeePCLK";

    /**
     * Default it starts at 30, but if more devices are supported then it can go from 30 to 45
     */
    private static final String DEFAULT_ZIGBEE_GAS_LOGICAL_DEVICE_ADDRESS = "30";

    private SecurityProvider securityProvider;

    public ZigbeeGasProperties(PropertySpecService propertySpecService) {
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
                this.integerSpec(MAX_REC_PDU_SIZE, false, PropertyTranslationKeys.EICT_MAX_REC_PDU_SIZE),
                this.integerSpec(PK_RETRIES, false, PropertyTranslationKeys.EICT_RETRIES),
                this.integerSpec(PK_TIMEOUT, false, PropertyTranslationKeys.EICT_TIMEOUT),
                this.integerSpec(ROUND_TRIP_CORRECTION, false, PropertyTranslationKeys.EICT_ROUND_TRIP_CORRECTION),
                this.hexStringSpec(DATATRANSPORT_AUTHENTICATIONKEY, false, PropertyTranslationKeys.EICT_DATATRANSPORT_AUTHENTICATIONKEY),
                this.hexStringSpec(DATATRANSPORT_ENCRYPTIONKEY, false, PropertyTranslationKeys.EICT_DATATRANSPORT_ENCRYPTIONKEY),
                this.integerSpec(LOGBOOK_SELECTOR, false, PropertyTranslationKeys.EICT_LOGBOOK_SELECTOR),
                this.integerSpec(VERIFY_FIRMWARE_VERSION, false, PropertyTranslationKeys.EICT_VERIFY_FIRMWARE_VERSION),
                this.stringSpec(ZIGBEE_MAC, false, PropertyTranslationKeys.EICT_ZIGBEE_MAC),
                this.stringSpec(ZIGBEE_PCLK, false, PropertyTranslationKeys.EICT_ZIGBEE_PCLK))
            .forEach(propertySpecs::add);
        return propertySpecs;
    }

    @ProtocolProperty
    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_ZIGBEE_GAS_CLIENT_MAC_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, DEFAULT_ZIGBEE_GAS_LOGICAL_DEVICE_ADDRESS);
    }

    public void setSecurityProvider(final AM110RSecurityProvider am110RSecurityProvider) {
        this.securityProvider = am110RSecurityProvider;
    }

    public boolean isFirmwareUpdateSession() {
        return getClientMacAddress() == FIRMWARE_CLIENT;
    }

    public boolean verifyFirmwareVersion() {
        return getBooleanProperty(VERIFY_FIRMWARE_VERSION, DEFAULT_VERIFY_FIRMWARE_VERSION);
    }

    /**
     * Getter for the LogBookSelector bitmask
     *
     * @return the bitmask, containing which event logbooks that should be read out.
     */
    public int getLogbookSelector() {
        return getIntProperty(LOGBOOK_SELECTOR, DEFAULT_LOGBOOK_SELECTOR);
    }

    public String getZigbeeMAC() {
        return getStringValue(ZIGBEE_MAC, "");
    }

    public String getZigbeePCLK() {
        return getStringValue(ZIGBEE_PCLK, "");
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (this.securityProvider == null) {
            this.securityProvider = new AM110RSecurityProvider(getProtocolProperties());
        }
        return this.securityProvider;
    }

}