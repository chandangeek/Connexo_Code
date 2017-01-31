/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.ArrayList;
import java.util.List;

enum PowerConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    IEC1107LimitPowerQuality(DeviceMessageId.POWER_CONFIGURATION_IEC1107_LIMIT_POWER_QUALITY, "Limit power quality") {
        @Override
        protected TranslationKey translationKey() {
            return ConfigurationChangeDeviceMessageAttributes.powerQualityThresholdAttributeName;
        }
    },
    SetReferenceVoltage(DeviceMessageId.POWER_CONFIGURATION_SET_REFERENCE_VOLTAGE, "Set reference voltage") {
        @Override
        protected TranslationKey translationKey() {
            return PowerConfigurationDeviceMessageAttributes.ReferenceVoltageAttributeName;
        }
    },
    SetVoltageSagTimeThreshold(DeviceMessageId.POWER_CONFIGURATION_SET_VOLTAGE_SAG_TIME_THRESHOLD, "Set voltage sag time treshold") {
        @Override
        protected TranslationKey translationKey() {
            return PowerConfigurationDeviceMessageAttributes.VoltageSagTimeThresholdAttributeName;
        }
    },
    SetVoltageSwellTimeThreshold(DeviceMessageId.POWER_CONFIGURATION_SET_VOLTAGE_SWELL_TIME_THRESHOLD, "Set voltage swell time treshold") {
        @Override
        protected TranslationKey translationKey() {
            return PowerConfigurationDeviceMessageAttributes.VoltageSwellTimeThresholdAttributeName;
        }
    },
    SetVoltageSagThreshold(DeviceMessageId.POWER_CONFIGURATION_SET_VOLTAGE_SAG_THRESHOLD, "Set voltage sag threshold") {
        @Override
        protected TranslationKey translationKey() {
            return PowerConfigurationDeviceMessageAttributes.VoltageSagThresholdAttributeName;
        }
    },
    SetVoltageSwellThreshold(DeviceMessageId.POWER_CONFIGURATION_SET_VOLTAGE_SWELL_THRESHOLD, "Set voltage swell threshold") {
        @Override
        protected TranslationKey translationKey() {
            return PowerConfigurationDeviceMessageAttributes.VoltageSwellThresholdAttributeName;
        }
    },
    SetLongPowerFailureTimeThreshold(DeviceMessageId.POWER_CONFIGURATION_SET_LONG_POWER_FAILURE_TIME_THRESHOLD, "Set long power failure time treshold") {
        @Override
        protected TranslationKey translationKey() {
            return PowerConfigurationDeviceMessageAttributes.LongPowerFailureTimeThresholdAttributeName;
        }
    },
    SetLongPowerFailureThreshold(DeviceMessageId.POWER_CONFIGURATION_SET_LONG_POWER_FAILURE_THRESHOLD, "Set long power failure treshold") {
        @Override
        protected TranslationKey translationKey() {
            return PowerConfigurationDeviceMessageAttributes.LongPowerFailureThresholdAttributeName;
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    PowerConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return PowerConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
        propertySpecs.add(propertySpecService.bigDecimalSpec().named(translationKey()).fromThesaurus(thesaurus).markRequired().finish());
        return propertySpecs;
    }

    protected abstract TranslationKey translationKey();

}