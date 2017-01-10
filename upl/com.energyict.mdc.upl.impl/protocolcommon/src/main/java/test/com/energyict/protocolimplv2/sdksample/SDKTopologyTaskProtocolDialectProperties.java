package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.cpo.MdwToUplPropertySpecAdapter;
import com.energyict.cpo.PropertySpec;
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
public class SDKTopologyTaskProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    public static final String slaveOneSerialNumberPropertyName = "SlaveOneSerialNumber";
    public static final String slaveTwoSerialNumberPropertyName = "SlaveTwoSerialNumber";

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.SDK_SAMPLE_TOPOLOGY_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "SDK dialect for topology testing";
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                MdwToUplPropertySpecAdapter.adapt(getSlaveOneSerialNumber()),
                MdwToUplPropertySpecAdapter.adapt(getSlaveTwoSerialNumber()));
    }

    private PropertySpec getSlaveOneSerialNumber() {
        return PropertySpecFactory.stringPropertySpec(slaveOneSerialNumberPropertyName);
    }

    public PropertySpec getSlaveTwoSerialNumber() {
        return PropertySpecFactory.stringPropertySpec(slaveTwoSerialNumberPropertyName);
    }
}
