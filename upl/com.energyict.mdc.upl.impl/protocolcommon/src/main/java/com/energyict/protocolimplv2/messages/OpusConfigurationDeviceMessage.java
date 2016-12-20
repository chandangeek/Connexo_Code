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
public enum OpusConfigurationDeviceMessage implements DeviceMessageSpecSupplier {

    SetOpusOSNbr(25001, "Set opus OS number", DeviceMessageConstants.SetOpusOSNbrAttributeName, DeviceMessageConstants.SetOpusOSNbrAttributeDefaultTranslation),
    SetOpusPassword(25002, "Set opus password", DeviceMessageConstants.SetOpusPasswordAttributeName, DeviceMessageConstants.SetOpusPasswordAttributeDefaultTranslation),
    SetOpusTimeout(25003, "Set opus timeout", DeviceMessageConstants.SetOpusTimeoutAttributeName, DeviceMessageConstants.SetOpusTimeoutAttributeDefaultTranslation),
    SetOpusConfig(25004, "Set opus configuration", DeviceMessageConstants.SetOpusConfigAttributeName, DeviceMessageConstants.SetOpusConfigAttributeDefaultTranslation),
    OpusSetOption(25005, "Opus - Set an option", DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation),
    OpusClrOption(25006, "Opus - Clear an option", DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation);

    private final long id;
    private final String defaultNameTranslation;
    private final String propertyName;
    private final String propertyDefaultTranslation;

    OpusConfigurationDeviceMessage(long id, String defaultNameTranslation, String propertyName, String propertyDefaultTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
        this.propertyName = propertyName;
        this.propertyDefaultTranslation = propertyDefaultTranslation;
    }

    private String getNameResourceKey() {
        return OpusConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.OPUS_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
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