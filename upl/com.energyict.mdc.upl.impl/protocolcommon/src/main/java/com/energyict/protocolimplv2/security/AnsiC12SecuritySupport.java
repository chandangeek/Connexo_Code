package com.energyict.protocolimplv2.security;

import com.energyict.cbo.Password;
import com.energyict.comserver.adapters.common.LegacySecurityPropertyConverter;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for an Ansi C12 protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/01/13
 * Time: 12:02
 */
public class AnsiC12SecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    private final String authenticationTranslationKeyConstant = "AnsiC12SecuritySupport.authenticationlevel.";

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(),
                DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec()
        );
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.ANSI_C12_SECURITY.toString();
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
    public PropertySpec getSecurityPropertySpec(String name) {
        for (PropertySpec securityProperty : getSecurityProperties()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = TypedProperties.empty();
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
            // override the password (as it is provided as a Password object instead of a String
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.PASSWORD.toString(), new Password(""));
            if (Password.class.isAssignableFrom(property.getClass())) {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), ((Password) property).getValue());
            } else {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), property);
            }
            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME, String.valueOf(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()));
        }
        return typedProperties;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        String authenticationDeviceAccessLevelProperty = typedProperties.getStringProperty(SECURITY_LEVEL_PROPERTY_NAME);
        final int authenticationDeviceAccessLevel=authenticationDeviceAccessLevelProperty!=null?
                Integer.valueOf(authenticationDeviceAccessLevelProperty):
                new RestrictedAuthentication().getId();

        final TypedProperties securityRelatedTypedProperties = new TypedProperties();

        if (authenticationDeviceAccessLevelProperty==null) {
            securityRelatedTypedProperties.setProperty(DeviceSecurityProperty.ANSI_C12_USER.name(), "");
            securityRelatedTypedProperties.setProperty(DeviceSecurityProperty.ANSI_C12_USER_ID.name(), 0);
        } else {
            securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, authenticationDeviceAccessLevel, getAuthenticationAccessLevels()));
        }


        return new DeviceProtocolSecurityPropertySet() {
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                    DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(),
                    DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec()
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                    DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(),
                    DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec()
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                    DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(),
                    DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec()
            );
        }
    }
}
