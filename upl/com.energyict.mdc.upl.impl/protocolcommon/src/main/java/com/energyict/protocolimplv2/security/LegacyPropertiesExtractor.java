package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.DeviceAccessLevel;

import com.energyict.mdc.upl.TypedProperties;

import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @since 4/2/13 12:46 PM
 */
public class LegacyPropertiesExtractor {

    public static TypedProperties getSecurityRelatedProperties(com.energyict.mdc.upl.properties.TypedProperties oldTypedProperties, int currentDeviceAccessLevel, List<? extends DeviceAccessLevel> deviceAccessLevels) {
        TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
        deviceAccessLevels
                .stream()
                .filter(level -> level.getId() == currentDeviceAccessLevel)
                .flatMap(level -> level.getSecurityProperties().stream())
                .filter(propertySpec -> oldTypedProperties.hasValueFor(propertySpec.getName()))
                .map(PropertySpec::getName)
                .forEach(propertySpecName -> securityRelatedTypedProperties.setProperty(propertySpecName, oldTypedProperties.getProperty(propertySpecName)));
        return securityRelatedTypedProperties;
    }

}