package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;

/**
 * Copyrights EnergyICT
 *
 * @since 4/2/13 12:46 PM
 */
public class LegacyPropertiesExtractor {
    static public TypedProperties getSecurityRelatedPropertiesForAuthentication(TypedProperties typedProperties, int currentAuthenticationDeviceAccessLevel, DeviceProtocolSecurityCapabilities deviceProtocolSecurityCapabilities) {
        TypedProperties securityRelatedTypedProperties = new TypedProperties();
        for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : deviceProtocolSecurityCapabilities.getAuthenticationAccessLevels()) {
            if (authenticationDeviceAccessLevel.getId()==currentAuthenticationDeviceAccessLevel) {
                for (PropertySpec propertySpec : authenticationDeviceAccessLevel.getSecurityProperties()) {
                    if (typedProperties.hasValueFor(propertySpec.getName())) {
                        securityRelatedTypedProperties.setProperty(propertySpec.getName(), typedProperties.getProperty(propertySpec.getName()));
                    }
                }
            }
        }
        return securityRelatedTypedProperties;
    }

    static public TypedProperties getSecurityRelatedPropertiesForEncryption(TypedProperties typedProperties, int currentEncryptionDeviceAccessLevel, DeviceProtocolSecurityCapabilities deviceProtocolSecurityCapabilities) {
        TypedProperties securityRelatedTypedProperties = new TypedProperties();
        for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : deviceProtocolSecurityCapabilities.getEncryptionAccessLevels()) {
            if (encryptionDeviceAccessLevel.getId()==currentEncryptionDeviceAccessLevel) {
                for (PropertySpec propertySpec : encryptionDeviceAccessLevel.getSecurityProperties()) {
                    if (typedProperties.hasValueFor(propertySpec.getName())) {
                        securityRelatedTypedProperties.setProperty(propertySpec.getName(), typedProperties.getProperty(propertySpec.getName()));
                    }
                }
            }
        }
        return securityRelatedTypedProperties;
    }



}
