package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Collections;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.prepaidCreditAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.prepaidCreditAttributeNameDefaultTranslation;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PrepaidConfigurationDeviceMessage implements DeviceMessageSpecSupplier {

    AddPrepaidCredit(27001, "Add prepaid credit") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            TranslationKeyImpl translationKey = new TranslationKeyImpl(prepaidCreditAttributeName, prepaidCreditAttributeNameDefaultTranslation);
            return Collections.singletonList(
                service
                    .bigDecimalSpec()
                    .named(prepaidCreditAttributeName, translationKey)
                    .describedAs(translationKey.description())
                    .markRequired()
                    .finish());
        }
    },
    DisablePrepaid(27002, "Disable prepaid") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    EnablePrepaid(27003, "Enable prepaid") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    PrepaidConfigurationDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    @Override
    public long id() {
        return this.id;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    private String getNameResourceKey() {
        return PrepaidConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.PREPAID_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

}