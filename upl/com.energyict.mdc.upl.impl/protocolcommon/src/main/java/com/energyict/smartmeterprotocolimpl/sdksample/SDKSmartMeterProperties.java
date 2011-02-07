package com.energyict.smartmeterprotocolimpl.sdksample;

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.base.AbstractProtocolProperties;
import com.energyict.protocolimpl.base.ProtocolProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 7-feb-2011
 * Time: 10:46:48
 */
public class SDKSmartMeterProperties extends AbstractProtocolProperties {

    public static final String SIMULATE_REAL_COMMUNICATION = "SimulateRealCommunication";

    public static final String DEFAULT_SIMULATE_REAL_COMMUNICATION = "0";

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(SIMULATE_REAL_COMMUNICATION);
        return optional;
    }

    public List<String> getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        return required;
    }

    @ProtocolProperty
    public boolean isSimulateRealCommunication() {
        return getBooleanProperty(SIMULATE_REAL_COMMUNICATION, DEFAULT_SIMULATE_REAL_COMMUNICATION);
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {

    }
    
}
