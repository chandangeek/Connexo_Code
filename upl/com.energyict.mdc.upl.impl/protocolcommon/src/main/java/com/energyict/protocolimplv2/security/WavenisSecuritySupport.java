package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.LegacyDeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import com.energyict.protocolimpl.properties.TypedProperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides general security <b>capabilities</b> for Wavenis protocols
 * that use a single password for authentication and an encryptionKey for data encryption
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/01/13
 * Time: 16:13
 */
public class WavenisSecuritySupport extends AbstractSecuritySupport implements LegacyDeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    public static final String ENCRYPTION_KEY_PROPERTY_NAME = "WavenisEncryptionKey";
    private static final String authenticationTranslationKeyConstant = "WavenisSecuritySupport.authenticationlevel.";
    private static final String encryptionTranslationKeyConstant = "WavenisSecuritySupport.encryptionlevel.";

    public WavenisSecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService),
                DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return Optional.empty();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.singletonList(new StandardAuthenticationAccessLevel());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.singletonList(new StandardEncryptionAccessLevel());
    }

    @Override
    public List<String> getLegacySecurityProperties() {
        return Arrays.asList(
                SECURITY_LEVEL_PROPERTY_NAME,
                ENCRYPTION_KEY_PROPERTY_NAME);
    }

    @Override
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = TypedProperties.empty();
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME, String.valueOf(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()));
            typedProperties.setProperty(ENCRYPTION_KEY_PROPERTY_NAME,
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ENCRYPTION_KEY.toString(), ""));
        }
        return typedProperties;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        return this.convertFromTypedProperties((TypedProperties) typedProperties);
    }

    private DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        String authenticationDeviceAccessLevelProperty = typedProperties.getTypedProperty(SECURITY_LEVEL_PROPERTY_NAME);
        final int authenticationDeviceAccessLevel = authenticationDeviceAccessLevelProperty != null ?
                Integer.valueOf(authenticationDeviceAccessLevelProperty) :
                new StandardAuthenticationAccessLevel().getId();

        String encryptionKeyProperty = typedProperties.getStringProperty(ENCRYPTION_KEY_PROPERTY_NAME);
        final int encryptionDeviceAccessLevel = encryptionKeyProperty != null ?
                Integer.valueOf(encryptionKeyProperty) :
                new StandardEncryptionAccessLevel().getId();

        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
        if (authenticationDeviceAccessLevelProperty != null) {
            securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, authenticationDeviceAccessLevel, getAuthenticationAccessLevels()));
        } else {
            securityRelatedTypedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), "");
            securityRelatedTypedProperties.setProperty(SecurityPropertySpecName.ENCRYPTION_KEY.toString(), "");
        }
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, encryptionDeviceAccessLevel, getEncryptionAccessLevels()));

        return new DeviceProtocolSecurityPropertySet() {

            @Override
            public String getClient() {
                return null;
            }

            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return authenticationDeviceAccessLevel;
            }

            @Override
            public int getEncryptionDeviceAccessLevel() {
                return encryptionDeviceAccessLevel;
            }

            @Override
            public TypedProperties getSecurityProperties() {
                return securityRelatedTypedProperties;
            }
        };
    }

    /**
     * Standard authentication level that requires a password and an access identifier
     */
    protected class StandardAuthenticationAccessLevel implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "Standard Wavenis authentication";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
        }
    }

    /**
     * Standard encryption level that requires a password and an encryption identifier
     */
    protected class StandardEncryptionAccessLevel implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "Standard Wavenis encryption";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
        }
    }
}
