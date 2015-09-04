package com.energyict.protocolimplv2.security;

import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TypedProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.LegacySecurityPropertyConverter;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;

/**
 * Provides general security <b>capabilities</b> for a DLMS protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/01/13
 * Time: 16:39
 */
public class DlmsSecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    private static final String DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME = "DataTransportEncryptionKey";
    private static final String DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME = "DataTransportAuthenticationKey";
    private final String authenticationTranslationKeyConstant = "DlmsSecuritySupport.authenticationlevel.";
    private final String encryptionTranslationKeyConstant = "DlmsSecuritySupport.encryptionlevel.";

    private PropertySpecService propertySpecService;

    @Inject
    public DlmsSecuritySupport(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
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

        private int getAccessLevel() {
            return this.accessLevel;
        }

    }

    /**
     * Summarizes the used ID for the EncryptionLevels.
     */
    protected enum EncryptionAccessLevelIds {
        NO_MESSAGE_ENCRYPTION(0),
        MESSAGE_ENCRYPTION(1),
        MESSAGE_AUTHENTICATION(2),
        MESSAGE_ENCRYPTION_AUTHENTICATION(3);

        private final int accessLevel;

        EncryptionAccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        protected int getAccessLevel() {
            return this.accessLevel;
        }
    }

    @Override
    public List<PropertySpec> getSecurityPropertySpecs() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService),
                DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService),
                DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService),
                DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService)
        );
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

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(final TypedProperties typedProperties) {
        String securityLevelProperty = typedProperties.getStringProperty(SECURITY_LEVEL_PROPERTY_NAME);
        if (securityLevelProperty == null) {
            securityLevelProperty = "0:0";
        }
        if (!securityLevelProperty.contains(":")) {
            securityLevelProperty+=":0";
        }
        final int authenticationLevel = getAuthenticationLevel(securityLevelProperty);
        final int encryptionLevel = getEncryptionLevel(securityLevelProperty);
        checkForCorrectClientMacAddressPropertySpecType(typedProperties);
        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, authenticationLevel, getAuthenticationAccessLevels()));
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, encryptionLevel, getEncryptionAccessLevels()));


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
                return securityRelatedTypedProperties;
            }
        };
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

    protected String getDataTransportAuthenticationKeyLegacyPropertyname() {
   	    return DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME;
   	}

    protected String getDataTransportEncryptionKeyLegacyPropertyName() {
        return DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME;
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
            return Collections.singletonList(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService));
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
            return Arrays.asList(
                    DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
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
            return Arrays.asList(
                    DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
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
            return Arrays.asList(
                    DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
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
            return Collections.singletonList(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService));
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
            return Arrays.asList(
                    DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
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
            return Collections.singletonList(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService));
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
            return Arrays.asList(
                    DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
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
            return Arrays.asList(
                    DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));

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
            return Arrays.asList(
                    DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(propertySpecService));
        }
    }

}