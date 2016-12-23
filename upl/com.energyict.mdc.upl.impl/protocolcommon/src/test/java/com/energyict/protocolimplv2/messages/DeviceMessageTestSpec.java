package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalender;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test enum implementing DeviceMessageSpec
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/02/13
 * Time: 15:16
 */
public enum DeviceMessageTestSpec implements DeviceMessageSpecSupplier {

    TEST_SPEC_WITH_SIMPLE_SPECS {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, "testMessageSpec.simpleBigDecimal"),
                    this.stringSpec(service, "testMessageSpec.simpleString")
            );
        }
    },
    TEST_SPEC_WITH_EXTENDED_SPECS {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.tariffCalendarSpec(service, "testMessageSpec.codetable"),
                    this.dateTimeSpec(service, "testMessageSpec.activationdate")
            );
        }
    },
    TEST_SPEC_WITHOUT_SPECS {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    };

    private static final String TRANSLATION = "Translation not supported in unit testing";

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, TRANSLATION);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, TRANSLATION);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec dateTimeSpec(PropertySpecService service, String deviceMessageConstantKey) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, TRANSLATION);
        return service
                .dateTimeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec tariffCalendarSpec(PropertySpecService service, String deviceMessageConstantKey) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, TRANSLATION);
        return service
                .referenceSpec(TariffCalender.class.getName())
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    private String getNameResourceKey() {
        return DeviceMessageTestSpec.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                0, new TranslationKeyImpl(this.getNameResourceKey(), TRANSLATION),
                DeviceMessageTestCategories.CONNECTIVITY_SETUP,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

}