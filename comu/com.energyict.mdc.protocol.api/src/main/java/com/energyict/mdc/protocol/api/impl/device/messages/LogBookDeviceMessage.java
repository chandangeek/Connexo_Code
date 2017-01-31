/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.toDateAttributeName;

enum LogBookDeviceMessage implements DeviceMessageSpecEnum {

    SetInputChannel(DeviceMessageId.LOG_BOOK_SET_INPUT_CHANNEL, "Set input channel") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(EventsDeviceMessageAttributes.SetInputChannelAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetCondition(DeviceMessageId.LOG_BOOK_SET_CONDITION, "Set condition") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(EventsDeviceMessageAttributes.SetConditionAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetConditionValue(DeviceMessageId.LOG_BOOK_SET_CONDITION_VALUE, "Set condition value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(EventsDeviceMessageAttributes.SetConditionValueAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetTimeTrue(DeviceMessageId.LOG_BOOK_SET_TIME_TRUE, "Set time true") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(EventsDeviceMessageAttributes.SetTimeTrueAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetTimeFalse(DeviceMessageId.LOG_BOOK_SET_TIME_FALSE, "Set time false") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(EventsDeviceMessageAttributes.SetTimeFalseAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetOutputChannel(DeviceMessageId.LOG_BOOK_SET_OUTPUT_CHANNEL, "Set output channel") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(EventsDeviceMessageAttributes.SetOutputChannelAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetAlarm(DeviceMessageId.LOG_BOOK_SET_ALARM, "Set alarm") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(EventsDeviceMessageAttributes.SetAlarmAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetTag(DeviceMessageId.LOG_BOOK_SET_TAG, "Set tag") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(EventsDeviceMessageAttributes.SetTagAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetInverse(DeviceMessageId.LOG_BOOK_SET_INVERSE, "Set inverse") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(EventsDeviceMessageAttributes.SetInverseAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetImmediate(DeviceMessageId.LOG_BOOK_SET_IMMEDIATE, "Set immediate") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(EventsDeviceMessageAttributes.SetImmediateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    ReadDebugLogBook(DeviceMessageId.LOG_BOOK_READ_DEBUG, "Read debug logbook") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(fromDateAttributeName, EventsDeviceMessageAttributes.fromDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(toDateAttributeName, EventsDeviceMessageAttributes.toDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    ReadManufacturerSpecificLogBook(DeviceMessageId.LOG_BOOK_READ_MANUFACTURER_SPECIFIC, "Read manufacturer specific logbook") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(fromDateAttributeName, EventsDeviceMessageAttributes.fromDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(toDateAttributeName, EventsDeviceMessageAttributes.toDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    ResetMainLogbook(DeviceMessageId.LOG_BOOK_RESET_MAIN_LOGBOOK, "Reset main logbook"),
    ResetCoverLogbook(DeviceMessageId.LOG_BOOK_RESET_COVER_LOGBOOK, "Reset cover logbook"),
    ResetBreakerLogbook(DeviceMessageId.LOG_BOOK_RESET_BREAKER_LOGBOOK, "Reset breaker logbook"),
    ResetCommunicationLogbook(DeviceMessageId.LOG_BOOK_RESET_COMMUNICATION_LOGBOOK, "Reset communication logbook"),
    ResetLQILogbook(DeviceMessageId.LOG_BOOK_RESET_LQI_LOGBOOK, "Reset LQI logbook"),
    ResetVoltageCutLogbook(DeviceMessageId.LOG_BOOK_RESET_VOLTAGE_CUT_LOGBOOK, "Reset ");

    private DeviceMessageId id;
    private String defaultTranslation;

    LogBookDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return LogBookDeviceMessage.class.getSimpleName() + "." + this.toString();
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
    }

}
