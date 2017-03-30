/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

enum DLMSConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetDLMSDeviceID(DeviceMessageId.DLMS_CONFIGURATION_SET_DEVICE_ID, "Set DLMS device ID") {
        @Override
        protected TranslationKey translationKey() {
            return new SimpleTranslationKey(DeviceMessageConstants.SetDLMSDeviceIDAttributeName, "Set DLMS device id");
        }
    },
    SetDLMSMeterID(DeviceMessageId.DLMS_CONFIGURATION_SET_METER_ID, "Set DLMS meter ID") {
        @Override
        protected TranslationKey translationKey() {
            return new SimpleTranslationKey(DeviceMessageConstants.SetDLMSMeterIDAttributeName, "Set DLMS meter id");
        }
    },
    SetDLMSPassword(DeviceMessageId.DLMS_CONFIGURATION_SET_PASSWORD, "Set DLMS password") {
        @Override
        protected TranslationKey translationKey() {
            return new SimpleTranslationKey(DeviceMessageConstants.SetDLMSPasswordAttributeName, "Set DLMS password");
        }
    },
    SetDLMSIdleTime(DeviceMessageId.DLMS_CONFIGURATION_SET_IDLE_TIME, "Set DLMS idle time") {
        @Override
        protected TranslationKey translationKey() {
            return new SimpleTranslationKey(DeviceMessageConstants.SetDLMSIdleTimeAttributeName, "Set DLMS idle time");
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    DLMSConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }


    @Override
    public String getKey() {
        return DLMSConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
        propertySpecs.add(
                propertySpecService
                    .stringSpec()
                    .named(translationKey())
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        return propertySpecs;
    }

    protected abstract TranslationKey translationKey();

    protected String propertyName() {
        return translationKey().getKey();
    }

    public static TranslationKey[] allTranslationKeys() {
        return Stream.of(values()).map(DLMSConfigurationDeviceMessage::translationKey).toArray(TranslationKey[]::new);
    }

}