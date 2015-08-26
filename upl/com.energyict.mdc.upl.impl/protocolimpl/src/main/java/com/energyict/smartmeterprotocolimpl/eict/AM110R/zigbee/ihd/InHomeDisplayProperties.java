package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.ihd;

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
 * Provides property information for the InHomeDisplay
 */
public class InHomeDisplayProperties extends SmsWakeUpDlmsProtocolProperties {

    private static final String ZIGBEE_MAC = "ZigbeeMAC";
    private static final String ZIGBEE_PCLK = "ZigbeePCLK";

    public static final String DEFAULT_IHD_CLIENT_MAC_ADDRESS = "64";

    /**
     * Uses the same logical device address as the HUB!!
     */
    public static final String DEFAULT_IHD_LOGICAL_DEVICE_ADDRESS = "1";

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
        optional.add(DlmsProtocolProperties.ADDRESSING_MODE);
        optional.add(DlmsProtocolProperties.CLIENT_MAC_ADDRESS);
        optional.add(DlmsProtocolProperties.SERVER_MAC_ADDRESS);
        optional.add(DlmsProtocolProperties.CONNECTION);
        optional.add(DlmsProtocolProperties.SERVER_MAC_ADDRESS);
        optional.add(DlmsProtocolProperties.FORCED_DELAY);
        optional.add(DlmsProtocolProperties.DELAY_AFTER_ERROR);
        optional.add(DlmsProtocolProperties.INFORMATION_FIELD_SIZE);
        optional.add(DlmsProtocolProperties.MAX_REC_PDU_SIZE);
        optional.add(DlmsProtocolProperties.RETRIES);
        optional.add(DlmsProtocolProperties.TIMEOUT);
        optional.add(DlmsProtocolProperties.ROUND_TRIP_CORRECTION);
        optional.add(DlmsProtocolProperties.BULK_REQUEST);
        optional.add(DlmsProtocolProperties.CIPHERING_TYPE);
        optional.add(DlmsProtocolProperties.NTA_SIMULATION_TOOL);
        optional.add(AM110RSecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY);
        optional.add(AM110RSecurityProvider.DATATRANSPORT_ENCRYPTIONKEY);
        optional.add(AM110RSecurityProvider.NEW_DATATRANSPORT_ENCRYPTION_KEY);
        optional.add(AM110RSecurityProvider.NEW_DATATRANSPORT_AUTHENTICATION_KEY);
        optional.add(AM110RSecurityProvider.NEW_HLS_SECRET);
        optional.add(ZIGBEE_MAC);
        optional.add(ZIGBEE_PCLK);
        return optional;
    }

    public List<String> getRequiredKeys() {
        ArrayList<String> required = new ArrayList<String>();
        required.add(DlmsProtocolProperties.SECURITY_LEVEL);
        return required;
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