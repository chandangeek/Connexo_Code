package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.protocol.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.impl.RequiredPropertySpecFactory;
import com.energyict.protocolimplv2.messages.enums.ActivityCalendarType;

import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarTypeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contractsXmlUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysCodeTableAttributeName;

/**
 * Provides a summary of all <i>ActivityCalendar</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 7/02/13
 * Time: 12:01
 */
public enum ActivityCalendarDeviceMessage implements DeviceMessageSpec {

    ACTIVITY_CALENDAR_READ,
    WRITE_CONTRACTS_FROM_XML_USERFILE(RequiredPropertySpecFactory.newInstance().userFileReferencePropertySpec(contractsXmlUserFileAttributeName)),
    ACTIVITY_CALENDER_SEND(RequiredPropertySpecFactory.newInstance().stringPropertySpec(activityCalendarNameAttributeName),
            RequiredPropertySpecFactory.newInstance().codeTableReferencePropertySpec(activityCalendarCodeTableAttributeName)),
    ACTIVITY_CALENDER_SEND_WITH_DATETIME(RequiredPropertySpecFactory.newInstance().stringPropertySpec(activityCalendarNameAttributeName),
            RequiredPropertySpecFactory.newInstance().codeTableReferencePropertySpec(activityCalendarCodeTableAttributeName),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(activityCalendarActivationDateAttributeName)),
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE(
            RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValues(activityCalendarTypeAttributeName, ActivityCalendarType.getAllDescriptions()),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(activityCalendarNameAttributeName),
            RequiredPropertySpecFactory.newInstance().codeTableReferencePropertySpec(activityCalendarCodeTableAttributeName),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(activityCalendarActivationDateAttributeName)),
    ACTIVITY_CALENDER_SEND_WITH_DATE(RequiredPropertySpecFactory.newInstance().stringPropertySpec(activityCalendarNameAttributeName),
            RequiredPropertySpecFactory.newInstance().codeTableReferencePropertySpec(activityCalendarCodeTableAttributeName),
            RequiredPropertySpecFactory.newInstance().datePropertySpec(activityCalendarActivationDateAttributeName)),
    SPECIAL_DAY_CALENDAR_SEND(RequiredPropertySpecFactory.newInstance().codeTableReferencePropertySpec(specialDaysCodeTableAttributeName)),
    SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE(
            RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValues(activityCalendarTypeAttributeName, ActivityCalendarType.getAllDescriptions()),
            RequiredPropertySpecFactory.newInstance().codeTableReferencePropertySpec(specialDaysCodeTableAttributeName)),
    CLEAR_AND_DISABLE_PASSIVE_TARIFF(),
    ACTIVATE_PASSIVE_CALENDAR(RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(activityCalendarActivationDateAttributeName));

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
