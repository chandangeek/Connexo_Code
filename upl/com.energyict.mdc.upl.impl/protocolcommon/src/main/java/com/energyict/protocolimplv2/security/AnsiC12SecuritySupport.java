package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.LegacyDeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides general security <b>capabilities</b> for an Ansi C12 protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/01/13
 * Time: 12:02
 */
public class AnsiC12SecuritySupport extends AbstractSecuritySupport implements LegacyDeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    protected static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    private final String authenticationTranslationKeyConstant = "AnsiC12SecuritySupport.authenticationlevel.";

    public AnsiC12SecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(this.propertySpecService),
                DeviceSecurityProperty.BINARY_PASSWORD.getPropertySpec(this.propertySpecService),
                DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(this.propertySpecService),
                DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec(this.propertySpecService)
        );
    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return Optional.empty();
    }

    @Override
    public List<String> getLegacySecurityProperties() {
        return Collections.singletonList(SECURITY_LEVEL_PROPERTY_NAME);
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.asList(
                new UnRestrictedAuthentication(),
                new RestrictedAuthentication(),
                new ReadOnlyAuthentication()
        );
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
            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME, String.valueOf(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()));
        }
        return typedProperties;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        return this.convertFromTypedProperties(TypedProperties.copyOf(typedProperties));
    }

    protected DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        String authenticationDeviceAccessLevelProperty = typedProperties.getStringProperty(SECURITY_LEVEL_PROPERTY_NAME);
        final int authenticationDeviceAccessLevel;
        if (authenticationDeviceAccessLevelProperty != null) {
            authenticationDeviceAccessLevel = Integer.valueOf(authenticationDeviceAccessLevelProperty);
        } else {
            authenticationDeviceAccessLevel = new RestrictedAuthentication().getId();
        }

        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();

        if (authenticationDeviceAccessLevelProperty == null) {
            securityRelatedTypedProperties.setProperty(SecurityPropertySpecTranslationKeys.BINARY_PASSWORD.toString(), 0);
            securityRelatedTypedProperties.setProperty(SecurityPropertySpecTranslationKeys.ANSI_C12_USER.toString(), "");
            securityRelatedTypedProperties.setProperty(SecurityPropertySpecTranslationKeys.ANSI_C12_USER_ID.toString(), 0);
        } else {
            securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, authenticationDeviceAccessLevel, getAuthenticationAccessLevels()));
        }


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
                return authenticationDeviceAccessLevel;
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
     * UnRestricted ANSI C12 authentication level
     */
    protected class UnRestrictedAuthentication implements AuthenticationDeviceAccessLevel {

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
            return "Unrestricted authentication";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec(propertySpecService)
            );
        }
    }

    /**
     * Restricted ANSI C12 authentication level
     */
    protected class RestrictedAuthentication implements AuthenticationDeviceAccessLevel {

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
            return "Restricted authentication";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.BINARY_PASSWORD.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec(propertySpecService)
            );
        }
    }

    /**
     * Read Only ANSI C12 authentication level
     */
    protected class ReadOnlyAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return 2;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "Read only authentication";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.BINARY_PASSWORD.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec(propertySpecService)
            );
        }
    }

}