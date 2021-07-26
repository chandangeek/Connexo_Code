package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.properties.HexString;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Date;

public interface DeviceMessageSpecSupplierUtilities {
    default PropertySpecBuilder<String> optStringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    default PropertySpecBuilder<String> optStringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, int length) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpecOfExactLength(length)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    default PropertySpecBuilder<String> stringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return optStringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .markRequired();
    }

    default PropertySpecBuilder<String> stringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, int length) {
        return optStringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation, length)
                .markRequired();
    }

    default PropertySpecBuilder<BigDecimal> optBigDecimalSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    default PropertySpecBuilder<BigDecimal> bigDecimalSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return optBigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .markRequired();
    }

    default PropertySpecBuilder<ObisCode> optObisCodeSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .obisCodeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    default PropertySpecBuilder<ObisCode> obisCodeSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return optObisCodeSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .markRequired();
    }

    default PropertySpecBuilder<Boolean> optBooleanSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .booleanSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    default PropertySpecBuilder<Boolean> booleanSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return optBooleanSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .markRequired();
    }

    default PropertySpecBuilder<Duration> optDurationSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .durationSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    default PropertySpecBuilder<Date> optDateTimeSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .dateTimeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    default PropertySpecBuilder<HexString> optHexStringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .hexStringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    default PropertySpecBuilder<HexString> hexStringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return optHexStringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .markRequired();
    }

}
