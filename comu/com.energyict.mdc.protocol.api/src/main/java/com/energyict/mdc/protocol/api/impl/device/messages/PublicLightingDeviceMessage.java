package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.*;

/**
 * Provides a summary of all <i>public lighting</i> related messages
 *
 * Copyrights EnergyICT
 * Date: 12/8/14
 * Time: 11:21 AM
 */
public enum PublicLightingDeviceMessage  implements DeviceMessageSpecEnum {

    SET_RELAY_OPERATING_MODE(DeviceMessageId.PUBLIC_LIGHTING_SET_RELAY_OPERATING_MODE, "Public lighting set relay operating mode"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(relayNumberAttributeName, true, BigDecimal.ONE, BigDecimals.TWO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(relayOperatingModeAttributeName, true, BigDecimal.ZERO, BigDecimal.ONE, BigDecimals.TWO, BigDecimals.THREE));
        }
    },
    SET_TIME_SWITCHING_TABLE(DeviceMessageId.PUBLIC_LIGHTING_SET_TIME_SWITCHING_TABLE, " Public lighting set time switching table") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(relayNumberAttributeName, true, BigDecimal.ONE, BigDecimals.TWO));
//            propertySpecs.add(propertySpecService.referencePropertySpec(configUserFileAttributeName, true, FactoryIds.USERFILE));
        }
    },
    SET_THRESHOLD_OVER_CONSUMPTION(DeviceMessageId.PUBLIC_LIGHTING_SET_THRESHOLD_OVER_CONSUMPTION, "Public lighting set threshold over consumption"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(threshold, true, BigDecimal.ZERO));
        }
    },
    SET_OVERALL_MINIMUM_THRESHOLD(DeviceMessageId.PUBLIC_LIGHTING_SET_OVERALL_MINIMUM_THRESHOLD, "Public lighting set overall minimum threshold"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(threshold, true, BigDecimal.ZERO));
        }
    },
    SET_OVERALL_MAXIMUM_THRESHOLD(DeviceMessageId.PUBLIC_LIGHTING_SET_OVERALL_MAXIMUM_THRESHOLD, "Public lighting set overall maximum threshold"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(threshold, true, BigDecimal.ZERO));
        }
    },
    SET_RELAY_TIME_OFFSETS_TABLE(DeviceMessageId.PUBLIC_LIGHTING_SET_RELAY_TIME_OFFSETS_TABLE, "Public lighting set relay time offsets table"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(relayNumberAttributeName, true, BigDecimal.ONE, BigDecimals.TWO));
            propertySpecs.add(propertySpecService.stringPropertySpec(beginDatesAttributeName, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(endDatesAttributeName, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(offOffsetsAttributeName, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(onOffsetsAttributeName, true, ""));
        }
    },
    WRITE_GPS_COORDINATES(DeviceMessageId.PUBLIC_LIGHTING_WRITE_GPS_COORDINATES, "Public lighting write gps coordinates"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(latitudeAttributeName, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(longitudeAttributeName, true, ""));
        }
    },

    ;

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

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService);
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

    @Override
    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}

