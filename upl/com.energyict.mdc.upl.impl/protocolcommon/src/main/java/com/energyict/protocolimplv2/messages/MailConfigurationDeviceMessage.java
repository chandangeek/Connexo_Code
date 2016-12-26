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
    SetPOPUsername(17001, "Set POP username", DeviceMessageConstants.SetPOPUsernameAttributeName, DeviceMessageConstants.SetPOPUsernameAttributeDefaultTranslation),
    SetPOPPassword(17002, "Set POP password", DeviceMessageConstants.SetPOPPasswordAttributeName, DeviceMessageConstants.SetPOPPasswordAttributeDefaultTranslation),
    SetPOPHost(17003, "Set POP host", DeviceMessageConstants.SetPOPHostAttributeName, DeviceMessageConstants.SetPOPHostAttributeDefaultTranslation),
    SetPOPReadMailEvery(17004, "Set POP read mail every", DeviceMessageConstants.SetPOPReadMailEveryAttributeName, DeviceMessageConstants.SetPOPReadMailEveryAttributeDefaultTranslation),
    SetPOP3Options(17005, "Set POP3 options", DeviceMessageConstants.SetPOP3OptionsAttributeName, DeviceMessageConstants.SetPOP3OptionsAttributeDefaultTranslation),
    SetSMTPFrom(17006, "Set SMTP from", DeviceMessageConstants.SetSMTPFromAttributeName, DeviceMessageConstants.SetSMTPFromAttributeDefaultTranslation),
    SetSMTPTo(17007, "Set SMTP to", DeviceMessageConstants.SetSMTPToAttributeName, DeviceMessageConstants.SetSMTPToAttributeDefaultTranslation),
    SetSMTPConfigurationTo(17008, "Set SMTP configuration to", DeviceMessageConstants.SetSMTPConfigurationToAttributeName, DeviceMessageConstants.SetSMTPConfigurationToAttributeDefaultTranslation),
    SetSMTPServer(17009, "Set SMTP server", DeviceMessageConstants.SetSMTPServerAttributeName, DeviceMessageConstants.SetSMTPServerAttributeDefaultTranslation),
    SetSMTPDomain(17010, "Set SMTP domain", DeviceMessageConstants.SetSMTPDomainAttributeName, DeviceMessageConstants.SetSMTPDomainAttributeDefaultTranslation),
    SetSMTPSendMailEvery(17011, "Set SMTP send mail every", DeviceMessageConstants.SetSMTPSendMailEveryAttributeName, DeviceMessageConstants.SetSMTPSendMailEveryAttributeDefaultTranslation),
    SetSMTPCurrentInterval(17012, "Set SMTP current interval", DeviceMessageConstants.SetSMTPCurrentIntervalAttributeName, DeviceMessageConstants.SetSMTPCurrentIntervalAttributeDefaultTranslation),
    SetSMTPDatabaseID(17013, "Set SMTP database ID", DeviceMessageConstants.SetSMTPDatabaseIDAttributeName, DeviceMessageConstants.SetSMTPDatabaseIDAttributeDefaultTranslation),
    SetSMTPOptions(17014, "Set SMTP options", DeviceMessageConstants.SetSMTPOptionsAttributeName, DeviceMessageConstants.SetSMTPOptionsAttributeDefaultTranslation),
    POP3SetOption(17015, "POP3 - Set an option", DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation),
    POP3ClrOption(17016, "POP3 - Clear an option", DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation),
    SMTPSetOption(17017, "SMTP - Set an option", DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation),
    SMTPClrOption(17018, "SMTP - Clear an option", DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation);

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
    public long id() {
        return this.id;
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.MAIL_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

    private List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        return Collections.singletonList(this.stringSpec(propertySpecService, this.propertyName, this.propertyDefaultTranslation));
    }

    private PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

}