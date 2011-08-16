package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd;

import com.energyict.dlms.DLMSReference;
import com.energyict.genericprotocolimpl.nta.abstractnta.NTASecurityProvider;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides property information for the InHomeDisplay
 */
public class InHomeDisplayProperties extends DlmsProtocolProperties {

    public static final String DEFAULT_IHD_CLIENT_MAC_ADDRESS = "64";

    /**
     * Uses the same logical device address as the HUB!!
     */
    public static final String DEFAULT_IHD_LOGICAL_DEVICE_ADDRESS = "1";


    @Override
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {
        //TODO implement proper functionality.
    }

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
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
        return new ArrayList<String>();
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
}