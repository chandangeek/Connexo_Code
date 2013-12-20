package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.RequiredPropertySpecFactory;
import com.energyict.protocolimplv2.messages.enums.DSTAlgorithm;

import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.TimeZoneOffsetInHoursAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.meterTimeAttributeName;

/**
 * Provides a summary of all <i>Clock</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ClockDeviceMessage implements DeviceMessageSpec {

    SET_TIME(RequiredPropertySpecFactory.newInstance().datePropertySpec(meterTimeAttributeName)),
    SET_TIMEZONE(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(TimeZoneOffsetInHoursAttributeName)),     //In hours

    EnableOrDisableDST(RequiredPropertySpecFactory.newInstance().notNullableBooleanPropertySpec(DeviceMessageConstants.enableDSTAttributeName)),
    SetEndOfDST(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.month),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.dayOfMonth),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.dayOfWeek),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.hour)),
    SetStartOfDST(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.month),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.dayOfMonth),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.dayOfWeek),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.hour)),
    SetStartOfDSTWithoutHour(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.month),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.dayOfMonth),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.dayOfWeek)),
    SetEndOfDSTWithoutHour(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.month),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.dayOfMonth),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.dayOfWeek)),
    SetDSTAlgorithm(
            RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValues(DeviceMessageConstants.dstStartAlgorithmAttributeName, DSTAlgorithm.getAllDescriptions()),
            RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValues(DeviceMessageConstants.dstEndAlgorithmAttributeName, DSTAlgorithm.getAllDescriptions())
    ),

    //EIWeb messages
    SetDST(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetDSTAttributeName)),
    SetTimezone(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetTimezoneAttributeName)),
    SetTimeAdjustment(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetTimeAdjustmentAttributeName)),
    SetNTPServer(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetNTPServerAttributeName)),
    SetRefreshClockEvery(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetRefreshClockEveryAttributeName)),
    SetNTPOptions(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetNTPOptionsAttributeName));

    private static final DeviceMessageCategory clockCategory = DeviceMessageCategories.CLOCK;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private ClockDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
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
        return ClockDeviceMessage.class.getSimpleName() + "." + this.toString();
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
