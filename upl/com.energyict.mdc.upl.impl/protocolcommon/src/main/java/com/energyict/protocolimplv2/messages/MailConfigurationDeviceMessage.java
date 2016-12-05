package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum MailConfigurationDeviceMessage implements DeviceMessageSpecSupplier {

    // Read Mail (POP3) Parameters
    SetPOPUsername(0, "Set POP username", DeviceMessageConstants.SetPOPUsernameAttributeName, DeviceMessageConstants.SetPOPUsernameAttributeDefaultTranslation),
    SetPOPPassword(1, "Set POP password", DeviceMessageConstants.SetPOPPasswordAttributeName, DeviceMessageConstants.SetPOPPasswordAttributeDefaultTranslation),
    SetPOPHost(2, "Set POP host", DeviceMessageConstants.SetPOPHostAttributeName, DeviceMessageConstants.SetPOPHostAttributeDefaultTranslation),
    SetPOPReadMailEvery(3, "Set POP read mail every", DeviceMessageConstants.SetPOPReadMailEveryAttributeName, DeviceMessageConstants.SetPOPReadMailEveryAttributeDefaultTranslation),
    SetPOP3Options(4, "Set POP3 options", DeviceMessageConstants.SetPOP3OptionsAttributeName, DeviceMessageConstants.SetPOP3OptionsAttributeDefaultTranslation),
    SetSMTPTo(6, "Set SMTP to", DeviceMessageConstants.SetSMTPToAttributeName, DeviceMessageConstants.SetSMTPToAttributeDefaultTranslation),
    SetSMTPConfigurationTo(7, "Set SMTP configuration to", DeviceMessageConstants.SetSMTPConfigurationToAttributeName, DeviceMessageConstants.SetSMTPConfigurationToAttributeDefaultTranslation),
    SetSMTPServer(8, "Set SMTP server", DeviceMessageConstants.SetSMTPServerAttributeName, DeviceMessageConstants.SetSMTPServerAttributeDefaultTranslation),
    SetSMTPDomain(9, "Set SMTP domain", DeviceMessageConstants.SetSMTPDomainAttributeName, DeviceMessageConstants.SetSMTPDomainAttributeDefaultTranslation),
    SetSMTPSendMailEvery(10, "Set SMTP send mail every", DeviceMessageConstants.SetSMTPSendMailEveryAttributeName, DeviceMessageConstants.SetSMTPSendMailEveryAttributeDefaultTranslation),
    SetSMTPCurrentInterval(11, "Set SMTP current interval", DeviceMessageConstants.SetSMTPCurrentIntervalAttributeName, DeviceMessageConstants.SetSMTPCurrentIntervalAttributeDefaultTranslation),
    SetSMTPDatabaseID(12, "Set SMTP database ID", DeviceMessageConstants.SetSMTPDatabaseIDAttributeName, DeviceMessageConstants.SetSMTPDatabaseIDAttributeDefaultTranslation),
    SetSMTPOptions(13, "Set SMTP options", DeviceMessageConstants.SetSMTPOptionsAttributeName, DeviceMessageConstants.SetSMTPOptionsAttributeDefaultTranslation),
    POP3SetOption(14, "POP3 - Set an option", DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation),
    POP3ClrOption(15, "POP3 - Clear an option", DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation),
    SMTPSetOption(16, "SMTP - Set an option", DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation),
    SMTPClrOption(17, "SMTP - Clear an option", DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation);

    private final long id;
    private final String defaultNameTranslation;
    private final String propertyName;
    private final String propertyDefaultTranslation;

    MailConfigurationDeviceMessage(long id, String defaultNameTranslation, String propertyName, String propertyDefaultTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
        this.propertyName = propertyName;
        this.propertyDefaultTranslation = propertyDefaultTranslation;
    }

    private String getNameResourceKey() {
        return MailConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                this.id,
                new EnumBasedDeviceMessageSpecPrimaryKey(this, name()),
                new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.MAIL_CONFIGURATION,
                this.getPropertySpec(propertySpecService),
                propertySpecService, nlsService);
    }

    private List<PropertySpec> getPropertySpec(PropertySpecService propertySpecService) {
        return Collections.singletonList(this.stringSpec(propertySpecService, this.propertyName, this.propertyDefaultTranslation));
    }

    private PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

}