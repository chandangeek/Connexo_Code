package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.AM110RSecurityProvider;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.SmsWakeUpDlmsProtocolProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 20-jul-2011
 * Time: 13:29:29
 */
public class ZigbeeGasProperties extends SmsWakeUpDlmsProtocolProperties {

    public static final String DEFAULT_ZIGBEE_GAS_CLIENT_MAC_ADDRESS = "64";
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
    public static final String DEFAULT_ZIGBEE_GAS_LOGICAL_DEVICE_ADDRESS = "30";

    private SecurityProvider securityProvider;

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {
        // nothing to do
    }

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.addAll(super.getOptionalSmsWakeUpKeys());
        optional.add(DlmsProtocolProperties.CONNECTION);
        optional.add(DlmsProtocolProperties.CLIENT_MAC_ADDRESS);
        optional.add(DlmsProtocolProperties.SERVER_MAC_ADDRESS);
        optional.add(DlmsProtocolProperties.ADDRESSING_MODE);
        optional.add(DlmsProtocolProperties.MAX_REC_PDU_SIZE);
        optional.add(DlmsProtocolProperties.RETRIES);
        optional.add(DlmsProtocolProperties.TIMEOUT);
        optional.add(DlmsProtocolProperties.FORCED_DELAY);
        optional.add(DlmsProtocolProperties.ROUND_TRIP_CORRECTION);

        optional.add(AM110RSecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY);
        optional.add(AM110RSecurityProvider.DATATRANSPORT_ENCRYPTIONKEY);
        optional.add(VERIFY_FIRMWARE_VERSION);
        optional.add(LOGBOOK_SELECTOR);
        optional.add(ZIGBEE_MAC);
        optional.add(ZIGBEE_PCLK);
        return optional;
    }

    public List<String> getRequiredKeys() {
        ArrayList<String> required = new ArrayList<String>();
        required.add(DlmsProtocolProperties.SECURITY_LEVEL);
        return required;
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
