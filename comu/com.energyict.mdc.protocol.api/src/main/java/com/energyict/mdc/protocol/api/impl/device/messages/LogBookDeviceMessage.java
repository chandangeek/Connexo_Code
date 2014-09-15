package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.SetAlarmAttributeName;
import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.SetConditionAttributeName;
import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.SetConditionValueAttributeName;
import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.SetImmediateAttributeName;
import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.SetInputChannelAttributeName;
import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.SetInverseAttributeName;
import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.SetOutputChannelAttributeName;
import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.SetTagAttributeName;
import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.SetTimeFalseAttributeName;
import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.SetTimeTrueAttributeName;
import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.toDateAttributeName;

/**
 * Provides a summary of all DeviceMessages related to configuration/readout of LogBooks
 *
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum LogBookDeviceMessage implements DeviceMessageSpecEnum {

    SetInputChannel("Set input channel") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetInputChannelAttributeName, true, new StringFactory()));
        }
    },
    SetCondition("Set condition") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetConditionAttributeName, true, new StringFactory()));
        }
    },
    SetConditionValue("Set condition value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetConditionValueAttributeName, true, new StringFactory()));
        }
    },
    SetTimeTrue("Set time true") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetTimeTrueAttributeName, true, new StringFactory()));
        }
    },
    SetTimeFalse("Set time false") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetTimeFalseAttributeName, true, new StringFactory()));
        }
    },
    SetOutputChannel("Set output channel") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetOutputChannelAttributeName, true, new StringFactory()));
        }
    },
    SetAlarm("Set alarm") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetAlarmAttributeName, true, new StringFactory()));
        }
    },
    SetTag("Set tag") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetTagAttributeName, true, new StringFactory()));
        }
    },
    SetInverse("Set inverse") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetInverseAttributeName, true, new StringFactory()));
        }
    },
    SetImmediate("Set immediate") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetImmediateAttributeName, true, new StringFactory()));
        }
    },
    ReadDebugLogBook("Read debug logbook") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(fromDateAttributeName, true, new DateAndTimeFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(toDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    ReadManufacturerSpecificLogBook("Read manufacturer specific logbook") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(fromDateAttributeName, true, new DateAndTimeFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(toDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    ResetMainLogbook("Reset main logbook"),
    ResetCoverLogbook("Reset cover logbook"),
    ResetBreakerLogbook("Reset breaker logbook"),
    ResetCommunicationLogbook("Reset communication logbook"),
    ResetLQILogbook("Reset LQI logbook"),
    ResetVoltageCutLogbook("Reset ");

    private String defaultTranslation;

    LogBookDeviceMessage(String defaultTranslation) {
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getNameResourceKey() {
        return LogBookDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String defaultTranslation() {
        return this.defaultTranslation;
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

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKeyImpl(this, name());
    }

}