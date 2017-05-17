package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.LegacyDeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import com.energyict.mdc.upl.TypedProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides general security <b>capabilities</b> for a DLMS protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/01/13
 * Time: 16:39
 */
public class DlmsSecuritySupport extends AbstractSecuritySupport implements LegacyDeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    private static final String DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME = "DataTransportEncryptionKey";
    private static final String DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME = "DataTransportAuthenticationKey";
    private static final String HLS_SECRET_LEGACY_PROPERTY_NAME = "HlsSecret";
    private static final String HEX_PASSWORD_LEGACY_PROPERTY_NAME = "HexPassword";
    private static final String authenticationTranslationKeyConstant = "DlmsSecuritySupport.authenticationlevel.";
    private static final String encryptionTranslationKeyConstant = "DlmsSecuritySupport.encryptionlevel.";
    private static final String DEFAULT_CLIENT = "1";

    public DlmsSecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(DeviceSecurityProperty.PASSWORD.getPropertySpec(this.propertySpecService));
        propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(this.propertySpecService));
        propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(this.propertySpecService));
        return propertySpecs;
    }

    @Override
    public List<String> getLegacySecurityProperties() {
        return Arrays.asList(
                SECURITY_LEVEL_PROPERTY_NAME,
                HLS_SECRET_LEGACY_PROPERTY_NAME,
                HEX_PASSWORD_LEGACY_PROPERTY_NAME,
                getDataTransportAuthenticationKeyLegacyPropertyname(),
                getDataTransportEncryptionKeyLegacyPropertyName());
    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return Optional.of(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService));
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.asList(
                new NoAuthentication(),
                new LowLevelAuthentication(),
                new ManufactureAuthentication(),
                new Md5Authentication(),
                new Sha1Authentication(),
                new GmacAuthentication());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Arrays.asList(
                new NoMessageEncryption(),
                new MessageAuthentication(),
                new MessageEncryption(),
                new MessageEncryptionAndAuthentication());
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
            typedProperties.setProperty(SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS.toString(), deviceProtocolSecurityPropertySet.getClient()); // Add the ClientMacAddress
            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME,
                    deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel() +
                            ":" +
                            deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel());
            typedProperties.setProperty(getDataTransportEncryptionKeyLegacyPropertyName(),
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString(), ""));
            typedProperties.setProperty(getDataTransportAuthenticationKeyLegacyPropertyname(),
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString(), ""));
        }
        return typedProperties;
    }

    protected String getDataTransportAuthenticationKeyLegacyPropertyname() {
        return DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME;
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
        final int authenticationLevel = getAuthenticationLevel(securityLevelProperty);
        final int encryptionLevel = getEncryptionLevel(securityLevelProperty);
        final String client = loadCorrectClientMacAddressPropertyValue(oldTypedProperties);

        final TypedProperties result = TypedProperties.empty();
        result.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(oldTypedProperties, authenticationLevel, getAuthenticationAccessLevels()));
        result.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(oldTypedProperties, encryptionLevel, getEncryptionAccessLevels()));

        //Add properties that have a new key name or format (compared to EIServer 8.x)
        boolean passwordRequired = isRequiredOnThisLevel(SecurityPropertySpecTranslationKeys.PASSWORD.toString(), authenticationLevel, encryptionLevel);
        if (oldTypedProperties.hasValueFor(HLS_SECRET_LEGACY_PROPERTY_NAME) && passwordRequired) {
            result.setProperty(SecurityPropertySpecTranslationKeys.PASSWORD.toString(), oldTypedProperties.getStringProperty(HLS_SECRET_LEGACY_PROPERTY_NAME));
        }
        if (oldTypedProperties.hasValueFor(HEX_PASSWORD_LEGACY_PROPERTY_NAME) && passwordRequired) {
            result.setProperty(SecurityPropertySpecTranslationKeys.PASSWORD.toString(), oldTypedProperties.getStringProperty(HEX_PASSWORD_LEGACY_PROPERTY_NAME));
        }
        if (oldTypedProperties.hasValueFor(getDataTransportEncryptionKeyLegacyPropertyName()) && isRequiredOnThisLevel(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString(), authenticationLevel, encryptionLevel)) {
            result.setProperty(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString(), oldTypedProperties.getStringProperty(getDataTransportEncryptionKeyLegacyPropertyName()));
        }
        if (oldTypedProperties.hasValueFor(getDataTransportAuthenticationKeyLegacyPropertyname()) && isRequiredOnThisLevel(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString(), authenticationLevel, encryptionLevel)) {
            result.setProperty(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString(), oldTypedProperties.getStringProperty(getDataTransportAuthenticationKeyLegacyPropertyname()));
        }

        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public String getName() {
                return "security";
            }

            @Override
            public String getClient() {
                return client;
            }

            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return authenticationLevel;
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

    private boolean isRequiredOnThisLevel(String newPropertyKey, int authenticationLevel, int encryptionLevel) {
        for (DeviceAccessLevel deviceAccessLevel : getAuthenticationAccessLevels()) {
            if (deviceAccessLevel.getId() == authenticationLevel) {
                for (PropertySpec propertySpec : deviceAccessLevel.getSecurityProperties()) {
                    if (propertySpec.getName().equals(newPropertyKey)) {
                        return true;
                    }
                }
            }
        }

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

    private String loadCorrectClientMacAddressPropertyValue(TypedProperties typedProperties) {
        final Object clientMacAddress = typedProperties.getProperty(SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS.toString());
        if (clientMacAddress != null) {
            if (String.class.isAssignableFrom(clientMacAddress.getClass())) {
                typedProperties.removeProperty(SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS.toString());
                try {
                    typedProperties.setProperty(SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS.toString(), new BigDecimal((String) clientMacAddress));
                    return (String) clientMacAddress;
                } catch (NumberFormatException e) {
                    typedProperties.setProperty(SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS.toString(), new BigDecimal(DEFAULT_CLIENT));
                    return DEFAULT_CLIENT;
                }
            } else if (BigDecimal.class.isAssignableFrom(clientMacAddress.getClass())) {
                return String.valueOf(((BigDecimal) clientMacAddress).intValue());
            }
        }
        typedProperties.setProperty(SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS.toString(), new BigDecimal(DEFAULT_CLIENT));
        return DEFAULT_CLIENT;
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

    private int getAuthenticationLevel(String securityLevelProperty) {
        String authLevel = securityLevelProperty.substring(0, securityLevelProperty.indexOf(':'));
        try {
            return Integer.parseInt(authLevel);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Failed to extract AuthenticationDeviceAccessLevel from SecurityProperty '%s': %s could not be converted to int",
                    securityLevelProperty, authLevel));
        }
    }

    protected List<PropertySpec> getManufactureSpecificSecurityProperties() {
        return this.getManufactureSpecificSecurityProperties(this.propertySpecService);
    }

    protected List<PropertySpec> getManufactureSpecificSecurityProperties(PropertySpecService propertySpecService) {
        return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
    }

    /**
     * Summarizes the used ID for the AuthenticationLevels.
     */
    protected enum AuthenticationAccessLevelIds {
        NO_AUTHENTICATION(0),
        LOW_LEVEL_AUTHENTICATION(1),
        MANUFACTURER_SPECIFIC_AUTHENTICATION(2),
        MD5_AUTHENTICATION(3),
        SHA1_AUTHENTICATION(4),
        GMAC_AUTHENTICATION(5);

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
        MESSAGE_AUTHENTICATION(1),
        MESSAGE_ENCRYPTION(2),
        MESSAGE_ENCRYPTION_AUTHENTICATION(3);

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
            return encryptionTranslationKeyConstant + getId();
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
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "Message encryption";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
            return propertySpecs;
        }
    }

    /**
     * An encryption level where all APDU's are authenticated using
     * the encryption key and authentication key
     */
    protected class MessageAuthentication implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MESSAGE_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "Message authentication";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
            return propertySpecs;
        }
    }

    /**
     * An encryption level where all APDU's are authenticated <b>AND</b>
     * encrypted using the encryption key and authentication key
     */
    protected class MessageEncryptionAndAuthentication implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MESSAGE_ENCRYPTION_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "Message encryption and authentication";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
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
     * An authentication level which indicates that a plain text password
     * can be used to authenticate ourselves with the device.
     */
    protected class LowLevelAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "Low level authentication";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
        }
    }

    /**
     * An authentication level which indicates that a manufacturer specific
     * algorithm has to be used to authenticate with the device
     * <p/>
     * If this level should be used by your protocol, then make sure the provide the necessary properties.
     * As this is a manufacturer specific level, we can not <b>guess</b> what properties will be required.
     */
    protected class ManufactureAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_SPECIFIC_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "Manufacturer specific authentication";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return getManufactureSpecificSecurityProperties();
        }
    }

    /**
     * An authentication level specifying that an MD5 algorithm will be
     * used together with the password to authenticate ourselves with
     * the device.
     */
    protected class Md5Authentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "High level authentication using MD5";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
        }
    }

    /**
     * An authentication level specifying that an SHA1 algorithm will be
     * used together with the password to authenticate ourselves with
     * the device.
     */
    protected class Sha1Authentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "High level authentication using SHA1";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
        }
    }

    /**
     * An authentication level specifying that an GMAC algorithm will be
     * used together with the specific challenge to authenticate ourselves with
     * the device.
     */
    protected class GmacAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.GMAC_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return "High level authentication using GMAC";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService));
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
            return propertySpecs;
        }
    }
}