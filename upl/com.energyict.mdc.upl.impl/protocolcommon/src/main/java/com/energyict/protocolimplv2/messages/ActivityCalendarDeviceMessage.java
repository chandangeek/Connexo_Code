package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Provides a summary of all <i>ActivityCalendar</i> related messages
 *
 * Copyrights EnergyICT
 * Date: 7/02/13
 * Time: 12:01
 */
public enum ActivityCalendarDeviceMessage implements DeviceMessageSpec {

    ACTIVITY_CALENDER_SEND(PropertySpecFactory.stringPropertySpec(activityCalendarNameAttributeName),
            PropertySpecFactory.codeTableReferencePropertySpec(activityCalendarCodeTableAttributeName)),
    ACTIVITY_CALENDER_SEND_WITH_DATETIME(PropertySpecFactory.stringPropertySpec(activityCalendarNameAttributeName),
            PropertySpecFactory.codeTableReferencePropertySpec(activityCalendarCodeTableAttributeName),
            PropertySpecFactory.dateTimePropertySpec(activityCalendarActivationDateAttributeName)),
    ACTIVITY_CALENDER_SEND_WITH_DATE(PropertySpecFactory.stringPropertySpec(activityCalendarNameAttributeName),
            PropertySpecFactory.codeTableReferencePropertySpec(activityCalendarCodeTableAttributeName),
            PropertySpecFactory.datePropertySpec(activityCalendarActivationDateAttributeName)),
    SPECIAL_DAY_CALENDAR_SEND(PropertySpecFactory.codeTableReferencePropertySpec(specialDaysCodeTableAttributeName)),
    CLEAR_AND_DISABLE_PASSIVE_TARIFF(),
    ACTIVATE_PASSIVE_CALENDAR(PropertySpecFactory.dateTimePropertySpec(activityCalendarActivationDateAttributeName));

    private static final DeviceMessageCategory activityCalendarCategory = DeviceMessageCategories.ACTIVITY_CALENDAR;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private ActivityCalendarDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return activityCalendarCategory;
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
        return ActivityCalendarDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return deviceMessagePropertySpecs;
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
