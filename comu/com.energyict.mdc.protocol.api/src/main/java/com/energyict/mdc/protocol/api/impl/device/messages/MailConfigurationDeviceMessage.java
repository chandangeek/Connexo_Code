package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum MailConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    // Read Mail (POP3) Parameters
    SetPOPUsername(DeviceMessageId.MAIL_CONFIGURATION_SET_POP_USERNAME, "Set POP username") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetPOPUsernameAttributeName;
        }
    },
    SetPOPPassword(DeviceMessageId.MAIL_CONFIGURATION_SET_POP_PASSWORD, "Set POP password") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetPOPPasswordAttributeName;
        }
    },
    SetPOPHost(DeviceMessageId.MAIL_CONFIGURATION_SET_POP_HOST, "Set POP host") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetPOPHostAttributeName;
        }
    },
    SetPOPReadMailEvery(DeviceMessageId.MAIL_CONFIGURATION_SET_POP_READ_MAIL_EVERY, "Set POP read mail every") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetPOPReadMailEveryAttributeName;
        }
    },
    SetPOP3Options(DeviceMessageId.MAIL_CONFIGURATION_SET_POP3_OPTIONS, "Set POP3 options") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetPOP3OptionsAttributeName;
        }
    },

    // Send Mail (SMTP) Parameters
    SetSMTPFrom(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_FROM, "Set SMTP from") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetSMTPFromAttributeName;
        }
    },
    SetSMTPTo(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_TO, "Set SMTP to") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetSMTPToAttributeName;
        }
    },
    SetSMTPConfigurationTo(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_CONFIGURATION_TO, "Set SMTP configuration to") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetSMTPConfigurationToAttributeName;
        }
    },
    SetSMTPServer(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_SERVER, "Set SMTP server") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetSMTPServerAttributeName;
        }
    },
    SetSMTPDomain(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_DOMAIN, "Set SMTP domain") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetSMTPDomainAttributeName;
        }
    },
    SetSMTPSendMailEvery(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_SEND_MAIL_EVERY, "Set SMTP send mail every") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetSMTPSendMailEveryAttributeName;
        }
    },
    SetSMTPCurrentInterval(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_CURRENT_INTERVAL, "Set SMTP current interval") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetSMTPCurrentIntervalAttributeName;
        }
    },
    SetSMTPDatabaseID(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_DATABASE_ID, "Set SMTP database ID") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetSMTPDatabaseIDAttributeName;
        }
    },
    SetSMTPOptions(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_OPTIONS, "Set SMTP options") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetSMTPOptionsAttributeName;
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

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(this.stringProperty(this.propertyName(), propertySpecService));
        return propertySpecs;
    }

    private PropertySpec stringProperty(String name, PropertySpecService propertySpecService) {
        return propertySpecService.basicPropertySpec(name, true, new StringFactory());
    }

    protected abstract String propertyName();

    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}