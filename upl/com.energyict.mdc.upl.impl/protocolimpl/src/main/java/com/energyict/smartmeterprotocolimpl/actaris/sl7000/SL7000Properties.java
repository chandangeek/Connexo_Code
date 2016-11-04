package com.energyict.smartmeterprotocolimpl.actaris.sl7000;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.ConnectionMode;
import com.energyict.dlms.DLMSReference;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * @author sva
 */
class SL7000Properties extends DlmsProtocolProperties {

    private static final String USE_REGISTER_PROFILE = "UseRegisterProfile";
    private static final String LIMIT_MAX_NR_OF_DAYS = "LimitMaxNrOfDays";
    private static final String USE_LEGACY_HDLC_CONNECTION = "UseLegacyHDLCConnection";

    private static final String DEFAULT_SECURITY_LEVEL = "1:0";
    private static final String DEFAULT_ADDRESSING_MODE = "-1";
    private static final String DEFAULT_CLIENT_MAC_ADDRESS = "1";
    private static final String DEFAULT_SERVER_MAC_ADDRESS = "1:17";
    public static final String DEFAULT_MAX_REC_PDU_SIZE = "0";
    private static final String DEFAULT_USE_REGISTER_PROFILE = "0";
    private static final String DEFAULT_LIMIT_MAX_NR_OF_DAYS = "0";
    private static final String DEFAULT_USE_LEGACY_HDLC_CONNECTION = "0";

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.integer(PK_TIMEOUT, false),
                UPLPropertySpecFactory.integer(PK_RETRIES, false),
                UPLPropertySpecFactory.integer(PK_DELAY_AFTER_ERROR, false),
                UPLPropertySpecFactory.string(SECURITY_LEVEL, false),
                UPLPropertySpecFactory.integer(CLIENT_MAC_ADDRESS, false),
                UPLPropertySpecFactory.string(SERVER_MAC_ADDRESS, false),
                UPLPropertySpecFactory.integer(USE_REGISTER_PROFILE, false),
                UPLPropertySpecFactory.integer(LIMIT_MAX_NR_OF_DAYS, false),
                UPLPropertySpecFactory.integer(USE_LEGACY_HDLC_CONNECTION, false));
    }

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public ConnectionMode getConnectionMode() {
        return ConnectionMode.HDLC;
    }

    @Override
    public String getManufacturer() {
        return "SLB::SL7000";
    }

    @Override
    public String getSecurityLevel() {
        return getStringValue(SECURITY_LEVEL, DEFAULT_SECURITY_LEVEL);
    }

    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_CLIENT_MAC_ADDRESS);
    }

    @Override
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, DEFAULT_SERVER_MAC_ADDRESS);
    }

    @Override
    public int getAddressingMode() {
        return getIntProperty(ADDRESSING_MODE, DEFAULT_ADDRESSING_MODE);
    }

    @Override
    public int getMaxRecPDUSize() {
        return getIntProperty(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE);
    }

    public boolean useRegisterProfile() {
        return getBooleanProperty(USE_REGISTER_PROFILE, DEFAULT_USE_REGISTER_PROFILE);
    }

    @ProtocolProperty
    public int getRequestTimeZone() {
        return getIntProperty("RequestTimeZone", "0");
    }

    @Override
    public byte[] getSystemIdentifier() {
        return null;
    }

    /**
     * Limit the readout of load profile data to a given nr of days. By default (= 0), all load profile data is read out.<br></br>
     * If set to a positive number, load profile data for period 'FROM [lastReading] TO [lastReading + nrOfDays]' is read out.<br></br>
     * If set to a negative number, load profile data for period 'FROM [toDate - |nrOfDays|] TO [toDate] ' is read out.<br></br>
     */
    @ProtocolProperty
    public int getLimitMaxNrOfDays() {
        return getIntProperty(LIMIT_MAX_NR_OF_DAYS, DEFAULT_LIMIT_MAX_NR_OF_DAYS);
    }

    @ProtocolProperty
    public boolean getUseLegacyHDLCConnection() {
        return getIntProperty(USE_LEGACY_HDLC_CONNECTION, DEFAULT_USE_LEGACY_HDLC_CONNECTION) == 1;
    }
}