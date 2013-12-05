package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.protocol.dynamic.PropertySpec;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.protocol.dynamic.RequiredPropertySpecFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum MBusConfigurationDeviceMessage implements DeviceMessageSpec {

    SetMBusEvery(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetMBusEveryAttributeName)),
    SetMBusInterFrameTime(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetMBusInterFrameTimeAttributeName)),
    SetMBusConfig(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetMBusConfigAttributeName)),
    SetMBusVIF(RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.SetMBusVIFAttributeName));

    private static final DeviceMessageCategory category = DeviceMessageCategories.MBUS_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private MBusConfigurationDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
    }

    @Override
    public String getName() {
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return MBusConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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