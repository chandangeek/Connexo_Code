package com.energyict.smartmeterprotocolimpl.eict.ukhub;

import com.energyict.dlms.DLMSReference;
import com.energyict.genericprotocolimpl.nta.abstractnta.NTASecurityProvider;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 20-jul-2011
 * Time: 13:29:29
 */
public class UkHubProperties extends DlmsProtocolProperties {

    public static final String DEFAULT_UK_HUB_CLIENT_MAC_ADDRESS = "64";
    public static final String DEFAULT_UK_HUB_LOGICAL_DEVICE_ADDRESS = "1";
    private static final String MaxReceivePduSize = "4096";
    private static final String DefaultZ3BulkRequesSupport = "1";

    public static final int FIRMWARE_CLIENT = 0x50;

    @Override
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {
        // nothing to do
    }

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(DlmsProtocolProperties.ADDRESSING_MODE);
        optional.add(DlmsProtocolProperties.CLIENT_MAC_ADDRESS);
        optional.add(DlmsProtocolProperties.SERVER_MAC_ADDRESS);
        optional.add(DlmsProtocolProperties.CONNECTION);
        optional.add(DlmsProtocolProperties.FORCED_DELAY);
        optional.add(DlmsProtocolProperties.DELAY_AFTER_ERROR);
        optional.add(DlmsProtocolProperties.INFORMATION_FIELD_SIZE);
        optional.add(DlmsProtocolProperties.MAX_REC_PDU_SIZE);
        optional.add(DlmsProtocolProperties.RETRIES);
        optional.add(DlmsProtocolProperties.ROUND_TRIP_CORRECTION);
        optional.add(DlmsProtocolProperties.BULK_REQUEST);
        optional.add(DlmsProtocolProperties.CIPHERING_TYPE);
        optional.add(DlmsProtocolProperties.NTA_SIMULATION_TOOL);
        optional.add(NTASecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY);
        optional.add(NTASecurityProvider.DATATRANSPORT_ENCRYPTIONKEY);
        optional.add(NTASecurityProvider.NEW_DATATRANSPORT_ENCRYPTION_KEY);
        optional.add(NTASecurityProvider.NEW_DATATRANSPORT_AUTHENTICATION_KEY);
        optional.add(NTASecurityProvider.NEW_HLS_SECRET);
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
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_UK_HUB_CLIENT_MAC_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, DEFAULT_UK_HUB_LOGICAL_DEVICE_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public int getMaxRecPDUSize() {
        return getIntProperty(MAX_REC_PDU_SIZE, MaxReceivePduSize);
    }

    @ProtocolProperty
    @Override
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, DefaultZ3BulkRequesSupport);
    }

    public boolean isFirmwareUpdateSession() {
        return getClientMacAddress() == FIRMWARE_CLIENT;
    }

}
