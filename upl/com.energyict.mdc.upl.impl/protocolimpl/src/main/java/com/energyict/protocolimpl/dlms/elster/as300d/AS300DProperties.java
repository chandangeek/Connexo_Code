package com.energyict.protocolimpl.dlms.elster.as300d;

import com.energyict.dlms.DLMSReference;
import com.energyict.genericprotocolimpl.nta.abstractnta.NTASecurityProvider;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.dlms.DlmsProtocolProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 21/02/12
 * Time: 14:47
 */
public class AS300DProperties extends DlmsProtocolProperties {

    public AS300DProperties() {
        super(new Properties());
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {

    }

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
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

}
