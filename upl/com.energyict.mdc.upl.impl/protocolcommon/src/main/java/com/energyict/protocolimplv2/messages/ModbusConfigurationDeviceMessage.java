package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ModbusConfigurationDeviceMessage implements DeviceMessageSpec {

    SetMmEvery(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetMmEveryAttributeName)),
    SetMmTimeout(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetMmTimeoutAttributeName)),
    SetMmInstant(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetMmInstantAttributeName)),
    SetMmOverflow(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetMmOverflowAttributeName)),
    SetMmConfig(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetMmConfigAttributeName)),
    WriteSingleRegisters(PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.RadixFormatAttributeName, "DEC", "HEX"),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.RegisterAddressAttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.RegisterValueAttributeName)),
    WriteMultipleRegisters(PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.RadixFormatAttributeName, "DEC", "HEX"),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.RegisterAddressAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.RegisterValueAttributeName));

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