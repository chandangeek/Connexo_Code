package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;

import com.elster.jupiter.properties.PropertySpec;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum DLMSConfigurationDeviceMessage implements DeviceMessageSpec {

    SetDLMSDeviceID(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetDLMSDeviceIDAttributeName)),
    SetDLMSMeterID(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetDLMSMeterIDAttributeName)),
    SetDLMSPassword(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetDLMSPasswordAttributeName)),
    SetDLMSIdleTime(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetDLMSIdleTimeAttributeName));

    private static final DeviceMessageCategory category = DeviceMessageCategories.DLMS_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private DLMSConfigurationDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
    }

    @Override
    public String getName() {
        return this.getNameResourceKey();
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return DLMSConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.deviceMessagePropertySpecs;
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }
}