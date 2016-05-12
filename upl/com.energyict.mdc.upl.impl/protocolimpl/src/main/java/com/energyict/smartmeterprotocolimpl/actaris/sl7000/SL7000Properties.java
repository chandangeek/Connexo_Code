package com.energyict.smartmeterprotocolimpl.actaris.sl7000;

import com.energyict.dlms.ConnectionMode;
import com.energyict.dlms.DLMSReference;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * @author sva
 */
public class SL7000Properties extends DlmsProtocolProperties {

    public static final String USE_REGISTER_PROFILE = "UseRegisterProfile";
    public static final String LIMIT_MAX_NR_OF_DAYS = "LimitMaxNrOfDays";

    public static final String DEFAULT_SECURITY_LEVEL = "1:0";
    public static final String DEFAULT_ADDRESSING_MODE = "-1";
    public static final String DEFAULT_CLIENT_MAC_ADDRESS = "1";
    public static final String DEFAULT_SERVER_MAC_ADDRESS = "1:17";
    public static final String DEFAULT_MAX_REC_PDU_SIZE = "0";
    public static final String DEFAULT_USE_REGISTER_PROFILE = "0";
    public static final String DEFAULT_LIMIT_MAX_NR_OF_DAYS = "0";

    public List<String> getOptionalKeys() {
        List result = new ArrayList();
        result.add(TIMEOUT);
        result.add(RETRIES);
        result.add(DELAY_AFTER_ERROR);
        result.add(SECURITY_LEVEL);
        result.add(CLIENT_MAC_ADDRESS);
        result.add(SERVER_MAC_ADDRESS);
        result.add(USE_REGISTER_PROFILE);
        result.add(LIMIT_MAX_NR_OF_DAYS);
        return result;
    }

    public List<String> getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        return required;
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {
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
}