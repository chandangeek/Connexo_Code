package com.energyict.protocolimplv2.sdksample;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.tasks.DeviceProtocolDialectImpl;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A standard set of properties
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/02/13
 * Time: 15:02
 */
public class SDKStandardDeviceProtocolDialectProperties extends DeviceProtocolDialectImpl {

    /**
     * This value holds the name of the Property that will do something
     */
    public final String doSomeThingPropertyName = "DoSomeThing";

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.SDK_SAMPLE_STANDARD_DEVICE_PROTOCOL_DIALECT_NAME.getName();
    }

    private PropertySpec getDoSomeThingPropertySpec() {
        return PropertySpecFactory.booleanPropertySpec(doSomeThingPropertyName);
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
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.add(getDoSomeThingPropertySpec());
        return optionalProperties;
    }
}
