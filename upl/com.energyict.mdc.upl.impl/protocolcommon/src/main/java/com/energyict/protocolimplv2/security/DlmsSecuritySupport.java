package com.energyict.protocolimplv2.security;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.security.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for a DLMS protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/01/13
 * Time: 16:39
 */
public class DlmsSecuritySupport implements LegacyDeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    private static final String DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME = "DataTransportEncryptionKey";
    private static final String DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME = "DataTransportAuthenticationKey";
    private static final String HLS_SECRET_LEGACY_PROPERTY_NAME = "HlsSecret";
    private static final String HEX_PASSWORD_LEGACY_PROPERTY_NAME = "HexPassword";
    private static final String authenticationTranslationKeyConstant = "DlmsSecuritySupport.authenticationlevel.";
    private static final String encryptionTranslationKeyConstant = "DlmsSecuritySupport.encryptionlevel.";

    @Override
    public List<PropertySpec> getSecurityProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(DeviceSecurityProperty.PASSWORD.getPropertySpec());
        propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
        propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
        propertySpecs.add(getClientMacAddressPropertySpec());
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
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.DLMS_SECURITY.toString();
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
            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME,
                    deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel() +
                            ":" +
                            deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel());
            typedProperties.setProperty(getDataTransportEncryptionKeyLegacyPropertyName(),
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ENCRYPTION_KEY.toString(), ""));
            typedProperties.setProperty(getDataTransportAuthenticationKeyLegacyPropertyname(),
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), ""));
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
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(final TypedProperties oldTypedProperties) {
        String securityLevelProperty = oldTypedProperties.getStringProperty(SECURITY_LEVEL_PROPERTY_NAME);
        if (securityLevelProperty == null) {
            securityLevelProperty = getLegacySecurityLevelDefault();
        }
        if (!securityLevelProperty.contains(":")) {
            securityLevelProperty += ":0";
        }
        final int authenticationLevel = getAuthenticationLevel(securityLevelProperty);
        final int encryptionLevel = getEncryptionLevel(securityLevelProperty);
        checkForCorrectClientMacAddressPropertySpecType(oldTypedProperties);

        final TypedProperties result = TypedProperties.empty();
        result.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(oldTypedProperties, authenticationLevel, getAuthenticationAccessLevels()));
        result.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(oldTypedProperties, encryptionLevel, getEncryptionAccessLevels()));

        //Add properties that have a new key name or format (compared to EIServer 8.x)
        boolean passwordRequired = isRequiredOnThisLevel(SecurityPropertySpecName.PASSWORD.toString(), authenticationLevel, encryptionLevel);
        if (oldTypedProperties.hasValueFor(HLS_SECRET_LEGACY_PROPERTY_NAME) && passwordRequired) {
            result.setProperty(SecurityPropertySpecName.PASSWORD.toString(), oldTypedProperties.getStringProperty(HLS_SECRET_LEGACY_PROPERTY_NAME));
        }
        if (oldTypedProperties.hasValueFor(HEX_PASSWORD_LEGACY_PROPERTY_NAME) && passwordRequired) {
            result.setProperty(SecurityPropertySpecName.PASSWORD.toString(), oldTypedProperties.getStringProperty(HEX_PASSWORD_LEGACY_PROPERTY_NAME));
        }
        if (oldTypedProperties.hasValueFor(getDataTransportEncryptionKeyLegacyPropertyName()) && isRequiredOnThisLevel(SecurityPropertySpecName.ENCRYPTION_KEY.toString(), authenticationLevel, encryptionLevel)) {
            result.setProperty(SecurityPropertySpecName.ENCRYPTION_KEY.toString(), oldTypedProperties.getStringProperty(getDataTransportEncryptionKeyLegacyPropertyName()));
        }
        if (oldTypedProperties.hasValueFor(getDataTransportAuthenticationKeyLegacyPropertyname()) && isRequiredOnThisLevel(SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), authenticationLevel, encryptionLevel)) {
            result.setProperty(SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), oldTypedProperties.getStringProperty(getDataTransportAuthenticationKeyLegacyPropertyname()));
        }

        return new DeviceProtocolSecurityPropertySet() {
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

    private void checkForCorrectClientMacAddressPropertySpecType(TypedProperties typedProperties) {
        final Object clientMacAddress = typedProperties.getProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString());
        if (clientMacAddress != null && String.class.isAssignableFrom(clientMacAddress.getClass())) {
            typedProperties.removeProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString());
            try {
                typedProperties.setProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(), new BigDecimal((String) clientMacAddress));
            } catch (NumberFormatException e) {
                typedProperties.setProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(), new BigDecimal("1"));

            }
        }
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

    protected PropertySpec getClientMacAddressPropertySpec() {
        return DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec();
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

        private AuthenticationAccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        protected int getAccessLevel() {
            return this.accessLevel;
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

        private EncryptionAccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        protected int getAccessLevel() {
            return this.accessLevel;
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(getClientMacAddressPropertySpec());
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
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
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
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
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
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(getClientMacAddressPropertySpec());
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
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.PASSWORD.getPropertySpec());
            return propertySpecs;
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(getClientMacAddressPropertySpec());
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
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.PASSWORD.getPropertySpec());
            return propertySpecs;
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
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.PASSWORD.getPropertySpec());
            return propertySpecs;
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
        public List<PropertySpec> getSecurityProperties() {
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(getClientMacAddressPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec());
            propertySpecs.add(DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec());
            return propertySpecs;
        }
    }
}