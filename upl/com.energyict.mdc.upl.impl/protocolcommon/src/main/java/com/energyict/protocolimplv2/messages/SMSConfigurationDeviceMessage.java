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
public enum SMSConfigurationDeviceMessage implements DeviceMessageSpecSupplier {

    SetSmsDataNbr(21001, "Set sms data number", DeviceMessageConstants.SetSmsDataNbrAttributeName, DeviceMessageConstants.SetSmsDataNbrAttributeDefaultTranslation),
    SetSmsAlarmNbr(21002, "Set sms alarm number", DeviceMessageConstants.SetSmsAlarmNbrAttributeName, DeviceMessageConstants.SetSmsAlarmNbrAttributeDefaultTranslation),
    SetSmsEvery(21003, "Set sms every", DeviceMessageConstants.SetSmsEveryAttributeName, DeviceMessageConstants.SetSmsEveryAttributeDefaultTranslation),
    SetSmsNbr(21004, "Set sms number", DeviceMessageConstants.SetSmsNbrAttributeName, DeviceMessageConstants.SetSmsNbrAttributeDefaultTranslation),
    SetSmsCorrection(21005, "Set sms correction", DeviceMessageConstants.SetSmsCorrectionAttributeName, DeviceMessageConstants.SetSmsCorrectionAttributeDefaultTranslation),
    SetSmsConfig(21006, "Set sms configuration", DeviceMessageConstants.SetSmsConfigAttributeName, DeviceMessageConstants.SetSmsConfigAttributeDefaultTranslation),
    SMSSetOption(21007, "SMS - Set an option", DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation),
    SMSClrOption(21008, "SMS - Clear an option", DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation);

    private final long id;
    private final String defaultNameTranslation;
    private final String propertyName;
    private final String propertyDefaultTranslation;

    SMSConfigurationDeviceMessage(long id, String defaultNameTranslation, String propertyName, String propertyDefaultTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
        this.propertyName = propertyName;
        this.propertyDefaultTranslation = propertyDefaultTranslation;
    }

    private String getNameResourceKey() {
        return SMSConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    private List<PropertySpec> getPropertySpecs(PropertySpecService service) {
        return Collections.singletonList(this.stringSpec(service, this.propertyName, this.propertyDefaultTranslation));
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

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.SMS_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}