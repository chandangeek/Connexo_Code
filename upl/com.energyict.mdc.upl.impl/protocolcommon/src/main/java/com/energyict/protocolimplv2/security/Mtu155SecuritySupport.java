package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.LegacyDeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;
import com.energyict.mdc.upl.security.SecurityProperty;

import com.energyict.protocolimpl.properties.TypedProperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 10:51
 */
public class Mtu155SecuritySupport implements LegacyDeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    private static final String KEY_T_LEGACY_PROPERTY = "KeyT";
    private static final String KEY_C_LEGACY_PROPERTY = "KeyC";
    private static final String KEY_F_LEGACY_PROPERTY = "KeyF";
    private static final String AUTHENTICATION_TRANSLATION_KEY_CONSTANT = "Mtu155SecuritySupport.authenticationlevel.";
    private static final String ENCRYPTION_TRANSLATION_KEY_CONSTANT = "Mtu155SecuritySupport.encryptionlevel.";

    private final PropertySpecService propertySpecService;

    public Mtu155SecuritySupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService),
                getEncryptionKeyCPropertySpec(),
                getEncryptionKeyFPropertySpec(),
                getEncryptionKeyTPropertySpec());
    }

    @Override
    public List<String> getLegacySecurityProperties() {
        return Arrays.asList(
                KEY_T_LEGACY_PROPERTY,
                KEY_C_LEGACY_PROPERTY,
                KEY_F_LEGACY_PROPERTY,
                SECURITY_LEVEL_PROPERTY_NAME);
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.<AuthenticationDeviceAccessLevel>singletonList(new SimpleAuthentication());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Arrays.asList(new KeyCEncryption(), new KeyFEncryption(), new KeyTEncryption());
    }

    @Override
    public Optional<com.energyict.mdc.upl.properties.PropertySpec> getSecurityPropertySpec(String name) {
        for (PropertySpec securityProperty : getSecurityProperties()) {
            if (securityProperty.getName().equals(name)) {
                return Optional.of(securityProperty);
            }
        }
        return Optional.empty();
    }

    @Override
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = TypedProperties.empty();
        if (deviceProtocolSecurityPropertySet != null) {
            // override the password (as it is provided as a Password object instead of a String
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.PASSWORD.toString(), null);
            if (property == null) {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), "");
            } else if (Password.class.isAssignableFrom(property.getClass())) {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), ((Password) property).getValue());
            } else {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), property);
            }
            typedProperties.setProperty(KEY_T_LEGACY_PROPERTY,
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_1.toString(), ""));
            typedProperties.setProperty(KEY_C_LEGACY_PROPERTY,
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_2.toString(), ""));
            typedProperties.setProperty(KEY_F_LEGACY_PROPERTY,
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_3.toString(), ""));
            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME, String.valueOf(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()));
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
            final Object property = typedSecurityProperties.getProperty(SecurityPropertySpecName.PASSWORD.toString(), null);
            if (property == null) {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), "");
            } else if (Password.class.isAssignableFrom(property.getClass())) {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), ((Password) property).getValue());
            } else {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), property);
            }

            String keyTValue = (String) typedSecurityProperties.getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_1.toString(), "");
            String keyCValue = (String) typedSecurityProperties.getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_2.toString(), "");
            String keyFValue = (String) typedSecurityProperties.getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_3.toString(), "");
            typedProperties.setProperty(KEY_T_LEGACY_PROPERTY, keyTValue);
            typedProperties.setProperty(KEY_C_LEGACY_PROPERTY, keyCValue);
            typedProperties.setProperty(KEY_F_LEGACY_PROPERTY, keyFValue);

            int securityLevel;
            if (!keyTValue.isEmpty()) {
                securityLevel = AccessLevelIds.KEYT.getAccessLevel();
            } else if (!keyCValue.isEmpty()) {
                securityLevel = AccessLevelIds.KEYC.getAccessLevel();
            } else {
                securityLevel = AccessLevelIds.KEYF.getAccessLevel();
            }
            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME, Integer.toString(securityLevel));
        }
        return typedProperties;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        return this.convertFromTypedProperties(TypedProperties.copyOf(typedProperties));
    }

    private DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
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

    private PropertySpec getEncryptionKeyTPropertySpec() {
        return this.encryptionkeyPropertySpec(SecurityPropertySpecName.ENCRYPTION_KEY_1);
    }

    private PropertySpec getEncryptionKeyCPropertySpec() {
        return this.encryptionkeyPropertySpec(SecurityPropertySpecName.ENCRYPTION_KEY_2);
    }

    private PropertySpec getEncryptionKeyFPropertySpec() {
        return this.encryptionkeyPropertySpec(SecurityPropertySpecName.ENCRYPTION_KEY_3);
    }

    private PropertySpec encryptionkeyPropertySpec(SecurityPropertySpecName name) {
        return Services
                .propertySpecService()
                .encryptedStringSpec()
                .named(name.toString(), name.toString())
                .describedAs("Description for " + name.toString())
                .setDefaultValue("")
                .finish();
    }

    /**
     * Summarizes the used ID for the Encryption- and AuthenticationLevels.
     */
    protected enum AccessLevelIds {
        KEYT(0),
        KEYC(1),
        KEYF(2);

        private final int accessLevel;

        AccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        int getAccessLevel() {
            return this.accessLevel;
        }
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
            return AUTHENTICATION_TRANSLATION_KEY_CONSTANT + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "Default authentication";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
        }
    }

    protected class KeyCEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.KEYC.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return ENCRYPTION_TRANSLATION_KEY_CONSTANT + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "KeyC encryption";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getEncryptionKeyCPropertySpec());
        }
    }

    protected class KeyTEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.KEYT.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return ENCRYPTION_TRANSLATION_KEY_CONSTANT + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "KeyT encryption";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getEncryptionKeyTPropertySpec());
        }
    }

    protected class KeyFEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.KEYF.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return ENCRYPTION_TRANSLATION_KEY_CONSTANT + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "KeyF encryption";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getEncryptionKeyFPropertySpec());
        }
    }
}
