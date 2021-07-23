package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.time.Duration;
import java.util.Date;

/**
 * Produces {@link DeviceMessageSpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-01 (13:04)
 */
public interface DeviceMessageSpecSupplier {

    long id();

    DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter);

    default boolean equals(DeviceMessageSpec deviceMessageSpec) {
        return id() == deviceMessageSpec.getId();
    }

    default PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    default PropertySpec optStringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.optStringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    default PropertySpecBuilder<String> optStringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    default PropertySpecBuilder<String> stringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return optStringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .markRequired();
    }

    default PropertySpecBuilder<Boolean> optBooleanSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .booleanSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    default PropertySpec optBooleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return optBooleanSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .finish();
    }

    default PropertySpec booleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return optBooleanSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .markRequired()
                .finish();
    }

    default PropertySpecBuilder<Duration> optDurationSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .durationSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    default PropertySpec durationSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return optDurationSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .markRequired()
                .finish();
    }

    default PropertySpec optDurationSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return optDurationSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .finish();
    }

    default PropertySpecBuilder<Date> optDateTimeSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .dateTimeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    default PropertySpec dateTimeSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return optDateTimeSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .markRequired()
                .finish();
    }

    default PropertySpec optDateTimeSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return optDateTimeSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .finish();
    }
}