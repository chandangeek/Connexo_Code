package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
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
    public List<PropertySpec> getPropertySpecs() {
        PropertySpecService propertySpecService = this.getPropertySpecService();
        List<PropertySpec> propertySpecs = new ArrayList<>(this.getSmsWakeUpPropertySpecs(false));
        Stream.of(
                this.spec(SECURITY_LEVEL, true, propertySpecService::integerSpec),
                this.spec(ADDRESSING_MODE, false, propertySpecService::integerSpec),
                this.spec(CLIENT_MAC_ADDRESS, false, propertySpecService::integerSpec),
                this.spec(SERVER_MAC_ADDRESS, false, propertySpecService::stringSpec),
                this.spec(CONNECTION, false, propertySpecService::integerSpec),
                this.spec(PK_FORCED_DELAY, false, propertySpecService::integerSpec),
                this.spec(MAX_REC_PDU_SIZE, false, propertySpecService::integerSpec),
                this.spec(PK_RETRIES, false, propertySpecService::integerSpec),
                this.spec(PK_TIMEOUT, false, propertySpecService::integerSpec),
                this.spec(ROUND_TRIP_CORRECTION, false, propertySpecService::integerSpec),
                this.spec(DATATRANSPORT_AUTHENTICATIONKEY, false, propertySpecService::hexStringSpec),
                this.spec(DATATRANSPORT_ENCRYPTIONKEY, false, propertySpecService::hexStringSpec),
                this.spec(LOGBOOK_SELECTOR, false, propertySpecService::integerSpec),
                this.spec(VERIFY_FIRMWARE_VERSION, false, propertySpecService::integerSpec),
                this.spec(ZIGBEE_MAC, false, propertySpecService::stringSpec),
                this.spec(ZIGBEE_PCLK, false, propertySpecService::stringSpec))
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