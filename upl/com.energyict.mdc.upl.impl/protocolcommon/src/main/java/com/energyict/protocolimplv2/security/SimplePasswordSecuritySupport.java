package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides general security <b>capabilities</b> for DeviceProtocols
 * that use a single password to do authentication/encryption
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/01/13
 * Time: 14:47
 */
public class SimplePasswordSecuritySupport extends AbstractSecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final int AUTH_DEVICE_ACCESS_LEVEL = 0;

    public SimplePasswordSecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return Optional.empty();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.singletonList(new SimpleAuthentication());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.emptyList();
    }

    @Override
    public Optional<PropertySpec> getSecurityPropertySpec(String name) {
        if (DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService).getName().equals(name)) {
            return Optional.of(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
        } else {
            return null;
        }
    }

    @Override
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = TypedProperties.empty();
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
        }
        return typedProperties;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, AUTH_DEVICE_ACCESS_LEVEL, getAuthenticationAccessLevels()));
        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public String getName() {
                return "security";
            }

            @Override
            public Object getClient() {
                return null;
            }

            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return AUTH_DEVICE_ACCESS_LEVEL;
            }

            @Override
            public int getEncryptionDeviceAccessLevel() {
                return DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
            }

            @Override
            public TypedProperties getSecurityProperties() {
                return securityRelatedTypedProperties;
            }
        };
    }

    /**
     * A simple authentication level that requires a single password
     */
    class SimpleAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AUTH_DEVICE_ACCESS_LEVEL;
        }

        @Override
        public String getTranslationKey() {
            return "SimplePasswordSecuritySupport.authenticationlevel." + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "Password authentication";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
        }
    }

}