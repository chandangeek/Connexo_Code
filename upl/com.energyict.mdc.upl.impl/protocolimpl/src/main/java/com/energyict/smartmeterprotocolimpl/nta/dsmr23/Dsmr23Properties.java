package com.energyict.smartmeterprotocolimpl.nta.dsmr23;

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
 * Date: 14-jul-2011
 * Time: 11:26:48
 */
public class Dsmr23Properties extends DlmsProtocolProperties {

    public static final String OLD_MBUS_DISCOVERY = "OldMbusDiscovery";
    public static final String FIX_MBUS_HEX_SHORT_ID = "FixMbusHexShortId";

    @Override
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {

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
        optional.add(OLD_MBUS_DISCOVERY);
        optional.add(FIX_MBUS_HEX_SHORT_ID);
        optional.add(DlmsProtocolProperties.WAKE_UP);
        return optional;
    }

    public List<String> getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        required.add(DlmsProtocolProperties.SECURITY_LEVEL);
        return required;
    }

    @ProtocolProperty
    public boolean getFixMbusHexShortId() {
        return getBooleanProperty(FIX_MBUS_HEX_SHORT_ID, "0");
    }

    @ProtocolProperty
    public boolean getOldMbusDiscovery() {
        return getBooleanProperty(OLD_MBUS_DISCOVERY, "0");
    }
}
