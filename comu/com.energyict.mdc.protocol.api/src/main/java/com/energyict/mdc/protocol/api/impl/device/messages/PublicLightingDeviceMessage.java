/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceMessageFile;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

enum PublicLightingDeviceMessage implements DeviceMessageSpecEnum {

    SET_RELAY_OPERATING_MODE(DeviceMessageId.PUBLIC_LIGHTING_SET_RELAY_OPERATING_MODE, "Public lighting set relay operating mode"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PublicLightingDeviceMessageAttributes.relayNumberAttributeName, BigDecimal.ONE, BigDecimals.TWO);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PublicLightingDeviceMessageAttributes.relayOperatingModeAttributeName, BigDecimal.ZERO, BigDecimal.ONE, BigDecimals.TWO, BigDecimals.THREE);
        }
    },
    SET_TIME_SWITCHING_TABLE(DeviceMessageId.PUBLIC_LIGHTING_SET_TIME_SWITCHING_TABLE, " Public lighting set time switching table") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PublicLightingDeviceMessageAttributes.relayNumberAttributeName, BigDecimal.ONE, BigDecimals.TWO);
            propertySpecs.add(
                    propertySpecService
                            .referenceSpec(DeviceMessageFile.class)
                            .named(DeviceMessageAttributes.configUserFileAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SET_THRESHOLD_OVER_CONSUMPTION(DeviceMessageId.PUBLIC_LIGHTING_SET_THRESHOLD_OVER_CONSUMPTION, "Public lighting set threshold over consumption"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PublicLightingDeviceMessageAttributes.threshold, BigDecimal.ZERO);
        }
    },
    SET_OVERALL_MINIMUM_THRESHOLD(DeviceMessageId.PUBLIC_LIGHTING_SET_OVERALL_MINIMUM_THRESHOLD, "Public lighting set overall minimum threshold"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PublicLightingDeviceMessageAttributes.threshold, BigDecimal.ZERO);
        }
    },
    SET_OVERALL_MAXIMUM_THRESHOLD(DeviceMessageId.PUBLIC_LIGHTING_SET_OVERALL_MAXIMUM_THRESHOLD, "Public lighting set overall maximum threshold"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PublicLightingDeviceMessageAttributes.threshold, BigDecimal.ZERO);
        }
    },
    SET_RELAY_TIME_OFFSETS_TABLE(DeviceMessageId.PUBLIC_LIGHTING_SET_RELAY_TIME_OFFSETS_TABLE, "Public lighting set relay time offsets table"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PublicLightingDeviceMessageAttributes.relayNumberAttributeName, BigDecimal.ONE, BigDecimals.TWO);
            this.addStringSpec(propertySpecs, propertySpecService, thesaurus, PublicLightingDeviceMessageAttributes.beginDatesAttributeName);
            this.addStringSpec(propertySpecs, propertySpecService, thesaurus, PublicLightingDeviceMessageAttributes.endDatesAttributeName);
            this.addStringSpec(propertySpecs, propertySpecService, thesaurus, PublicLightingDeviceMessageAttributes.offOffsetsAttributeName);
            this.addStringSpec(propertySpecs, propertySpecService, thesaurus, PublicLightingDeviceMessageAttributes.onOffsetsAttributeName);
        }
    },
    WRITE_GPS_COORDINATES(DeviceMessageId.PUBLIC_LIGHTING_WRITE_GPS_COORDINATES, "Public lighting write gps coordinates"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addStringSpec(propertySpecs, propertySpecService, thesaurus, PublicLightingDeviceMessageAttributes.latitudeAttributeName);
            this.addStringSpec(propertySpecs, propertySpecService, thesaurus, PublicLightingDeviceMessageAttributes.longitudeAttributeName);
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    PublicLightingDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    public String getKey() {
        return PublicLightingDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultTranslation;
    }

    @Override
    public DeviceMessageId getId() {
        return this.id;
    }

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
        return propertySpecs;
    }

    protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        // Default behavior is not to add anything
    };

    protected void addStringSpec(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus, PublicLightingDeviceMessageAttributes name) {
        propertySpecs.add(propertySpecService.stringSpec().named(name).fromThesaurus(thesaurus).markRequired().setDefaultValue("").finish());
    };

    protected void addBigDecimalSpec(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus, PublicLightingDeviceMessageAttributes name, BigDecimal... values) {
        propertySpecs.add(propertySpecService.bigDecimalSpec().named(name).fromThesaurus(thesaurus).markRequired().addValues(values).markExhaustive().finish());
    };

}