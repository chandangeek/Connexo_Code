package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.cpo.MdwToUplPropertySpecAdapter;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 9:59
 */
public class SDKBreakerTaskProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    public static final String breakerStatus = "breakerStatus";

    private static final String CONNECTED = "connected";
    private static final String DISCONNECTED = "disconnected";
    private static final String ARMED = "armed";

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.SDK_SAMPLE_TOPOLOGY_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "SDK dialect for breaker testing";
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                MdwToUplPropertySpecAdapter.adapt(PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(breakerStatus, CONNECTED, CONNECTED, DISCONNECTED, ARMED))
        );
    }
}