package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;
import com.energyict.protocolimpl.properties.TypedProperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for DeviceProtocols
 * that use a single password and a UserIdentification to do authentication/encryption.<br/>
 * Be aware that the UserIdentification is validated as a string, but can also just
 * contain a numerical value.
 * <p>
 * Copyrights EnergyICT
 * Date: 14/01/13
 * Time: 9:28
 */
public class PasswordWithUserIdentificationSecuritySupport extends AbstractSecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final int STANDARD_AUTH_DEVICE_ACCESS_LEVEL = 10;
    private static final int STANDARD_ENCRYPTION_DEVICE_ACCESS_LEVEL = 20;

    public PasswordWithUserIdentificationSecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService),
                DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
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
            // override the password (as it is provided as a Password object instead of a String
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.PASSWORD.toString(), new EmptyPassword());
            if (Password.class.isAssignableFrom(property.getClass())) {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), ((Password) property).getValue());
            } else {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), property);
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
        overrideDeviceAccessIdentifierPropertyIfAbsent(typedProperties);

        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, STANDARD_AUTH_DEVICE_ACCESS_LEVEL, getAuthenticationAccessLevels()));
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, STANDARD_ENCRYPTION_DEVICE_ACCESS_LEVEL, getEncryptionAccessLevels()));
        return new DeviceProtocolSecurityPropertySet() {
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

    private void overrideDeviceAccessIdentifierPropertyIfAbsent(TypedProperties typedProperties) {
        Object deviceAccessIdentifier = typedProperties.getProperty(DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService).getName());
        if (deviceAccessIdentifier == null) {
            deviceAccessIdentifier = typedProperties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName());
        }
        if (deviceAccessIdentifier != null) {
            typedProperties.setProperty(DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService).getName(), deviceAccessIdentifier);
        }
    }

    private static class EmptyPassword implements Password {
        @Override
        public String getValue() {
            return "";
        }
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
            return "PasswordWithUserIdentificationSecuritySupport.accesslevel.10";
        }

        @Override
        public String getDefaultTranslation() {
            return "Standard authentication";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
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
            return "PasswordWithUserIdentificationSecuritySupport.accesslevel.20";
        }

        @Override
        public String getDefaultTranslation() {
            return "Standard encryption";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
        }
    }

}