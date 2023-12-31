package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.HexString;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;

/**
 * Produces {@link DeviceMessageSpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-01 (13:04)
 */
public interface DeviceMessageSpecSupplier extends DeviceMessageSpecSupplierUtilities {

    long id();

    DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter);

    default boolean equals(DeviceMessageSpec deviceMessageSpec) {
        return id() == deviceMessageSpec.getId();
    }

    default PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    default PropertySpec optStringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return optStringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    default PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String... possibleValues) {
        return stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(possibleValues)
                .markExhaustive()
                .finish();
    }

    default PropertySpec optStringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String... possibleValues) {
        return optStringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(possibleValues)
                .markExhaustive()
                .finish();
    }

    default PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, int length) {
        return stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation, length).finish();
    }

    default PropertySpec optStringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, int length) {
        return optStringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation, length).finish();
    }

    default PropertySpec optBooleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return optBooleanSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .finish();
    }

    default PropertySpec booleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return booleanSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .finish();
    }

    default PropertySpec optHexStringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return optHexStringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .finish();
    }

    default PropertySpec hexStringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return hexStringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .finish();
    }

    default PropertySpec hexStringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, HexString defaultValue) {
        return hexStringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .setDefaultValue(defaultValue)
                .finish();
    }

    default PropertySpec booleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, boolean defaultValue) {
        return booleanSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .setDefaultValue(defaultValue)
                .finish();
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

    default PropertySpec dateTimeSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return optDateTimeSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .markRequired()
                .finish();
    }

    default PropertySpec optDateTimeSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return optDateTimeSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .finish();
    }

    default PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .finish();
    }

    default PropertySpec optBigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.optBigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .finish();
    }

    default PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal... possibleValues) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(possibleValues)
                .markExhaustive()
                .finish();
    }

    default PropertySpec bigDecimalSpecWithPossibleValues(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal defaultVal, BigDecimal... possibleValues) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .setDefaultValue(defaultVal)
                .addValues(possibleValues)
                .markExhaustive()
                .finish();
    }

    default PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal defaultVal) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .setDefaultValue(defaultVal)
                .markRequired()
                .finish();
    }

    default PropertySpec messageFileSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .referenceSpec(DeviceMessageFile.class.getName())
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }
}
