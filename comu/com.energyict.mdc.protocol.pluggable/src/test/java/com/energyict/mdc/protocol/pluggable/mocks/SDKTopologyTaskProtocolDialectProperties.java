package com.energyict.mdc.protocol.pluggable.mocks;

import com.energyict.mdc.dynamic.PropertySpecService;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 9:59
 */
public class SDKTopologyTaskProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    public static final String slaveOneSerialNumberPropertyName = "SlaveOneSerialNumber";
    public static final String slaveTwoSerialNumberPropertyName = "SlaveTwoSerialNumber";

    private final PropertySpecService propertySpecService;

    public SDKTopologyTaskProtocolDialectProperties(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return "SDKTopologyDialect";
    }

    @Override
    public String getDisplayName() {
        return "SDK dialect for topology testing";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.add(getSlaveOneSerialNumber());
        optionalProperties.add(getSlaveTwoSerialNumber());
        return optionalProperties;
    }

    private PropertySpec getSlaveOneSerialNumber() {
        return this.propertySpecService.basicPropertySpec(slaveOneSerialNumberPropertyName, false, new StringFactory());
    }

    public PropertySpec getSlaveTwoSerialNumber() {
        return this.propertySpecService.basicPropertySpec(slaveTwoSerialNumberPropertyName, false, new StringFactory());
    }

}