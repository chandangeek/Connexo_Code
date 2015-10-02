package com.energyict.protocolimplv2.security;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.EncryptedStringFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.protocol.api.security.LegacySecurityPropertyConverter;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 10:51
 */
public class Mtu155SecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    @Inject
    public Mtu155SecuritySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    /**
     * Summarizes the used ID for the Encryption- and AuthenticationLevels.
     */
    protected enum AccessLevelIds {
        KEYT(0, TranslationKeys.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_0),
        KEYC(1,TranslationKeys.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_1),
        KEYF(2, TranslationKeys.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_2);

        private final int accessLevel;
        private final TranslationKey translationKey;

        private AccessLevelIds(int accessLevel, TranslationKey translationKey) {
            this.accessLevel = accessLevel;
            this.translationKey = translationKey;
        }

        protected int getAccessLevel() {
            return this.accessLevel;
        }

        public TranslationKey getTranslationKey() {
            return translationKey;
        }
    }

    @Override
    public List<PropertySpec> getSecurityPropertySpecs() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(this.propertySpecService),
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
        for (PropertySpec securityProperty : getSecurityPropertySpecs()) {
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

            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME, String.valueOf(securityProperties.get(0).getEncryptionDeviceAccessLevel().getId()));
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

    private PropertySpec getEncryptionKeyTPropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_1.toString())
                .setDefaultValue("")
                .markRequired()
                .finish();
    }

    private PropertySpec getEncryptionKeyCPropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_2.toString())
                .setDefaultValue("")
                .markRequired()
                .finish();
    }

    private PropertySpec getEncryptionKeyFPropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_3.toString())
                .setDefaultValue("")
                .markRequired()
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
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.NOORPASSWORDSECURITYSUPPORT_AUTHENTICATIONLEVEL_0).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
        }
    }

    protected class KeyCEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.KEYC.accessLevel;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(AccessLevelIds.KEYC.getTranslationKey()).format();
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
        public String getTranslation() {
            return thesaurus.getFormat(AccessLevelIds.KEYT.getTranslationKey()).format();
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
        public String getTranslation() {
            return thesaurus.getFormat(AccessLevelIds.KEYF.getTranslationKey()).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getEncryptionKeyFPropertySpec());
        }
    }
}
