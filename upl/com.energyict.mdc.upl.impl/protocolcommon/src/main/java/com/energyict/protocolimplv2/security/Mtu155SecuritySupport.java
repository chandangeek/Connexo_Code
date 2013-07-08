package com.energyict.protocolimplv2.security;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecBuilder;
import com.energyict.cpo.TypedProperties;
import com.energyict.dynamicattributes.EncryptedStringFactory;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.security.LegacySecurityPropertyConverter;
import com.energyict.mdc.protocol.security.SecurityProperty;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 10:51
 */
public class Mtu155SecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    private final String authenticationTranslationKeyConstant = "Mtu155SecuritySupport.authenticationlevel.";
    private final String encryptionTranslationKeyConstant = "Mtu155SecuritySupport.encryptionlevel.";

    /**
     * Summarizes the used ID for the Encryption- and AuthenticationLevels.
     */
    protected enum AccessLevelIds {
        KEYT(0),
        KEYC(1),
        KEYF(2);

        private final int accessLevel;

        private AccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        protected int getAccessLevel() {
            return this.accessLevel;
        }

    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                getEncryptionKeyCPropertySpec(),
                getEncryptionKeyFPropertySpec(),
                getEncryptionKeyTPropertySpec());
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.MTU155_SECURITY.toString();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.<AuthenticationDeviceAccessLevel>asList(new SimpleAuthentication());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Arrays.asList(new KeyCEncryption(), new KeyFEncryption(), new KeyTEncryption());
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
            // override the password (as it is provided as a Password object instead of a String
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.PASSWORD.toString(), new Password(""));
            if (Password.class.isAssignableFrom(property.getClass())) {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), ((Password) property).getValue());
            } else {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), property);
            }
            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME, String.valueOf(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()));
            typedProperties.setProperty("KeyC",
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_2.toString(), ""));
            typedProperties.setProperty("KeyT",
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_1.toString(), ""));
            typedProperties.setProperty("KeyF",
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_3.toString(), ""));
        }
        return typedProperties;
    }

    public TypedProperties convertToTypedProperties(List<SecurityProperty> securityProperties) {
        TypedProperties typedProperties = TypedProperties.empty();
        if (!securityProperties.isEmpty()) {

            TypedProperties typedSecurityProperties = TypedProperties.empty();
            for (SecurityProperty property : securityProperties) {
                typedSecurityProperties.setProperty(property.getName(), property.getValue());
            }

            // override the password (as it is provided as a Password object instead of a String
            final Object property = typedSecurityProperties.getProperty(SecurityPropertySpecName.PASSWORD.toString(), new Password(""));
            if (Password.class.isAssignableFrom(property.getClass())) {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), ((Password) property).getValue());
            } else {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), property);
            }

            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME, String.valueOf(securityProperties.get(0).getSecurityPropertySet().getEncryptionDeviceAccessLevel().getId()));
            typedProperties.setProperty("KeyC",
                    typedSecurityProperties.getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_2.toString(), ""));
            typedProperties.setProperty("KeyT",
                    typedSecurityProperties.getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_1.toString(), ""));
            typedProperties.setProperty("KeyF",
                    typedSecurityProperties.getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_3.toString(), ""));

        }
        return typedProperties;
    }


    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        String securityLevelProperty = typedProperties.getStringProperty(SECURITY_LEVEL_PROPERTY_NAME);
        final int encryptionDeviceAccessLevel = securityLevelProperty != null ?
                getSecurityLevelIntegerValue(securityLevelProperty) :
                new KeyCEncryption().getId();

        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, 0, getAuthenticationAccessLevels()));
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, encryptionDeviceAccessLevel, getEncryptionAccessLevels()));


        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return 0;
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

    private Integer getSecurityLevelIntegerValue(String securityLevelProperty) {
        try {
            return Integer.valueOf(securityLevelProperty);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("SecurityLevel property contains a non-numeric value : %s",
                    securityLevelProperty));
        }
    }

    private PropertySpec<String> getEncryptionKeyTPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_1.toString())
                .setDefaultValue("")
                .finish();
    }

    private PropertySpec<String> getEncryptionKeyCPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_2.toString())
                .setDefaultValue("")
                .finish();
    }

    private PropertySpec<String> getEncryptionKeyFPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_3.toString())
                .setDefaultValue("")
                .finish();
    }

    /**
     * A simple authentication level that requires a single password
     */
    protected class SimpleAuthentication implements AuthenticationDeviceAccessLevel {

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
            return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec());
        }
    }

    protected class KeyCEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.KEYC.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getEncryptionKeyCPropertySpec());
        }
    }

    protected class KeyTEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.KEYT.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getEncryptionKeyTPropertySpec());
        }
    }

    protected class KeyFEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.KEYF.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getEncryptionKeyFPropertySpec());
        }
    }
}
