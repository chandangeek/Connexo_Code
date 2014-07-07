package com.energyict.protocolimplv2.messages;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;

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

    SetActiveChannel(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id), RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetActiveChannelAttributeName)),
    SetReactiveChannel(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id), RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetReactiveChannelAttributeName)),
    SetTimeBase(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id), RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetTimeBaseAttributeName)),
    SetPOut(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id), RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetPOutAttributeName)),
    SetPIn(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id), RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetPInAttributeName)),
    SetDeadTime(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id), RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetDeadTimeAttributeName)),
    SetAutomatic(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id), RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetAutomaticAttributeName)),
    SetCyclic(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id), RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetCyclicAttributeName)),
    SetInvert(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id), RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetInvertAttributeName)),
    SetAdaptSetpoint(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id), RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetAdaptSetpointAttributeName)),
    SetInstantAnalogOut(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id), RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetInstantAnalogOutAttributeName)),
    SetPredictedAnalogOut(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id), RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetPredictedAnalogOutAttributeName)),
    SetpointAnalogOut(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id), RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetpointAnalogOutAttributeName)),
    SetDifferenceAnalogOut(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id), RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetDifferenceAnalogOutAttributeName)),
    SetTariff(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id), RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetTariffAttributeName)),
    SetResetLoads(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id), RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetResetLoadsAttributeName)),
    SetSetpoint(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.tariff),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.CurrentValueAttributeName),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.NewValueAttributeName)),
    SetSwitchTime(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.day),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.month),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.year),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.hour),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.minute),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.second)),
    SetLoad(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.id),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.loadIdAttributeName),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.MaxOffAttributeName),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.DelayAttributeName),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.ManualAttributeName),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.StatusAttributeName),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.PeakShaverIPAddressAttributeName),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.PeakShaveChnNbrAttributeName));

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
