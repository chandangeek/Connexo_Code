package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ModemConfigurationDeviceMessage implements DeviceMessageSpec {

    SetDialCommand(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetDialCommandAttributeName)),
    SetModemInit1(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetModemInit1AttributeName)),
    SetModemInit2(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetModemInit2AttributeName)),
    SetModemInit3(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetModemInit3AttributeName)),
    SetPPPBaudRate(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetPPPBaudRateAttributeName)),
    SetModemtype(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetModemtypeAttributeName)),
    SetResetCycle(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetResetCycleAttributeName));

    private static final DeviceMessageCategory category = DeviceMessageCategories.MODEM_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private ModemConfigurationDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
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
        return ModemConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
    public
    DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }
}