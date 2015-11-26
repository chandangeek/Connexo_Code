package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimplv2.DeviceProtocolDialectName;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

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

    public SDKTopologyTaskProtocolDialectProperties(PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectName.SDK_SAMPLE_TOPOLOGY.getName();
    }

    @Override
    public String getDisplayName() {
        return this.getThesaurus().getFormat(DeviceProtocolDialectName.SDK_SAMPLE_TOPOLOGY).format();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.add(getSlaveOneSerialNumber());
        optionalProperties.add(getSlaveTwoSerialNumber());
        return optionalProperties;
    }

    private PropertySpec getSlaveOneSerialNumber() {
        return this.getPropertySpecService().basicPropertySpec(slaveOneSerialNumberPropertyName, false, new StringFactory());
    }

    public PropertySpec getSlaveTwoSerialNumber() {
        return this.getPropertySpecService().basicPropertySpec(slaveTwoSerialNumberPropertyName, false, new StringFactory());
    }

}