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
    static public void getSecurityRelatedPropertiesForAuthentication(TypedProperties securityRelatedTypedProperties, TypedProperties typedProperties, int currentAuthenticationDeviceAccessLevel, DeviceProtocolSecurityCapabilities deviceProtocolSecurityCapabilities) {
        for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : deviceProtocolSecurityCapabilities.getAuthenticationAccessLevels()) {
            if (authenticationDeviceAccessLevel.getId()==currentAuthenticationDeviceAccessLevel) {
                for (PropertySpec propertySpec : authenticationDeviceAccessLevel.getSecurityProperties()) {
                    if (typedProperties.hasValueFor(propertySpec.getName())) {
                        securityRelatedTypedProperties.setProperty(propertySpec.getName(), typedProperties.getProperty(propertySpec.getName()));
                    }
                }
            }
        }
    }

    static public void getSecurityRelatedPropertiesForEncryption(TypedProperties securityRelatedTypedProperties, TypedProperties typedProperties, int currentEncryptionDeviceAccessLevel, DeviceProtocolSecurityCapabilities deviceProtocolSecurityCapabilities) {
        for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : deviceProtocolSecurityCapabilities.getEncryptionAccessLevels()) {
            if (encryptionDeviceAccessLevel.getId()==currentEncryptionDeviceAccessLevel) {
                for (PropertySpec propertySpec : encryptionDeviceAccessLevel.getSecurityProperties()) {
                    if (typedProperties.hasValueFor(propertySpec.getName())) {
                        securityRelatedTypedProperties.setProperty(propertySpec.getName(), typedProperties.getProperty(propertySpec.getName()));
                    }
                }
            }
        }
    }



}
