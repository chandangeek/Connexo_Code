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
public enum DLMSConfigurationDeviceMessage implements DeviceMessageSpecSupplier {

    SetDLMSDeviceID(28001, "Set DLMS device ID", DeviceMessageConstants.SetDLMSDeviceIDAttributeName, DeviceMessageConstants.SetDLMSDeviceIDAttributeDefaultTranslation),
    SetDLMSMeterID(28002, "Set DLMS meter ID", DeviceMessageConstants.SetDLMSMeterIDAttributeName, DeviceMessageConstants.SetDLMSMeterIDAttributeDefaultTranslation),
    SetDLMSPassword(28003, "Set DLMS password", DeviceMessageConstants.SetDLMSPasswordAttributeName, DeviceMessageConstants.SetDLMSPasswordAttributeDefaultTranslation),
    SetDLMSIdleTime(28004, "Set DLMS idle time", DeviceMessageConstants.SetDLMSIdleTimeAttributeName, DeviceMessageConstants.SetDLMSIdleTimeAttributeDefaultTranslation);

    private final long id;
    private final String defaultNameTranslation;
    private final String propertyName;
    private final String propertyDefaultTranslation;

    DLMSConfigurationDeviceMessage(long id, String defaultNameTranslation, String propertyName, String propertyDefaultTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
        this.propertyName = propertyName;
        this.propertyDefaultTranslation = propertyDefaultTranslation;
    }

    private String getNameResourceKey() {
        return DLMSConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
                DeviceMessageCategories.DLMS_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}