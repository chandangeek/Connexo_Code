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

enum PPPConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetISP1Phone(DeviceMessageId.PPP_CONFIGURATION_SET_ISP1_PHONE, "Set ISP1 phone") {
        @Override
        protected TranslationKey translationKey() {
            return PPPConfigurationDeviceMessageAttributes.SetISP1PhoneAttributeName;
        }
    },
    SetISP1Username(DeviceMessageId.PPP_CONFIGURATION_SET_ISP1_USERNAME, "Set ISP1 username") {
        @Override
        protected TranslationKey translationKey() {
            return PPPConfigurationDeviceMessageAttributes.SetISP1UsernameAttributeName;
        }
    },
    SetISP1Password(DeviceMessageId.PPP_CONFIGURATION_SET_ISP1_PASSWORD, "Set ISP1 password") {
        @Override
        protected TranslationKey translationKey() {
            return PPPConfigurationDeviceMessageAttributes.SetISP1PasswordAttributeName;
        }
    },
    SetISP1Tries(DeviceMessageId.PPP_CONFIGURATION_SET_ISP1_TRIES, "Set ISP1 tries") {
        @Override
        protected TranslationKey translationKey() {
            return PPPConfigurationDeviceMessageAttributes.SetISP1TriesAttributeName;
        }
    },
    SetISP2Phone(DeviceMessageId.PPP_CONFIGURATION_SET_ISP2_PHONE, "Set ISP2 phone") {
        @Override
        protected TranslationKey translationKey() {
            return PPPConfigurationDeviceMessageAttributes.SetISP2PhoneAttributeName;
        }
    },
    SetISP2Username(DeviceMessageId.PPP_CONFIGURATION_SET_ISP2_USERNAME, "Set ISP2 username") {
        @Override
        protected TranslationKey translationKey() {
            return PPPConfigurationDeviceMessageAttributes.SetISP2UsernameAttributeName;
        }
    },
    SetISP2Password(DeviceMessageId.PPP_CONFIGURATION_SET_ISP2_PASSWORD, "Set ISP2 password") {
        @Override
        protected TranslationKey translationKey() {
            return PPPConfigurationDeviceMessageAttributes.SetISP2PasswordAttributeName;
        }
    },
    SetISP2Tries(DeviceMessageId.PPP_CONFIGURATION_SET_ISP2_TRIES, "Set ISP2 tries") {
        @Override
        protected TranslationKey translationKey() {
            return PPPConfigurationDeviceMessageAttributes.SetISP2TriesAttributeName;
        }
    },
    SetPPPIdleTimeout(DeviceMessageId.PPP_CONFIGURATION_SET_IDLE_TIMEOUT, "Set PPP idle timeout") {
        @Override
        protected TranslationKey translationKey() {
            return PPPConfigurationDeviceMessageAttributes.SetPPPIdleTimeoutAttributeName;
        }
    },
    SetPPPRetryInterval(DeviceMessageId.PPP_CONFIGURATION_SET_RETRY_INTERVAL, "Set PPP retry interval") {
        @Override
        protected TranslationKey translationKey() {
            return PPPConfigurationDeviceMessageAttributes.SetPPPRetryIntervalAttributeName;
        }
    },
    SetPPPOptions(DeviceMessageId.PPP_CONFIGURATION_SET_OPTIONS, "Set PPP options") {
        @Override
        protected TranslationKey translationKey() {
            return PPPConfigurationDeviceMessageAttributes.SetPPPOptionsAttributeName;
        }
    },
    SetPPPIdleTime(DeviceMessageId.PPP_CONFIGURATION_SET_IDLE_TIME, "Set PPP idle time"){
        @Override
        protected TranslationKey translationKey() {
            return PPPConfigurationDeviceMessageAttributes.SetPPPIdleTime;
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    PPPConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return PPPConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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