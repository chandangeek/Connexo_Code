package com.energyict.protocolimpl.dlms.actarisace6000;

import com.energyict.dlms.DLMSReference;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by avrancea on 3/15/2017.
 */
public class ACE6000Properties extends DlmsProtocolProperties {
    @Override
    public DLMSReference getReference() {
        return DLMSReference.SN;
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {

    }

    @Override
    public List<String> getOptionalKeys() {
        List result = new ArrayList();
        result.add("Timeout");
        result.add("Retries");
        result.add("DelayAfterFail");
        result.add("RequestTimeZone");
        result.add("FirmwareVersion");
        result.add("SecurityLevel");
        result.add("ClientMacAddress");
        result.add("ServerUpperMacAddress");
        result.add("ServerLowerMacAddress");
        result.add("ExtendedLogging");
        result.add("AddressingMode");
        result.add("AlarmStatusFlagChannel");
        result.add("Password");
        return result;
    }

    @Override
    public List<String> getRequiredKeys() {
        List result = new ArrayList(0);
        return result;
    }
}
