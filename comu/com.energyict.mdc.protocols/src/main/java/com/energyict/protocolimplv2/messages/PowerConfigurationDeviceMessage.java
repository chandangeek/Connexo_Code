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
public enum PowerConfigurationDeviceMessage implements DeviceMessageSpec {

    IEC1107LimitPowerQuality(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.powerQualityThresholdAttributeName)),
    SetReferenceVoltage(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.ReferenceVoltageAttributeName)),
    SetVoltageSagTimeThreshold(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.VoltageSagTimeThresholdAttributeName)),
    SetVoltageSwellTimeThreshold(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.VoltageSwellTimeThresholdAttributeName)),
    SetVoltageSagThreshold(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.VoltageSagThresholdAttributeName)),
    SetVoltageSwellThreshold(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.VoltageSwellThresholdAttributeName)),
    SetLongPowerFailureTimeThreshold(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.LongPowerFailureTimeThresholdAttributeName)),
    SetLongPowerFailureThreshold(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.LongPowerFailureThresholdAttributeName));

    private static final DeviceMessageCategory category = DeviceMessageCategories.POWER_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private PowerConfigurationDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
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
        return PowerConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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