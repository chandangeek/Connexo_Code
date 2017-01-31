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

enum OpusConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetOpusOSNbr(DeviceMessageId.OPUS_CONFIGURATION_SET_OS_NUMBER, "Set opus OS number") {
        @Override
        protected TranslationKey translationKey() {
            return OpusDeviceMessageAttributes.SetOpusOSNbrAttributeName;
        }
    },
    SetOpusPassword(DeviceMessageId.OPUS_CONFIGURATION_SET_PASSWORD, "Set opus password") {
        @Override
        protected TranslationKey translationKey() {
            return OpusDeviceMessageAttributes.SetOpusPasswordAttributeName;
        }
    },
    SetOpusTimeout(DeviceMessageId.OPUS_CONFIGURATION_SET_TIMEOUT, "Set opus timeout") {
        @Override
        protected TranslationKey translationKey() {
            return OpusDeviceMessageAttributes.SetOpusTimeoutAttributeName;
        }
    },
    SetOpusConfig(DeviceMessageId.OPUS_CONFIGURATION_SET_CONFIG, "Set opus configuration") {
        @Override
        protected TranslationKey translationKey() {
            return OpusDeviceMessageAttributes.SetOpusConfigAttributeName;
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    OpusConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return OpusConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
        propertySpecs.add(propertySpecService.stringSpec().named(this.translationKey()).fromThesaurus(thesaurus).markRequired().finish());
        return propertySpecs;
    }

    protected abstract TranslationKey translationKey();

}