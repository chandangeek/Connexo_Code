package com.energyict.smartmeterprotocolimpl.webrtuz3;

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.base.AbstractProtocolProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 7-feb-2011
 * Time: 14:16:15
 */
public class WebRTUZ3Properties extends AbstractProtocolProperties {



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

}
