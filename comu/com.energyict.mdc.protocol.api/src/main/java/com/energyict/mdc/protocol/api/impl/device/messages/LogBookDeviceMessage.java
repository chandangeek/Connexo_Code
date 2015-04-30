package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetAlarmAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetConditionAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetConditionValueAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetImmediateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetInputChannelAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetInverseAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetOutputChannelAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetTagAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetTimeFalseAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetTimeTrueAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.toDateAttributeName;

/**
 * Provides a summary of all DeviceMessages related to configuration/readout of LogBooks
 *
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum LogBookDeviceMessage implements DeviceMessageSpecEnum {

    SetInputChannel(DeviceMessageId.LOG_BOOK_SET_INPUT_CHANNEL, "Set input channel") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetInputChannelAttributeName, true, new StringFactory()));
        }
    },
    SetCondition(DeviceMessageId.LOG_BOOK_SET_CONDITION, "Set condition") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetConditionAttributeName, true, new StringFactory()));
        }
    },
    SetConditionValue(DeviceMessageId.LOG_BOOK_SET_CONDITION_VALUE, "Set condition value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetConditionValueAttributeName, true, new StringFactory()));
        }
    },
    SetTimeTrue(DeviceMessageId.LOG_BOOK_SET_TIME_TRUE, "Set time true") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetTimeTrueAttributeName, true, new StringFactory()));
        }
    },
    SetTimeFalse(DeviceMessageId.LOG_BOOK_SET_TIME_FALSE, "Set time false") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetTimeFalseAttributeName, true, new StringFactory()));
        }
    },
    SetOutputChannel(DeviceMessageId.LOG_BOOK_SET_OUTPUT_CHANNEL, "Set output channel") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetOutputChannelAttributeName, true, new StringFactory()));
        }
    },
    SetAlarm(DeviceMessageId.LOG_BOOK_SET_ALARM, "Set alarm") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetAlarmAttributeName, true, new StringFactory()));
        }
    },
    SetTag(DeviceMessageId.LOG_BOOK_SET_TAG, "Set tag") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetTagAttributeName, true, new StringFactory()));
        }
    },
    SetInverse(DeviceMessageId.LOG_BOOK_SET_INVERSE, "Set inverse") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetInverseAttributeName, true, new StringFactory()));
        }
    },
    SetImmediate(DeviceMessageId.LOG_BOOK_SET_IMMEDIATE, "Set immediate") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetImmediateAttributeName, true, new StringFactory()));
        }
    },
    ReadDebugLogBook(DeviceMessageId.LOG_BOOK_READ_DEBUG, "Read debug logbook") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(fromDateAttributeName, true, new DateAndTimeFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(toDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    ReadManufacturerSpecificLogBook(DeviceMessageId.LOG_BOOK_READ_MANUFACTURER_SPECIFIC, "Read manufacturer specific logbook") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(fromDateAttributeName, true, new DateAndTimeFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(toDateAttributeName, true, new DateAndTimeFactory()));
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