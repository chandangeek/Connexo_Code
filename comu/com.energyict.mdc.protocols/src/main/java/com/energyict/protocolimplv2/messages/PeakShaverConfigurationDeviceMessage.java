package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.util.Arrays;
import java.util.List;

/**
 * Provides a summary of all <i>Clock</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PeakShaverConfigurationDeviceMessage implements DeviceMessageSpec {

    SetActiveChannel(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetActiveChannelAttributeName)),
    SetReactiveChannel(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetReactiveChannelAttributeName)),
    SetTimeBase(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetTimeBaseAttributeName)),
    SetPOut(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPOutAttributeName)),
    SetPIn(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPInAttributeName)),
    SetDeadTime(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDeadTimeAttributeName)),
    SetAutomatic(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetAutomaticAttributeName)),
    SetCyclic(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetCyclicAttributeName)),
    SetInvert(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetInvertAttributeName)),
    SetAdaptSetpoint(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetAdaptSetpointAttributeName)),
    SetInstantAnalogOut(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetInstantAnalogOutAttributeName)),
    SetPredictedAnalogOut(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPredictedAnalogOutAttributeName)),
    SetpointAnalogOut(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetpointAnalogOutAttributeName)),
    SetDifferenceAnalogOut(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDifferenceAnalogOutAttributeName)),
    SetTariff(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetTariffAttributeName)),
    SetResetLoads(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetResetLoadsAttributeName)),
    SetSetpoint(
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.tariff),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.CurrentValueAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.NewValueAttributeName)),
    SetSwitchTime(
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.day),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.month),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.year),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.hour),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.minute),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.second)),
    SetLoad(
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.loadIdAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.MaxOffAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.DelayAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.ManualAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.StatusAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.PeakShaverIPAddressAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.PeakShaveChnNbrAttributeName));

    private static final DeviceMessageCategory clockCategory = DeviceMessageCategories.PEAK_SHAVER_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private PeakShaverConfigurationDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return clockCategory;
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
        return PeakShaverConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
