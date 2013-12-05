package com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.protocol.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.impl.OptionalPropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.ArrayList;
import java.util.Collections;
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
    public String getDisplayName() {
        return "SDK dialect for topology testing";
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        if (name.equals(slaveOneSerialNumberPropertyName)) {
            return getSlaveOneSerialNumber();
        } else if (name.equals(slaveTwoSerialNumberPropertyName)) {
            return getSlaveTwoSerialNumber();
        }
        return null;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.add(getSlaveOneSerialNumber());
        optionalProperties.add(getSlaveTwoSerialNumber());
        return optionalProperties;
    }

    private PropertySpec<String> getSlaveOneSerialNumber() {
        return OptionalPropertySpecFactory.newInstance().stringPropertySpec(slaveOneSerialNumberPropertyName);
    }

    public PropertySpec getSlaveTwoSerialNumber() {
        return OptionalPropertySpecFactory.newInstance().stringPropertySpec(slaveTwoSerialNumberPropertyName);
    }
}
