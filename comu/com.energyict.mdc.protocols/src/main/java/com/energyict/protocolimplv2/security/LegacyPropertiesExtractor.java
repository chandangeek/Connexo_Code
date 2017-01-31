/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.security;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;

import java.util.List;

public class LegacyPropertiesExtractor {

    public static TypedProperties getSecurityRelatedProperties(TypedProperties typedProperties, int currentDeviceAccessLevel, List<? extends DeviceAccessLevel> deviceAccessLevels) {
        TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
        for (DeviceAccessLevel deviceAccessLevel : deviceAccessLevels) {
            if (deviceAccessLevel.getId()==currentDeviceAccessLevel) {
                for (PropertySpec propertySpec : deviceAccessLevel.getSecurityProperties()) {
                    if (typedProperties.hasValueFor(propertySpec.getName())) {
                        securityRelatedTypedProperties.setProperty(propertySpec.getName(), typedProperties.getProperty(propertySpec.getName()));
                    }
                }
            }
        }
        return securityRelatedTypedProperties;
    }

}