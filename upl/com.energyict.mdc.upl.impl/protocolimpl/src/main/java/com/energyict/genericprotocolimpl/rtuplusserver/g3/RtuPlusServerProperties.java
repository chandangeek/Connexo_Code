package com.energyict.genericprotocolimpl.rtuplusserver.g3;

import com.energyict.dlms.ConnectionMode;
import com.energyict.dlms.DLMSReference;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 21/02/12
 * Time: 14:47
 */
public class RtuPlusServerProperties extends DlmsProtocolProperties {

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
    public ConnectionMode getConnectionMode() {
        return ConnectionMode.TCPIP;
    }

    @Override
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, "1:16");
    }

    @Override
    public String getSecurityLevel() {
        return getStringValue(SECURITY_LEVEL, "0:0");
    }

}
