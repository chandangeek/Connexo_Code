package com.energyict.smartmeterprotocolimpl.eict.AM110R;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.AM110RSecurityProvider;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.MultipleClientRelatedObisCodes;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.SmsWakeUpDlmsProtocolProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 20-jul-2011
 * Time: 13:29:29
 */
public class AM110RProperties extends SmsWakeUpDlmsProtocolProperties {

    public static final String DEFAULT_UK_HUB_CLIENT_MAC_ADDRESS = "64";
    public static final String DEFAULT_AM110R_HUB_LOGICAL_DEVICE_ADDRESS = "1";
    private static final String MaxReceivePduSize = "4096";
    private static final String DefaultZ3BulkRequesSupport = "1";
    private static final String VERIFY_FIRMWARE_VERSION = "VerifyFirmwareVersion";
    private static final String DEFAULT_VERIFY_FIRMWARE_VERSION = "0";
    private static final String LOGBOOK_SELECTOR = "LogbookSelector";
    private static final String DEFAULT_LOGBOOK_SELECTOR = "-1";

    private SecurityProvider securityProvider;

    @ProtocolProperty
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

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
        return optional;
    }

    public List<String> getRequiredKeys() {
        ArrayList<String> required = new ArrayList<String>();
        required.add(DlmsProtocolProperties.SECURITY_LEVEL);
        return required;
    }

    @ProtocolProperty
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_UK_HUB_CLIENT_MAC_ADDRESS);
    }

    @ProtocolProperty
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, DEFAULT_AM110R_HUB_LOGICAL_DEVICE_ADDRESS);
    }

    @ProtocolProperty
    public int getMaxRecPDUSize() {
        return getIntProperty(MAX_REC_PDU_SIZE, MaxReceivePduSize);
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

    @ProtocolProperty
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, DefaultZ3BulkRequesSupport);
    }

    public boolean isFirmwareUpdateSession() {
        return getClientMacAddress() == MultipleClientRelatedObisCodes.FIRMWARE_CLIENT.getClientId();
    }

    public void setSecurityProvider(AM110RSecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (this.securityProvider == null) {
            this.securityProvider = new AM110RSecurityProvider(getProtocolProperties());
        }
        return this.securityProvider;
    }
}
