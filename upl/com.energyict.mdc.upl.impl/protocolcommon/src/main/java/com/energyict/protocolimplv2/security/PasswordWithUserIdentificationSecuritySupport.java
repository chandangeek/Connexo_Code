package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides general security <b>capabilities</b> for DeviceProtocols
 * that use a single password and a UserIdentification to do authentication/encryption.<br/>
 * Be aware that the UserIdentification is validated as a string, but can also just
 * contain a numerical value.
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/01/13
 * Time: 9:28
 */
public class PasswordWithUserIdentificationSecuritySupport extends AbstractSecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    protected static final int STANDARD_AUTH_DEVICE_ACCESS_LEVEL = 10;
    protected static final int STANDARD_ENCRYPTION_DEVICE_ACCESS_LEVEL = 20;
    protected static final String LEGACY_DEVICE_ACCESS_IDENTIFIER_PROPERTY = "UserId";
    private static final String authenticationTranslationKeyConstant = "PasswordWithUserIdentificationSecuritySupport.accesslevel.";
    private static final String encryptionTranslationKeyConstant = "PasswordWithUserIdentificationSecuritySupport.accesslevel.";

    public PasswordWithUserIdentificationSecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return Optional.of(DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService));
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
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
    public com.energyict.mdc.upl.properties.TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = TypedProperties.empty();
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
            String deviceAccessIdentifierPropertyName = DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService).getName();
            typedProperties.setProperty(deviceAccessIdentifierPropertyName, deviceProtocolSecurityPropertySet.getClient()); // Add the client
            if (typedProperties.hasValueFor(deviceAccessIdentifierPropertyName)) {
                typedProperties.setProperty(
                        LEGACY_DEVICE_ACCESS_IDENTIFIER_PROPERTY,
                        typedProperties.getProperty(deviceAccessIdentifierPropertyName)
                );
            }
        }
        return typedProperties;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        return this.convertFromTypedProperties(TypedProperties.copyOf(typedProperties));
    }

    private DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
        Object deviceAccessIdentifier = overrideDeviceAccessIdentifierPropertyIfAbsent(typedProperties);

        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, STANDARD_AUTH_DEVICE_ACCESS_LEVEL, getAuthenticationAccessLevels()));
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, STANDARD_ENCRYPTION_DEVICE_ACCESS_LEVEL, getEncryptionAccessLevels()));
        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public String getName() {
                return "security";
            }

            @Override
            public Object getClient() {
                return String.valueOf(deviceAccessIdentifier);
            }

            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return STANDARD_AUTH_DEVICE_ACCESS_LEVEL;
            }

            @Override
            public int getEncryptionDeviceAccessLevel() {
                return STANDARD_ENCRYPTION_DEVICE_ACCESS_LEVEL;
            }

            @Override
            public TypedProperties getSecurityProperties() {
                return securityRelatedTypedProperties;
            }
        };
    }

    private Object overrideDeviceAccessIdentifierPropertyIfAbsent(TypedProperties typedProperties) {
        Object deviceAccessIdentifier = typedProperties.getProperty(DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService).getName());
        if (deviceAccessIdentifier == null && typedProperties.hasValueFor(LEGACY_DEVICE_ACCESS_IDENTIFIER_PROPERTY)) {
            deviceAccessIdentifier = typedProperties.getProperty(LEGACY_DEVICE_ACCESS_IDENTIFIER_PROPERTY);
        } else if (deviceAccessIdentifier == null && typedProperties.hasValueFor(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName())) {
            deviceAccessIdentifier = typedProperties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName());
        }
        if (deviceAccessIdentifier != null) {
            typedProperties.setProperty(DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService).getName(), deviceAccessIdentifier);
        }
        return deviceAccessIdentifier;
    }

    /**
     * Standard authentication level that requires a password and an access identifier
     */
    protected class StandardAuthenticationAccessLevel implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return STANDARD_AUTH_DEVICE_ACCESS_LEVEL;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "Standard authentication";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
        }
    }

    /**
     * Standard encryption level that requires a password and an encryption identifier
     */
    protected class StandardEncryptionAccessLevel implements EncryptionDeviceAccessLevel {


        @Override
        public int getId() {
            return STANDARD_ENCRYPTION_DEVICE_ACCESS_LEVEL;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "Standard encryption";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
        }
    }

}