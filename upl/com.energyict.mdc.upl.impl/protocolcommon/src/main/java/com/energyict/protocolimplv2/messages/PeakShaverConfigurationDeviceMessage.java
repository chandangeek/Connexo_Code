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
 * Provides a summary of all <i>Clock</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PeakShaverConfigurationDeviceMessage implements DeviceMessageSpec {

    SetActiveChannel(0, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetActiveChannelAttributeName)),
    SetReactiveChannel(1, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetReactiveChannelAttributeName)),
    SetTimeBase(2, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetTimeBaseAttributeName)),
    SetPOut(3, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPOutAttributeName)),
    SetPIn(4, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPInAttributeName)),
    SetDeadTime(5, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDeadTimeAttributeName)),
    SetAutomatic(6, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetAutomaticAttributeName)),
    SetCyclic(7, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetCyclicAttributeName)),
    SetInvert(8, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetInvertAttributeName)),
    SetAdaptSetpoint(9, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetAdaptSetpointAttributeName)),
    SetInstantAnalogOut(10, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetInstantAnalogOutAttributeName)),
    SetPredictedAnalogOut(11, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPredictedAnalogOutAttributeName)),
    SetpointAnalogOut(12, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetpointAnalogOutAttributeName)),
    SetDifferenceAnalogOut(13, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDifferenceAnalogOutAttributeName)),
    SetTariff(14, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetTariffAttributeName)),
    SetResetLoads(15, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetResetLoadsAttributeName)),
    SetSetpoint(16,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.tariff),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.CurrentValueAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.NewValueAttributeName)),
    SetSwitchTime(17,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.id),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.day),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.month),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.year),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.hour),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.minute),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.second)),
    SetLoad(18,
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
    private final int id;

    private PeakShaverConfigurationDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
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
        return new EnumBasedDeviceMessageSpecPrimaryKey(this, name());
    }

    @Override
    public int getMessageId() {
        return id;
    }
}
