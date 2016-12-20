package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.beginDatesAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.beginDatesAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.configUserFileAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.configUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.endDatesAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.endDatesAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.latitudeAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.latitudeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.longitudeAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.longitudeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.offOffsetsAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.offOffsetsAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.onOffsetsAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.onOffsetsAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.relayNumberAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.relayNumberAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.relayOperatingModeAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.relayOperatingModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.threshold;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.thresholdDefaultTranslation;

/**
 * Provides a summary of all <i>Public Lighting</i> related messages.
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:00
 */
public enum PublicLightingDeviceMessage implements DeviceMessageSpecSupplier {

    SET_RELAY_OPERATING_MODE(33001, "Set relay operating mode") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, relayNumberAttributeName, relayNumberAttributeDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.bigDecimalSpec(service, relayOperatingModeAttributeName, relayOperatingModeAttributeDefaultTranslation, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3))
            );
        }
    },
    SET_TIME_SWITCHING_TABLE(33002, "Write the time switching table") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, relayNumberAttributeName, relayNumberAttributeDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.deviceMessageFileSpec(service, configUserFileAttributeName, configUserFileAttributeDefaultTranslation)
            );
        }
    },
    SET_THRESHOLD_OVER_CONSUMPTION(33003, "Set the threshold for over consumption") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, threshold, thresholdDefaultTranslation));
        }
    },
    SET_OVERALL_MINIMUM_THRESHOLD(33004, "Set overall minimum threshold") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, threshold, thresholdDefaultTranslation));
        }
    },
    SET_OVERALL_MAXIMUM_THRESHOLD(33005, "Set overall maximum threshold") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, threshold, thresholdDefaultTranslation));
        }
    },
    SET_RELAY_TIME_OFFSETS_TABLE(33006, "Write relay offsets table") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, relayNumberAttributeName, relayNumberAttributeDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.stringSpec(service, beginDatesAttributeName, beginDatesAttributeDefaultTranslation),
                    this.stringSpec(service, endDatesAttributeName, endDatesAttributeDefaultTranslation),
                    this.stringSpec(service, offOffsetsAttributeName, offOffsetsAttributeDefaultTranslation),
                    this.stringSpec(service, onOffsetsAttributeName, onOffsetsAttributeDefaultTranslation)
            );
        }
    },
    WRITE_GPS_COORDINATES(33007, "Write the GPS coordinates") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, latitudeAttributeName, latitudeAttributeDefaultTranslation),
                    this.stringSpec(service, longitudeAttributeName, longitudeAttributeDefaultTranslation)
            );
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    PublicLightingDeviceMessage(long id, String defaultNameTranslation) {
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

    private PropertySpecBuilder<BigDecimal> bigDecimalPropertySpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.bigDecimalPropertySpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal... possibleValues) {
        return this.bigDecimalPropertySpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(possibleValues)
                .markExhaustive()
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
        return PublicLightingDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.PUBLIC_LIGHTING,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}