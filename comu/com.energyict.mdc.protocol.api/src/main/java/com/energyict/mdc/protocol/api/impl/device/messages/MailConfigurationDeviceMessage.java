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

enum MailConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    // Read Mail (POP3) Parameters
    SetPOPUsername(DeviceMessageId.MAIL_CONFIGURATION_SET_POP_USERNAME, "Set POP username") {
        @Override
        protected TranslationKey translationKey() {
            return MailDeviceMessageAttributes.SetPOPUsernameAttributeName;
        }
    },
    SetPOPPassword(DeviceMessageId.MAIL_CONFIGURATION_SET_POP_PASSWORD, "Set POP password") {
        @Override
        protected TranslationKey translationKey() {
            return MailDeviceMessageAttributes.SetPOPPasswordAttributeName;
        }
    },
    SetPOPHost(DeviceMessageId.MAIL_CONFIGURATION_SET_POP_HOST, "Set POP host") {
        @Override
        protected TranslationKey translationKey() {
            return MailDeviceMessageAttributes.SetPOPHostAttributeName;
        }
    },
    SetPOPReadMailEvery(DeviceMessageId.MAIL_CONFIGURATION_SET_POP_READ_MAIL_EVERY, "Set POP read mail every") {
        @Override
        protected TranslationKey translationKey() {
            return MailDeviceMessageAttributes.SetPOPReadMailEveryAttributeName;
        }
    },
    SetPOP3Options(DeviceMessageId.MAIL_CONFIGURATION_SET_POP3_OPTIONS, "Set POP3 options") {
        @Override
        protected TranslationKey translationKey() {
            return MailDeviceMessageAttributes.SetPOP3OptionsAttributeName;
        }
    },

    // Send Mail (SMTP) Parameters
    SetSMTPFrom(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_FROM, "Set SMTP from") {
        @Override
        protected TranslationKey translationKey() {
            return MailDeviceMessageAttributes.SetSMTPFromAttributeName;
        }
    },
    SetSMTPTo(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_TO, "Set SMTP to") {
        @Override
        protected TranslationKey translationKey() {
            return MailDeviceMessageAttributes.SetSMTPToAttributeName;
        }
    },
    SetSMTPConfigurationTo(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_CONFIGURATION_TO, "Set SMTP configuration to") {
        @Override
        protected TranslationKey translationKey() {
            return MailDeviceMessageAttributes.SetSMTPConfigurationToAttributeName;
        }
    },
    SetSMTPServer(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_SERVER, "Set SMTP server") {
        @Override
        protected TranslationKey translationKey() {
            return MailDeviceMessageAttributes.SetSMTPServerAttributeName;
        }
    },
    SetSMTPDomain(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_DOMAIN, "Set SMTP domain") {
        @Override
        protected TranslationKey translationKey() {
            return MailDeviceMessageAttributes.SetSMTPDomainAttributeName;
        }
    },
    SetSMTPSendMailEvery(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_SEND_MAIL_EVERY, "Set SMTP send mail every") {
        @Override
        protected TranslationKey translationKey() {
            return MailDeviceMessageAttributes.SetSMTPSendMailEveryAttributeName;
        }
    },
    SetSMTPCurrentInterval(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_CURRENT_INTERVAL, "Set SMTP current interval") {
        @Override
        protected TranslationKey translationKey() {
            return MailDeviceMessageAttributes.SetSMTPCurrentIntervalAttributeName;
        }
    },
    SetSMTPDatabaseID(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_DATABASE_ID, "Set SMTP database ID") {
        @Override
        protected TranslationKey translationKey() {
            return MailDeviceMessageAttributes.SetSMTPDatabaseIDAttributeName;
        }
    },
    SetSMTPOptions(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_OPTIONS, "Set SMTP options") {
        @Override
        protected TranslationKey translationKey() {
            return MailDeviceMessageAttributes.SetSMTPOptionsAttributeName;
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    MailConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return MailConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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

}