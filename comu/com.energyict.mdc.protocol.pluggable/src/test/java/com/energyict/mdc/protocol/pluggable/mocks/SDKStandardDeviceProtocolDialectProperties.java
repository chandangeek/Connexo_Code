package com.energyict.mdc.protocol.pluggable.mocks;

import com.energyict.mdc.dynamic.PropertySpecService;

import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;

import java.util.ArrayList;
import java.util.List;

/**
 * A standard set of properties
 * <p>
 * Copyrights EnergyICT
 * Date: 5/02/13
 * Time: 15:02
 */
public class SDKStandardDeviceProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    private final PropertySpecService propertySpecService;

    /**
     * This value holds the name of the Property that will do something
     */
    public final String doSomeThingPropertyName = "DoSomeThing";

    public SDKStandardDeviceProtocolDialectProperties(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return "SDKStandardDialect";
    }

    @Override
    public String getDisplayName() {
        return "SDK dialect (default)";
    }

    private PropertySpec getDoSomeThingPropertySpec() {
        return this.propertySpecService.basicPropertySpec(doSomeThingPropertyName, false, new BooleanFactory());
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        if (name.equals(doSomeThingPropertyName)) {
            return getDoSomeThingPropertySpec();
        }
        else {
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