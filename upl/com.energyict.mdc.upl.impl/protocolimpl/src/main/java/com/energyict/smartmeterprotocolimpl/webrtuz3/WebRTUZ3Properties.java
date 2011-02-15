package com.energyict.smartmeterprotocolimpl.webrtuz3;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 7-feb-2011
 * Time: 14:16:15
 */
public class WebRTUZ3Properties extends DlmsProtocolProperties {

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        return optional;
    }

    public List<String> getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        return required;
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {

    }

    public static void main(String[] args) {
        System.out.println(new WebRTUZ3Properties().toString());
    }

    @Override
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }
}
