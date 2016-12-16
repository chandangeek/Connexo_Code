package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Collections;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.UserFileConfigAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.UserFileConfigAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.xmlConfigAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.xmlConfigAttributeName;

/**
 * Provides a summary of all <i>Advanced Test</i> related messages.
 *
 * Copyrights EnergyICT
 * Date: 2/05/13
 * Time: 9:52
 */
public enum AdvancedTestMessage implements DeviceMessageSpecSupplier {

    XML_CONFIG(32001, "XML configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, xmlConfigAttributeName, xmlConfigAttributeDefaultTranslation));
        }
    },
    USERFILE_CONFIG(32002, "User file configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceMessageFileSpec(service, UserFileConfigAttributeName, UserFileConfigAttributeDefaultTranslation));
        }
    },
    LogObjectList(32003, "Log object list") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    AdvancedTestMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec deviceMessageFileSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .referenceSpec(DeviceMessageFile.class.getName())
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    private String getNameResourceKey() {
        return AdvancedTestMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.ALARM_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}