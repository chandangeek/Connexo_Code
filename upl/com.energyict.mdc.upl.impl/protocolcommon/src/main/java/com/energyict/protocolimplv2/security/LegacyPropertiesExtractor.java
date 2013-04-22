package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.security.DeviceAccessLevel;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @since 4/2/13 12:46 PM
 */
public class LegacyPropertiesExtractor {

    static public TypedProperties getSecurityRelatedProperties(TypedProperties typedProperties, int currentEncryptionDeviceAccessLevel, List<? extends DeviceAccessLevel> deviceAccessLevels) {
        TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
        for (DeviceAccessLevel deviceAccessLevel : deviceAccessLevels) {
            if (deviceAccessLevel.getId()==currentEncryptionDeviceAccessLevel) {
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
