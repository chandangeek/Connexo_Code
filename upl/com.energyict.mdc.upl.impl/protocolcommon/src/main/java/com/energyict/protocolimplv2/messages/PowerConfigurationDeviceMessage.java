package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PowerConfigurationDeviceMessage implements DeviceMessageSpec {

    IEC1107LimitPowerQuality(0, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.powerQualityThresholdAttributeName)),
    SetReferenceVoltage(1, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ReferenceVoltageAttributeName)),
    SetVoltageSagTimeThreshold(2, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.VoltageSagTimeThresholdAttributeName)),
    SetVoltageSwellTimeThreshold(3, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.VoltageSwellTimeThresholdAttributeName)),
    SetVoltageSagThreshold(4, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.VoltageSagThresholdAttributeName)),
    SetVoltageSwellThreshold(5, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.VoltageSwellThresholdAttributeName)),
    SetLongPowerFailureTimeThreshold(6, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.LongPowerFailureTimeThresholdAttributeName)),
    SetLongPowerFailureThreshold(7, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.LongPowerFailureThresholdAttributeName));

    private static final DeviceMessageCategory category = DeviceMessageCategories.POWER_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private PowerConfigurationDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
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

    @Override
    public int getMessageId() {
        return id;
    }
}