package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.protocolimplv2.messages.enums.ActivityCalendarType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Provides a summary of all <i>ActivityCalendar</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 7/02/13
 * Time: 12:01
 */
public enum ActivityCalendarDeviceMessage implements DeviceMessageSpec {

    ACTIVITY_CALENDAR_READ(0),
    WRITE_CONTRACTS_FROM_XML_USERFILE(1, PropertySpecFactory.userFileReferencePropertySpec(contractsXmlUserFileAttributeName)),
    ACTIVITY_CALENDER_SEND(2, PropertySpecFactory.stringPropertySpec(activityCalendarNameAttributeName),
            PropertySpecFactory.codeTableReferencePropertySpec(activityCalendarCodeTableAttributeName)),
    ACTIVITY_CALENDER_SEND_WITH_DATETIME(3, PropertySpecFactory.stringPropertySpec(activityCalendarNameAttributeName),
            PropertySpecFactory.codeTableReferencePropertySpec(activityCalendarCodeTableAttributeName),
            PropertySpecFactory.dateTimePropertySpec(activityCalendarActivationDateAttributeName)),
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE(4,
            PropertySpecFactory.stringPropertySpecWithValues(activityCalendarTypeAttributeName, ActivityCalendarType.getAllDescriptions()),
            PropertySpecFactory.stringPropertySpec(activityCalendarNameAttributeName),
            PropertySpecFactory.codeTableReferencePropertySpec(activityCalendarCodeTableAttributeName),
            PropertySpecFactory.dateTimePropertySpec(activityCalendarActivationDateAttributeName)),
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT(5,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(contractAttributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.stringPropertySpec(activityCalendarNameAttributeName),
            PropertySpecFactory.codeTableReferencePropertySpec(activityCalendarCodeTableAttributeName),
            PropertySpecFactory.dateTimePropertySpec(activityCalendarActivationDateAttributeName)),
    ACTIVITY_CALENDER_SEND_WITH_DATE(6, PropertySpecFactory.stringPropertySpec(activityCalendarNameAttributeName),
            PropertySpecFactory.codeTableReferencePropertySpec(activityCalendarCodeTableAttributeName),
            PropertySpecFactory.datePropertySpec(activityCalendarActivationDateAttributeName)),
    SPECIAL_DAY_CALENDAR_SEND(7, PropertySpecFactory.codeTableReferencePropertySpec(specialDaysCodeTableAttributeName)),
    SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE(8,
            PropertySpecFactory.stringPropertySpecWithValues(activityCalendarTypeAttributeName, ActivityCalendarType.getAllDescriptions()),
            PropertySpecFactory.codeTableReferencePropertySpec(specialDaysCodeTableAttributeName)),
    SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME(9,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(contractAttributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.codeTableReferencePropertySpec(specialDaysCodeTableAttributeName),
            PropertySpecFactory.dateTimePropertySpec(activityCalendarActivationDateAttributeName)),
    CLEAR_AND_DISABLE_PASSIVE_TARIFF(10),
    ACTIVATE_PASSIVE_CALENDAR(11, PropertySpecFactory.dateTimePropertySpec(activityCalendarActivationDateAttributeName)),
    SPECIAL_DAY_CALENDAR_SEND_FROM_XML_USER_FILE(12, PropertySpecFactory.userFileReferencePropertySpec(XmlUserFileAttributeName)),
    ACTIVITY_CALENDAR_SEND_WITH_DATETIME_FROM_XML_USER_FILE(13,
            PropertySpecFactory.userFileReferencePropertySpec(XmlUserFileAttributeName),
            PropertySpecFactory.dateTimePropertySpec(activityCalendarActivationDateAttributeName)),
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_DEFAULT_TARIFF_CODE(14,
            PropertySpecFactory.stringPropertySpec(activityCalendarNameAttributeName),
            PropertySpecFactory.codeTableReferencePropertySpec(activityCalendarCodeTableAttributeName),
            PropertySpecFactory.dateTimePropertySpec(activityCalendarActivationDateAttributeName),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(defaultTariffCodeAttrributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3))),
    ;

    private static final DeviceMessageCategory activityCalendarCategory = DeviceMessageCategories.ACTIVITY_CALENDAR;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private ActivityCalendarDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
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

    @Override
    public int getMessageId() {
        return id;
    }
}
