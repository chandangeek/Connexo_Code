package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimplv2.DeviceProtocolDialectName;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.ArrayList;
import java.util.List;

/**
 * A standard set of properties.
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

    public SDKStandardDeviceProtocolDialectProperties(PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectName.SDK_SAMPLE_STANDARD_DEVICE_PROTOCOL.getName();
    }

    @Override
    public String getDisplayName() {
        return this.getThesaurus().getFormat(DeviceProtocolDialectName.SDK_SAMPLE_STANDARD_DEVICE_PROTOCOL).format();
    }

    private PropertySpec getDoSomeThingPropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(doSomeThingPropertyName, false, new BooleanFactory());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.add(getDoSomeThingPropertySpec());
        return optionalProperties;
    }

}