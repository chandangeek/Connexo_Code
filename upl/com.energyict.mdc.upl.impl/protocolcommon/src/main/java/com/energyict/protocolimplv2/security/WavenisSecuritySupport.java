package com.energyict.protocolimplv2.security;

import com.energyict.cbo.Password;
import com.energyict.comserver.adapters.common.LegacySecurityPropertyConverter;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import java.util.Arrays;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for Wavenis (DLMS) protocols
 * that use a single password for authentication and an encryptionKey for data encryption
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/01/13
 * Time: 16:13
 */
public class WavenisSecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    public static final String ENCRYPTION_KEY_PROPERTY_NAME = "WavenisEncryptionKey";
    private final String authenticationTranslationKeyConstant = "WavenisSecuritySupport.authenticationlevel.";
    private final String encryptionTranslationKeyConstant = "WavenisSecuritySupport.encryptionlevel.";

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.WAVENIS_SECURITY.toString();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.<AuthenticationDeviceAccessLevel>asList(new StandardAuthenticationAccessLevel());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Arrays.<EncryptionDeviceAccessLevel>asList(new StandardEncryptionAccessLevel());
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
        TypedProperties typedProperties = new TypedProperties();
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME, String.valueOf(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()));
            typedProperties.setProperty(ENCRYPTION_KEY_PROPERTY_NAME,
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ENCRYPTION_KEY.toString(), ""));
            // override the password (as it is provided as a Password object instead of a String
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.PASSWORD.toString(), new Password(""));
            if (Password.class.isAssignableFrom(property.getClass())) {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), ((Password) property).getValue());
            } else {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), property);
            }
        }
        return typedProperties;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        String authenticationDeviceAccessLevelProperty = typedProperties.getTypedProperty(SECURITY_LEVEL_PROPERTY_NAME);
        final int authenticationDeviceAccessLevel=authenticationDeviceAccessLevelProperty!=null?
                Integer.valueOf(authenticationDeviceAccessLevelProperty):
                new StandardAuthenticationAccessLevel().getId();

        String encryptionKeyProperty = typedProperties.getStringProperty(ENCRYPTION_KEY_PROPERTY_NAME);
        final int encryptionDeviceAccessLevel = encryptionKeyProperty!=null?
                Integer.valueOf(encryptionKeyProperty):
                new StandardEncryptionAccessLevel().getId();

        final TypedProperties securityRelatedTypedProperties = new TypedProperties();
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedPropertiesForAuthentication(typedProperties, authenticationDeviceAccessLevel, this));
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedPropertiesForEncryption(typedProperties, encryptionDeviceAccessLevel, this));

        return new DeviceProtocolSecurityPropertySet() {
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                    DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                    DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
        }
    }
}
