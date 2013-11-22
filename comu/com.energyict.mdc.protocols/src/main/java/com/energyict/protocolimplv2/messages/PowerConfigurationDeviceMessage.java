package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.messages.*;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PowerConfigurationDeviceMessage implements DeviceMessageSpec {

    IEC1107LimitPowerQuality(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.powerQualityThresholdAttributeName)),
    SetReferenceVoltage(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ReferenceVoltageAttributeName)),
    SetVoltageSagTimeThreshold(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.VoltageSagTimeThresholdAttributeName)),
    SetVoltageSwellTimeThreshold(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.VoltageSwellTimeThresholdAttributeName)),
    SetVoltageSagThreshold(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.VoltageSagThresholdAttributeName)),
    SetVoltageSwellThreshold(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.VoltageSwellThresholdAttributeName)),
    SetLongPowerFailureTimeThreshold(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.LongPowerFailureTimeThresholdAttributeName)),
    SetLongPowerFailureThreshold(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.LongPowerFailureThresholdAttributeName));

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
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
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