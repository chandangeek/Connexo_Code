package com.energyict.smartmeterprotocolimpl.elster.AS300P;

import com.energyict.dlms.ConnectionMode;
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
 * Date: 7-feb-2011
 * Time: 14:16:15
 */
public class AS300PProperties extends SmsWakeUpDlmsProtocolProperties {

    public static final String DEFAULT_AS300_CLIENT_MAC_ADDRESS = "64";
    public static final String DEFAULT_AS300_LOGICAL_DEVICE_ADDRESS = "45";

    private static final String LOGBOOK_SELECTOR = "LogbookSelector";
    private static final String DEFAULT_LOGBOOK_SELECTOR = "-1";

    private static final String VERIFY_FIRMWARE_VERSION = "VerifyFirmwareVersion";
    private static final String DEFAULT_VERIFY_FIRMWARE_VERSION = "0";

    private static final int FIRMWARE_CLIENT = 0x50;
    private static final String MaxReceivePduSize_Optical = "276";
    private static final String MaxReceivePduSize_TCP_IP = "1070";

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
        List<String> required = new ArrayList<String>();
        required.add(DlmsProtocolProperties.SECURITY_LEVEL);
        return required;
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {
    }

    @ProtocolProperty
    @Override
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, DEFAULT_AS300_LOGICAL_DEVICE_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_AS300_CLIENT_MAC_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, "1");
    }

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    public boolean isFirmwareUpdateSession() {
        return getClientMacAddress() == FIRMWARE_CLIENT;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if(super.securityProvider == null){
            setSecurityProvider(new AM110RSecurityProvider(getProtocolProperties()));
        }
        return super.securityProvider;
    }

    public void setSecurityProvider(SecurityProvider securityProvider){
        super.securityProvider = securityProvider;
    }

    @ProtocolProperty
    @Override
    public int getMaxRecPDUSize() {
        if (getConnectionMode().equals(ConnectionMode.HDLC)) {
            return getIntProperty(MAX_REC_PDU_SIZE, MaxReceivePduSize_Optical);
        } else {
            return getIntProperty(MAX_REC_PDU_SIZE, MaxReceivePduSize_TCP_IP);
        }
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
}
