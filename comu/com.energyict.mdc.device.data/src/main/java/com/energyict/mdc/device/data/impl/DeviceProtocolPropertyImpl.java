package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.exception.MessageSeeds;
import com.energyict.mdc.dynamic.PropertySpec;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Represents a <i>typed</i> property of a Device
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/14/14
 * Time: 9:03 AM
 */
public class DeviceProtocolPropertyImpl<T> implements DeviceProtocolProperty {

    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(min = 1, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    private String name;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.VALUE_IS_REQUIRED_KEY + "}")
    @Size(min = 1, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.VALUE_IS_REQUIRED_KEY + "}")
    private String stringValue;
    private Device device;

    @Inject
    public DeviceProtocolPropertyImpl() {
    }

    DeviceProtocolPropertyImpl<T> initialize(Device device, String name, String stringValue) {
        this.device = device;
        this.name = name;
        this.stringValue = stringValue;
        return this;
    }

    DeviceProtocolPropertyImpl<T> initialize(Device device, String name, T value) {
        this.device = device;
        this.name = name;
        this.stringValue = convertToStringValue(value);
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getStringValue() {
        return stringValue;
    }

    @Override
    public T getValue() {
        for (PropertySpec<T> propertySpec : device.getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs()) {
            if (propertySpec.getName().equals(this.name)) {
                return propertySpec.getValueFactory().fromStringValue(this.stringValue);
            }
        }
        return null;
    }

    private String convertToStringValue(T value) {
        if (this.device != null) {
            for (PropertySpec<T> propertySpec : device.getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs()) {
                if (propertySpec.getName().equals(this.name)) {
                    return propertySpec.getValueFactory().toStringValue(value);
                }
            }
        }
        return null;
    }

}
