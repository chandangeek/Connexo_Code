package test.com.energyict.smartmeterprotocolimpl.sdksample;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocolimpl.base.AbstractProtocolProperties;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 7-feb-2011
 * Time: 10:46:48
 */
public class SDKSmartMeterProperties extends AbstractProtocolProperties {

    private static final String SIMULATE_REAL_COMMUNICATION = "SimulateRealCommunication";

    private static final String DEFAULT_SIMULATE_REAL_COMMUNICATION = "0";

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(UPLPropertySpecFactory.integer(SIMULATE_REAL_COMMUNICATION, false));
    }

    @ProtocolProperty
    public boolean isSimulateRealCommunication() {
        return getBooleanProperty(SIMULATE_REAL_COMMUNICATION, DEFAULT_SIMULATE_REAL_COMMUNICATION);
    }

}