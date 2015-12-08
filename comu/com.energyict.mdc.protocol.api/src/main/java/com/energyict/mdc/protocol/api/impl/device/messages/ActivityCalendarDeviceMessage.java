package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttributes;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.contractAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.contractsXmlUserFileAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.specialDaysCodeTableAttributeName;

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
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService.referencePropertySpec(contractsXmlUserFileAttributeName, true, FactoryIds.USERFILE));
        }
    },
    ACTIVITY_CALENDER_SEND(DeviceMessageId.ACTIVITY_CALENDER_SEND, "Send activity calendar") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.activityCalendarNameAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(propertySpecService.referencePropertySpec(activityCalendarCodeTableAttributeName, true, FactoryIds.CODE));
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME, "Send activity calendar with activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.activityCalendarNameAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(propertySpecService.referencePropertySpec(activityCalendarCodeTableAttributeName, true, FactoryIds.CODE));
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DeviceMessageAttributes.activityCalendarActivationDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE, "Send activity calendar with activation date and type") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.activityCalendarTypeAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues(ActivityCalendarType.getAllDescriptions())
                            .markExhaustive()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.activityCalendarNameAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(propertySpecService.referencePropertySpec(activityCalendarCodeTableAttributeName, true, FactoryIds.CODE));
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DeviceMessageAttributes.activityCalendarActivationDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATE(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATE, "Send activity calendar with activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.activityCalendarNameAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(propertySpecService.referencePropertySpec(activityCalendarCodeTableAttributeName, true, FactoryIds.CODE));
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DeviceMessageAttributes.activityCalendarActivationDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SPECIAL_DAY_CALENDAR_SEND(DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND, "Send special days calendar") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService.referencePropertySpec(specialDaysCodeTableAttributeName, true, FactoryIds.CODE));
        }
    },
    SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE(DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE, "Send special days calendar with type") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.activityCalendarTypeAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues(ActivityCalendarType.getAllDescriptions())
                            .markExhaustive()
                            .finish());
            propertySpecs.add(propertySpecService.referencePropertySpec(specialDaysCodeTableAttributeName, true, FactoryIds.CODE));
        }
    },
    CLEAR_AND_DISABLE_PASSIVE_TARIFF(DeviceMessageId.ACTIVITY_CALENDAR_CLEAR_AND_DISABLE_PASSIVE_TARIFF, "Clear and disable passive tariff"),
    ACTIVATE_PASSIVE_CALENDAR(DeviceMessageId.ACTIVATE_CALENDAR_PASSIVE, "Activate passive calendar") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DeviceMessageAttributes.activityCalendarActivationDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    }, ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT, "Send activity calendar with date and contract"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(contractAttributeName, true, BigDecimal.ONE, BigDecimals.TWO));
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.activityCalendarNameAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue("")
                            .finish());
            propertySpecs.add(propertySpecService.referencePropertySpec(activityCalendarCodeTableAttributeName, true, FactoryIds.CODE));
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DeviceMessageAttributes.activityCalendarActivationDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME(DeviceMessageId.ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME, "Send special day calendar with date and contract"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(contractAttributeName, true, BigDecimal.ONE, BigDecimals.TWO));
            propertySpecs.add(propertySpecService.referencePropertySpec(specialDaysCodeTableAttributeName, true, FactoryIds.CODE));
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DeviceMessageAttributes.activityCalendarActivationDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());

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


    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
        return propertySpecs;
    }

    protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        // Default behavior is not to add anything
    };

}