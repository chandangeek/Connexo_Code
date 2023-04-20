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
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides general security <b>capabilities</b> for a Merlin Water clip-on protocol, both inbound and outbound
 */
public class MerlinSecuritySupport extends AbstractSecuritySupport implements LegacyDeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    private static final String DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME = "DataTransportEncryptionKey";
    private static final String AUTHENTICATION_TRANSLATION_KEY = "MerlinSecuritySupport.authenticationlevel.";
    private static final String ENCRYPTION_TRANSLATION_KEY = "MerlinSecuritySupport.encryptionlevel.";

    public MerlinSecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(this.propertySpecService));
        return propertySpecs;
    }

    @Override
    public List<String> getLegacySecurityProperties() {
        return Arrays.asList(
                SECURITY_LEVEL_PROPERTY_NAME,
                getDataTransportEncryptionKeyLegacyPropertyName());
    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return Optional.empty();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.singletonList(
                new NoAuthentication()
        );
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Arrays.asList(
                new NoMessageEncryption(),
                new MessageEncryption()
        );
    }

    @Override
    public Optional<PropertySpec> getSecurityPropertySpec(String name) {
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
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME,
                    deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel() +
                            ":" +
                            deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel());
            typedProperties.setProperty(getDataTransportEncryptionKeyLegacyPropertyName(),
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString(), ""));
        }
        return typedProperties;
    }

    protected String getDataTransportEncryptionKeyLegacyPropertyName() {
        return DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        return this.convertFromTypedProperties(TypedProperties.copyOf(typedProperties));
    }

    private DeviceProtocolSecurityPropertySet convertFromTypedProperties(final TypedProperties oldTypedProperties) {
        String securityLevelProperty = oldTypedProperties.getStringProperty(SECURITY_LEVEL_PROPERTY_NAME);
        if (securityLevelProperty == null) {
            securityLevelProperty = getLegacySecurityLevelDefault();
        }
        if (!securityLevelProperty.contains(":")) {
            securityLevelProperty += ":0";
        }
        final int encryptionLevel = getEncryptionLevel(securityLevelProperty);

        final TypedProperties result = TypedProperties.empty();
        result.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(oldTypedProperties, encryptionLevel, getEncryptionAccessLevels()));

        if (oldTypedProperties.hasValueFor(getDataTransportEncryptionKeyLegacyPropertyName()) && isRequiredOnThisLevel(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString(), encryptionLevel)) {
            result.setProperty(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString(), oldTypedProperties.getStringProperty(getDataTransportEncryptionKeyLegacyPropertyName()));
        }

        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public String getName() {
                return "security";
            }

            @Override
            public Object getClient() {
                return 0;
            }

            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return 0;
            }

            @Override
            public int getEncryptionDeviceAccessLevel() {
                return encryptionLevel;
            }

            @Override
            public TypedProperties getSecurityProperties() {
                return result;
            }
        };
    }

    protected String getLegacySecurityLevelDefault() {
        return "0:0";
    }

    private boolean isRequiredOnThisLevel(String newPropertyKey, int encryptionLevel) {
        for (DeviceAccessLevel deviceAccessLevel : getEncryptionAccessLevels()) {
            if (deviceAccessLevel.getId() == encryptionLevel) {
                for (PropertySpec propertySpec : deviceAccessLevel.getSecurityProperties()) {
                    if (propertySpec.getName().equals(newPropertyKey)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private int getEncryptionLevel(String securityLevelProperty) {
        String encryptionLevel = securityLevelProperty.substring(securityLevelProperty.indexOf(':') + 1);
        try {
            return Integer.parseInt(encryptionLevel);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Failed to extract EncryptionDeviceAccessLevel from SecurityProperty '%s': %s could not be converted to int",
                    securityLevelProperty, encryptionLevel));
        }
    }

    /**
     * Summarizes the used ID for the AuthenticationLevels.
     */
    protected enum AuthenticationAccessLevelIds {
        NO_AUTHENTICATION(0);

        private final int accessLevel;

        AuthenticationAccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        int getAccessLevel() {
            return accessLevel;
        }
    }

    /**
     * Summarizes the used ID for the EncryptionLevels.
     */
    protected enum EncryptionAccessLevelIds {
        NO_MESSAGE_ENCRYPTION(0),
        MESSAGE_ENCRYPTION(2);

        private final int accessLevel;

        EncryptionAccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        int getAccessLevel() {
            return accessLevel;
        }
    }

    /**
     * An encryption level where no encryption is done, no properties must be set
     */
    protected class NoMessageEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.NO_MESSAGE_ENCRYPTION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return ENCRYPTION_TRANSLATION_KEY + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "No message encryption";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }
    }

    /**
     * An encryption level where all APDU's are encrypted using
     * the encryption key and authentication key
     */
    protected class MessageEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MESSAGE_ENCRYPTION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return ENCRYPTION_TRANSLATION_KEY + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "Message encryption";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            return propertySpecs;
        }
    }


    /**
     * An authentication level which indicate that no authentication is required
     * for communication with the device.
     */
    protected class NoAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.NO_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return AUTHENTICATION_TRANSLATION_KEY + getId();
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

}
