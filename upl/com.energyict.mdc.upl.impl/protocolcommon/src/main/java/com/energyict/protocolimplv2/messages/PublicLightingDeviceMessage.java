package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;

import com.energyict.protocolimplv2.messages.nls.Thesaurus;
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
public enum PublicLightingDeviceMessage implements DeviceMessageSpec {

    SET_RELAY_OPERATING_MODE(0, "Set relay operating mode") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.bigDecimalSpec(relayNumberAttributeName, relayNumberAttributeDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.bigDecimalSpec(relayOperatingModeAttributeName, relayOperatingModeAttributeDefaultTranslation, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3))
            );
        }
    },
    SET_TIME_SWITCHING_TABLE(1, "Write the time switching table") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.bigDecimalSpec(relayNumberAttributeName, relayNumberAttributeDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.deviceMessageFileSpec(configUserFileAttributeName, configUserFileAttributeDefaultTranslation)
            );
        }
    },
    SET_THRESHOLD_OVER_CONSUMPTION(2, "Set the threshold for over consumption") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.bigDecimalSpec(threshold, thresholdDefaultTranslation));
        }
    },
    SET_OVERALL_MINIMUM_THRESHOLD(3, "Set overall minimum threshold") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.bigDecimalSpec(threshold, thresholdDefaultTranslation));
        }
    },
    SET_OVERALL_MAXIMUM_THRESHOLD(4, "Set overall maximum threshold") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.bigDecimalSpec(threshold, thresholdDefaultTranslation));
        }
    },
    SET_RELAY_TIME_OFFSETS_TABLE(5, "Write relay offsets table") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.bigDecimalSpec(relayNumberAttributeName, relayNumberAttributeDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.stringSpec(beginDatesAttributeName, beginDatesAttributeDefaultTranslation),
                    this.stringSpec(endDatesAttributeName, endDatesAttributeDefaultTranslation),
                    this.stringSpec(offOffsetsAttributeName, offOffsetsAttributeDefaultTranslation),
                    this.stringSpec(onOffsetsAttributeName, onOffsetsAttributeDefaultTranslation)
            );
        }
    },
    WRITE_GPS_COORDINATES(6, "Write the GPS coordinates") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.stringSpec(latitudeAttributeName, latitudeAttributeDefaultTranslation),
                    this.stringSpec(longitudeAttributeName, longitudeAttributeDefaultTranslation)
            );
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    PublicLightingDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected PropertySpec stringSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    private PropertySpecBuilder<BigDecimal> bigDecimalPropertySpecBuilder(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    protected PropertySpec bigDecimalSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.bigDecimalPropertySpecBuilder(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec bigDecimalSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal... possibleValues) {
        return this.bigDecimalPropertySpecBuilder(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(possibleValues)
                .markExhaustive()
                .finish();
    }

    protected PropertySpec deviceMessageFileSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .referenceSpec(DeviceMessageFile.class)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return DeviceMessageCategories.PUBLIC_LIGHTING;
    }

    @Override
    public String getName() {
        return Services
                .nlsService()
                .getThesaurus(Thesaurus.ID.toString())
                .getFormat(this.getNameTranslationKey())
                .format();
    }

    private String getNameResourceKey() {
        return PublicLightingDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public TranslationKeyImpl getNameTranslationKey() {
        return new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }

    @Override
    public long getMessageId() {
        return id;
    }

}