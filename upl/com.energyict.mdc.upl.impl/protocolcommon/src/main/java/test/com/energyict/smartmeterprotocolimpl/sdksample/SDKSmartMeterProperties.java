package test.com.energyict.smartmeterprotocolimpl.sdksample;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.base.AbstractProtocolProperties;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.properties.nls.PropertyTranslationKeys;
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

    private final PropertySpecService propertySpecService;

    public SDKSmartMeterProperties(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Collections.singletonList(
                UPLPropertySpecFactory
                        .specBuilder(SIMULATE_REAL_COMMUNICATION, false, PropertyTranslationKeys.SDKSAMPLE_SIMILATE_REAL_COMMUNICATION, this.propertySpecService::integerSpec)
                        .finish());
    }

    @ProtocolProperty
    public boolean isSimulateRealCommunication() {
        return getBooleanProperty(SIMULATE_REAL_COMMUNICATION, DEFAULT_SIMULATE_REAL_COMMUNICATION);
    }

}