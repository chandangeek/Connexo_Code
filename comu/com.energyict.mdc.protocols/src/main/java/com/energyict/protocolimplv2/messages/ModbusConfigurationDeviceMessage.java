package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.protocol.api.dynamic.PropertySpec;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.protocol.dynamic.RequiredPropertySpecFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ModbusConfigurationDeviceMessage implements DeviceMessageSpec {

    SetMmEvery(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetMmEveryAttributeName)),
    SetMmTimeout(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetMmTimeoutAttributeName)),
    SetMmInstant(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetMmInstantAttributeName)),
    SetMmOverflow(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetMmOverflowAttributeName)),
    SetMmConfig(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetMmConfigAttributeName)),
    WriteSingleRegisters(RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValues(DeviceMessageConstants.RadixFormatAttributeName, "DEC", "HEX"),
            RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.RegisterAddressAttributeName),
            RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.RegisterValueAttributeName)),
    WriteMultipleRegisters(RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValues(DeviceMessageConstants.RadixFormatAttributeName, "DEC", "HEX"),
            RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.RegisterAddressAttributeName),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.RegisterValueAttributeName));

    private static final DeviceMessageCategory category = DeviceMessageCategories.MODBUS_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private ModbusConfigurationDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
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
        return ModbusConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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