package com.energyict.protocolimpl.dlms.elster.as300d;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.genericprotocolimpl.nta.abstractnta.NTASecurityProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 21/02/12
 * Time: 14:47
 */
public class AS300DProperties extends DlmsProtocolProperties {

    /** Name of the property containing the load profile OBIS code to fetch. */
    private static final String PROPNAME_LOAD_PROFILE_OBIS_CODE = "LoadProfileObisCode";

    public AS300DProperties() {
        this(new Properties());
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {

    }

    public AS300DProperties(Properties properties) {
        super(properties);
    }

    @Override
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(PROPNAME_LOAD_PROFILE_OBIS_CODE);
        optional.add(CLIENT_MAC_ADDRESS);
        optional.add(SERVER_MAC_ADDRESS);
        optional.add(SECURITY_LEVEL);
        optional.add(CONNECTION);
        optional.add(NTASecurityProvider.DATATRANSPORT_ENCRYPTIONKEY);
        optional.add(NTASecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY);
        return optional;
    }

    public List<String> getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        // TODO: Add required keys
        return required;
    }

    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, "1");
    }

    @Override
    public String getServerMacAddress() {
        final String oldMacAddress = getStringValue(SERVER_MAC_ADDRESS, "1:16");
        return oldMacAddress.replaceAll("x", getNodeAddress());
    }

    @Override
    public String getSecurityLevel() {
        return getStringValue(SECURITY_LEVEL, "1:0");
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        return new NTASecurityProvider(getProtocolProperties());
    }

    @ProtocolProperty
    public ObisCode getLoadProfileObiscode() {
        final String obisString = getStringValue(PROPNAME_LOAD_PROFILE_OBIS_CODE, "");
        try {
            return ObisCode.fromString(obisString);
        } catch (IllegalArgumentException e) {
            return AS300DProfile.HOURLY_PROFILE;
        }
    }

}
