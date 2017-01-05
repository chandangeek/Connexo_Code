package com.energyict.smartmeterprotocolimpl.nta.dsmr23;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY;
import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_ENCRYPTIONKEY;

/**
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 11:26:48
 */
public class Dsmr23Properties extends DlmsProtocolProperties {

    private static final String OLD_MBUS_DISCOVERY = "OldMbusDiscovery";
    private static final String FIX_MBUS_HEX_SHORT_ID = "FixMbusHexShortId";

    private static final String DEFAULT_CLIENT_MAC_ADDRESS = "1";

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.integer(SECURITY_LEVEL, securityLevelIsRequired()),
                UPLPropertySpecFactory.integer(ADDRESSING_MODE, false),
                UPLPropertySpecFactory.integer(CLIENT_MAC_ADDRESS, false),
                UPLPropertySpecFactory.string(SERVER_MAC_ADDRESS, false),
                UPLPropertySpecFactory.integer(CONNECTION, false),
                UPLPropertySpecFactory.integer(PK_FORCED_DELAY, false),
                UPLPropertySpecFactory.integer(PK_DELAY_AFTER_ERROR, false),
                UPLPropertySpecFactory.integer(INFORMATION_FIELD_SIZE, false),
                UPLPropertySpecFactory.integer(MAX_REC_PDU_SIZE, false),
                UPLPropertySpecFactory.integer(PK_RETRIES, false),
                UPLPropertySpecFactory.integer(PK_TIMEOUT, false),
                UPLPropertySpecFactory.integer(ROUND_TRIP_CORRECTION, false),
                UPLPropertySpecFactory.integer(BULK_REQUEST, false),
                UPLPropertySpecFactory.integer(CIPHERING_TYPE, false),
                UPLPropertySpecFactory.integer(NTA_SIMULATION_TOOL, false),
                UPLPropertySpecFactory.hexString(DATATRANSPORT_AUTHENTICATIONKEY, false),
                UPLPropertySpecFactory.hexString(DATATRANSPORT_ENCRYPTIONKEY, false),
                UPLPropertySpecFactory.integer(OLD_MBUS_DISCOVERY, false),
                UPLPropertySpecFactory.integer(FIX_MBUS_HEX_SHORT_ID, false),
                UPLPropertySpecFactory.integer(WAKE_UP, false));
    }

    protected boolean securityLevelIsRequired() {
        return true;
    }


    @ProtocolProperty
    public boolean getFixMbusHexShortId() {
        return getBooleanProperty(FIX_MBUS_HEX_SHORT_ID, "0");
    }

    @ProtocolProperty
    public boolean getOldMbusDiscovery() {
        return getBooleanProperty(OLD_MBUS_DISCOVERY, "0");
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        return new NTASecurityProvider(getProtocolProperties());
    }

    @ProtocolProperty
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, "1");
    }

    @ProtocolProperty
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_CLIENT_MAC_ADDRESS);
    }

}