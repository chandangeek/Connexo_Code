package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import com.energyict.protocolimpl.properties.TypedProperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides general security <b>capabilities</b> for device that have either:
 * <ul>
 * <li>No password</li>
 * <li>A password</li>
 * </ul>
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/01/13
 * Time: 14:41
 */
public class NoOrPasswordSecuritySupport extends AbstractSecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String authenticationTranslationKeyConstant = "NoOrPasswordSecuritySupport.authenticationlevel.";

    public NoOrPasswordSecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
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
        return Arrays.asList(new NoAuthenticationAccessLevel(), new StandardAuthenticationAccessLevel());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.emptyList();
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = TypedProperties.empty();
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
        }
        return typedProperties;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        return this.convertFromTypedProperties(TypedProperties.copyOf(typedProperties));
    }

    private DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        String passwordProperty = typedProperties.getStringProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService).getName());
        final AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel;
        if (passwordProperty == null) {
            authenticationDeviceAccessLevel = new NoAuthenticationAccessLevel();
        } else {
            authenticationDeviceAccessLevel = new StandardAuthenticationAccessLevel();
        }

        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, authenticationDeviceAccessLevel.getId(), getAuthenticationAccessLevels()));

        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public String getClient() {
                return null;
            }

            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return authenticationDeviceAccessLevel.getId();
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
     * No authentication level that requires nothing
     */
    protected class NoAuthenticationAccessLevel implements AuthenticationDeviceAccessLevel {

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
            return "No authentication";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }
    }

    /**
     * Standard authentication level that requires a password
     */
    protected class StandardAuthenticationAccessLevel implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return 1;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
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