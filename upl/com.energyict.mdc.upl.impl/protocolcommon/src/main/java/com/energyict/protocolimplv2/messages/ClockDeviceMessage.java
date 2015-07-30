package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;
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

    SET_TIME(0, PropertySpecFactory.dateTimePropertySpec(meterTimeAttributeName)),
    SET_TIMEZONE(1, PropertySpecFactory.bigDecimalPropertySpec(TimeZoneOffsetInHoursAttributeName)),     //In hours

    EnableOrDisableDST(2, PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.enableDSTAttributeName)),
    SetEndOfDST(3,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.month),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.dayOfMonth),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.dayOfWeek),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.hour)),
    SetStartOfDST(4,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.month),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.dayOfMonth),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.dayOfWeek),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.hour)),
    SetStartOfDSTWithoutHour(5,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.month),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.dayOfMonth),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.dayOfWeek)),
    SetEndOfDSTWithoutHour(6,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.month),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.dayOfMonth),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.dayOfWeek)),
    SetDSTAlgorithm(7,
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.dstStartAlgorithmAttributeName, DSTAlgorithm.getAllDescriptions()),
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.dstEndAlgorithmAttributeName, DSTAlgorithm.getAllDescriptions())
    ),

    //EIWeb messages
    SetDST(8, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDSTAttributeName)),
    SetTimezone(9, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetTimezoneAttributeName)),
    SetTimeAdjustment(10, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetTimeAdjustmentAttributeName)),
    SetNTPServer(11, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetNTPServerAttributeName)),
    SetRefreshClockEvery(12, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetRefreshClockEveryAttributeName)),
    SetNTPOptions(13, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetNTPOptionsAttributeName)),
    FTIONForceTimeSync(14),

    SyncTime(15),

    ConfigureDST(17,
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.enableDSTAttributeName),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.StartOfDSTAttributeName),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.EndOfDSTAttributeName)
    ),
    ConfigureDSTWithoutHour(18,
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.enableDSTAttributeName),
            PropertySpecFactory.datePropertySpec(DeviceMessageConstants.StartOfDSTAttributeName),
            PropertySpecFactory.datePropertySpec(DeviceMessageConstants.EndOfDSTAttributeName)
    ),
    NTPSetOption(19, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.singleOptionAttributeName)),
    NTPClrOption(20, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.singleOptionAttributeName));

    private static final DeviceMessageCategory clockCategory = DeviceMessageCategories.CLOCK;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    public int getMessageId() {
        return id;
    }

    private ClockDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
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
