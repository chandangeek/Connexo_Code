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

enum SMSConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetSmsDataNbr(DeviceMessageId.SMS_CONFIGURATION_SET_DATA_NUMBER, "Set SMS data number") {
        @Override
        protected TranslationKey translationKey() {
            return SMSConfigurationDeviceMessageAttributes.SetSmsDataNbrAttributeName;
        }
    },
    SetSmsAlarmNbr(DeviceMessageId.SMS_CONFIGURATION_SET_ALARM_NUMBER, "Set SMS alarm number") {
        @Override
        protected TranslationKey translationKey() {
            return SMSConfigurationDeviceMessageAttributes.SetSmsAlarmNbrAttributeName;
        }
    },
    SetSmsEvery(DeviceMessageId.SMS_CONFIGURATION_SET_EVERY, "Set SMS every") {
        @Override
        protected TranslationKey translationKey() {
            return SMSConfigurationDeviceMessageAttributes.SetSmsEveryAttributeName;
        }
    },
    SetSmsNbr(DeviceMessageId.SMS_CONFIGURATION_SET_NUMBER, "Set SMS number") {
        @Override
        protected TranslationKey translationKey() {
            return SMSConfigurationDeviceMessageAttributes.SetSmsNbrAttributeName;
        }
    },
    SetSmsCorrection(DeviceMessageId.SMS_CONFIGURATION_SET_CORRECTION, "Set SMS correction") {
        @Override
        protected TranslationKey translationKey() {
            return SMSConfigurationDeviceMessageAttributes.SetSmsCorrectionAttributeName;
        }
    },
    SetSmsConfig(DeviceMessageId.SMS_CONFIGURATION_SET_CONFIG, "Set SMS configuration") {
        @Override
        protected TranslationKey translationKey() {
            return SMSConfigurationDeviceMessageAttributes.SetSmsConfigAttributeName;
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    SMSConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return SMSConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
        propertySpecs.add(propertySpecService.stringSpec().named(translationKey()).fromThesaurus(thesaurus).markRequired().finish());
        return propertySpecs;
    }

    protected abstract TranslationKey translationKey();

}