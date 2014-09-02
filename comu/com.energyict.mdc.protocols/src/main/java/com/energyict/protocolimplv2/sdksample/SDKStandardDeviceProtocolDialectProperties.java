package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;
import com.energyict.protocols.mdc.services.impl.Bus;

import java.util.ArrayList;
import java.util.List;

/**
 * A standard set of properties
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/02/13
 * Time: 15:02
 */
public class SDKStandardDeviceProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    /**
     * This value holds the name of the Property that will do something
     */
    public final String doSomeThingPropertyName = "DoSomeThing";

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.SDK_SAMPLE_STANDARD_DEVICE_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDisplayName() {
        return "SDK dialect (default)";
    }

    private PropertySpec getDoSomeThingPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(doSomeThingPropertyName, false, new BooleanFactory());
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        if(name.equals(doSomeThingPropertyName)){
            return getDoSomeThingPropertySpec();
        } else {
            return null;
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.add(getDoSomeThingPropertySpec());
        return optionalProperties;
    }
}
