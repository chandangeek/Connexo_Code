package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.StringFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;

import com.energyict.protocolimplv2.messages.enums.ActivityCalendarType;

import java.util.ArrayList;
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
    WRITE_CONTRACTS_FROM_XML_USERFILE {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(contractsXmlUserFileAttributeName, true, FactoryIds.USERFILE));
        }
    },
    ACTIVITY_CALENDER_SEND {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarNameAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.referencePropertySpec(activityCalendarCodeTableAttributeName, true, FactoryIds.CODE));
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarNameAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.referencePropertySpec(activityCalendarCodeTableAttributeName, true, FactoryIds.CODE));
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarActivationDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(activityCalendarTypeAttributeName, true, ActivityCalendarType.getAllDescriptions()));
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarNameAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.referencePropertySpec(activityCalendarCodeTableAttributeName, true, FactoryIds.CODE));
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarActivationDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATE {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarNameAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.referencePropertySpec(activityCalendarCodeTableAttributeName, true, FactoryIds.CODE));
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarActivationDateAttributeName, true, new DateFactory()));
        }
    },
    SPECIAL_DAY_CALENDAR_SEND {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(specialDaysCodeTableAttributeName, true, FactoryIds.CODE));
        }
    },
    SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(activityCalendarTypeAttributeName, true, ActivityCalendarType.getAllDescriptions()));
            propertySpecs.add(propertySpecService.referencePropertySpec(specialDaysCodeTableAttributeName, true, FactoryIds.CODE));
        }
    },
    CLEAR_AND_DISABLE_PASSIVE_TARIFF,
    ACTIVATE_PASSIVE_CALENDAR {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarActivationDateAttributeName, true, new DateAndTimeFactory()));
        }
    };

    private static final DeviceMessageCategory activityCalendarCategory = DeviceMessageCategories.ACTIVITY_CALENDAR;

    @Override
    public DeviceMessageCategory getCategory() {
        return activityCalendarCategory;
    }

    @Override
    public String getName() {
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, PropertySpecService.INSTANCE.get());
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

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
