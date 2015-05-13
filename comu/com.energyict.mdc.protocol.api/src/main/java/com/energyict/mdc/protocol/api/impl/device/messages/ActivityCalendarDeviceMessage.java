package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.*;

/**
 * Provides a summary of all <i>ActivityCalendar</i> related messages.
 * <p/>
 * Copyrights EnergyICT
 * Date: 7/02/13
 * Time: 12:01
 */
public enum ActivityCalendarDeviceMessage implements DeviceMessageSpecEnum {

    ACTIVITY_CALENDAR_READ(DeviceMessageId.ACTIVITY_CALENDAR_READ, "Read activity calendar"),
    WRITE_CONTRACTS_FROM_XML_USERFILE(DeviceMessageId.ACTIVITY_CALENDAR_WRITE_CONTRACTS_FROM_XML_USERFILE, "Write contract from XML user file") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(contractsXmlUserFileAttributeName, true, FactoryIds.USERFILE));
        }
    },
    ACTIVITY_CALENDER_SEND(DeviceMessageId.ACTIVITY_CALENDER_SEND, "Send activity calendar") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarNameAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.referencePropertySpec(activityCalendarCodeTableAttributeName, true, FactoryIds.CODE));
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME, "Send activity calendar with activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarNameAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.referencePropertySpec(activityCalendarCodeTableAttributeName, true, FactoryIds.CODE));
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarActivationDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE, "Send activity calendar with activation date and type") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(activityCalendarTypeAttributeName, true, ActivityCalendarType.getAllDescriptions()));
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarNameAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.referencePropertySpec(activityCalendarCodeTableAttributeName, true, FactoryIds.CODE));
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarActivationDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATE(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATE, "Send activity calendar with activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarNameAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.referencePropertySpec(activityCalendarCodeTableAttributeName, true, FactoryIds.CODE));
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarActivationDateAttributeName, true, new DateFactory()));
        }
    },
    SPECIAL_DAY_CALENDAR_SEND(DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND, "Send special days calendar") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(specialDaysCodeTableAttributeName, true, FactoryIds.CODE));
        }
    },
    SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE(DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE, "Send special days calendar with type") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(activityCalendarTypeAttributeName, true, ActivityCalendarType.getAllDescriptions()));
            propertySpecs.add(propertySpecService.referencePropertySpec(specialDaysCodeTableAttributeName, true, FactoryIds.CODE));
        }
    },
    CLEAR_AND_DISABLE_PASSIVE_TARIFF(DeviceMessageId.ACTIVITY_CALENDAR_CLEAR_AND_DISABLE_PASSIVE_TARIFF, "Clear and disable passive tariff"),
    ACTIVATE_PASSIVE_CALENDAR(DeviceMessageId.ACTIVATE_CALENDAR_PASSIVE, "Activate passive calendar") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarActivationDateAttributeName, true, new DateAndTimeFactory()));
        }
    }, ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT, "Send activity calendar with date and contract"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(contractAttributeName, true, BigDecimal.ONE, BigDecimals.TWO));
            propertySpecs.add(propertySpecService.stringPropertySpec(activityCalendarNameAttributeName, true, ""));
            propertySpecs.add(propertySpecService.referencePropertySpec(activityCalendarCodeTableAttributeName, true, FactoryIds.CODE));
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarActivationDateAttributeName, true, new DateFactory()));
        }
    },
    SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME(DeviceMessageId.ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME, "Send special day calendar with date and contract"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(contractAttributeName, true, BigDecimal.ONE, BigDecimals.TWO));
            propertySpecs.add(propertySpecService.referencePropertySpec(specialDaysCodeTableAttributeName, true, FactoryIds.CODE));
            propertySpecs.add(propertySpecService.basicPropertySpec(activityCalendarActivationDateAttributeName, true, new DateFactory()));

        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    ActivityCalendarDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return ActivityCalendarDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultTranslation;
    }

    @Override
    public DeviceMessageId getId() {
        return this.id;
    }


    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService);
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}