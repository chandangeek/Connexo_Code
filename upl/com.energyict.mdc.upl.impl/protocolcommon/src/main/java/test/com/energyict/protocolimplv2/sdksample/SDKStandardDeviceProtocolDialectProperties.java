package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Collections;
import java.util.List;

/**
 * A standard set of properties
 * <p>
 * Copyrights EnergyICT
 * Date: 5/02/13
 * Time: 15:02
 */
public class SDKStandardDeviceProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    /**
     * This value holds the name of the Property that will do something
     */
    public final String doSomeThingPropertyName = "DoSomeThing";

    public SDKStandardDeviceProtocolDialectProperties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.SDK_SAMPLE_STANDARD_DEVICE_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "SDK dialect (default)";
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Collections.singletonList(this.getDoSomeThingPropertySpec());
    }

    private PropertySpec getDoSomeThingPropertySpec() {
        return propertySpecService
                .booleanSpec()
                .named(doSomeThingPropertyName, doSomeThingPropertyName)
                .describedAs("Description for " + doSomeThingPropertyName)
                .finish();
    }

}
