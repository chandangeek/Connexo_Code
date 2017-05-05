///*
// * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
// */
//
//package com.energyict.protocolimplv2.security;
//
//import com.elster.jupiter.nls.Thesaurus;
//import com.elster.jupiter.properties.PropertySpec;
//import com.energyict.mdc.common.TypedProperties;
//import com.energyict.mdc.dynamic.PropertySpecService;
//import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
//import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
//import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
//import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;
//import com.energyict.protocols.mdc.services.impl.TranslationKeys;
//
//import javax.inject.Inject;
//import java.math.BigDecimal;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//public class DlmsSecuritySupportCryptography implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {
//
//    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
//    private static final String DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME = "DataTransportEncryptionKey";
//    private static final String DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME = "DataTransportAuthenticationKey";
//
//    private final PropertySpecService propertySpecService;
//    private final Thesaurus thesaurus;
//
//    @Inject
//    public DlmsSecuritySupportCryptography(PropertySpecService propertySpecService, Thesaurus thesaurus) {
//        super();
//        this.propertySpecService = propertySpecService;
//        this.thesaurus = thesaurus;
//    }
//
//    /**
//     * Summarizes the used ID for the AuthenticationLevels.
//     */
//    enum AuthenticationAccessLevelIds {
//        NO_AUTHENTICATION(0),
//        LOW_LEVEL_AUTHENTICATION(1),
//        MANUFACTURER_SPECIFIC_AUTHENTICATION(2),
//        MD5_AUTHENTICATION(3),
//        SHA1_AUTHENTICATION(4),
//        GMAC_AUTHENTICATION(5);
//
//        private final int accessLevel;
//
//        AuthenticationAccessLevelIds(int accessLevel) {
//            this.accessLevel = accessLevel;
//        }
//
//        int getAccessLevel() {
//            return this.accessLevel;
//        }
//
//    }
//
//    /**
//     * Summarizes the used ID for the EncryptionLevels.
//     */
//    enum EncryptionAccessLevelIds {
//        NO_MESSAGE_ENCRYPTION(0),
//        MESSAGE_AUTHENTICATION(1),
//        MESSAGE_ENCRYPTION(2),
//        MESSAGE_ENCRYPTION_AUTHENTICATION(3);
//
//        private final int accessLevel;
//
//        EncryptionAccessLevelIds(int accessLevel) {
//            this.accessLevel = accessLevel;
//        }
//
//        int getAccessLevel() {
//            return this.accessLevel;
//        }
//    }
//
//    public PropertySpecService getPropertySpecService() {
//        return propertySpecService;
//    }
//
//    @Override
//    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
//        return Arrays.asList(
//                new NoAuthentication(),
//                new LowLevelAuthentication(),
//                new ManufactureAuthentication(),
//                new Md5Authentication(),
//                new Sha1Authentication(),
//                new GmacAuthentication());
//    }
//
//    @Override
//    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
//        return Arrays.asList(
//                new NoMessageEncryption(),
//                new MessageAuthentication(),
//                new MessageEncryption(),
//                new MessageEncryptionAndAuthentication());
//    }
//
//    @Override
//    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
//        TypedProperties typedProperties = TypedProperties.empty();
//        if (deviceProtocolSecurityPropertySet != null) {
//            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
//            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME,
//                    deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel() +
//                            ":" +
//                            deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel());
//            typedProperties.setProperty(getDataTransportEncryptionKeyLegacyPropertyName(),
//                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ENCRYPTION_KEY_WITH_KEY_ACCESSOR.getKey(), ""));
//            typedProperties.setProperty(getDataTransportAuthenticationKeyLegacyPropertyname(),
//                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.AUTHENTICATION_KEY_WITH_KEY_ACCESSOR.getKey(), ""));
//        }
//        return typedProperties;
//    }
//
//    @Override
//    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(final TypedProperties typedProperties) {
//        String securityLevelProperty = typedProperties.getStringProperty(SECURITY_LEVEL_PROPERTY_NAME);
//        if (securityLevelProperty == null) {
//            securityLevelProperty = "0:0";
//        }
//        if (!securityLevelProperty.contains(":")) {
//            securityLevelProperty+=":0";
//        }
//        final int authenticationLevel = getAuthenticationLevel(securityLevelProperty);
//        final int encryptionLevel = getEncryptionLevel(securityLevelProperty);
//        final String client = getClientMacAddress(typedProperties);
//        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
//        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, authenticationLevel, getAuthenticationAccessLevels()));
//        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, encryptionLevel, getEncryptionAccessLevels()));
//
//
//        return new DeviceProtocolSecurityPropertySet() {
//            @Override
//            public String getClient() {
//                return client;
//            }
//
//            @Override
//            public int getAuthenticationDeviceAccessLevel() {
//                return authenticationLevel;
//            }
//
//            @Override
//            public int getEncryptionDeviceAccessLevel() {
//                return encryptionLevel;
//            }
//
//            @Override
//            public TypedProperties getSecurityProperties() {
//                return securityRelatedTypedProperties;
//            }
//        };
//    }
//
//    private String getClientMacAddress(TypedProperties typedProperties) {
//        final Object clientMacAddress = typedProperties.getProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.getKey());
//        if (clientMacAddress != null) {
//            if (String.class.isAssignableFrom(clientMacAddress.getClass())) {
//                return (String) clientMacAddress;
//            } else if (BigDecimal.class.isAssignableFrom(clientMacAddress.getClass())) {
//                return Integer.toString(((BigDecimal) clientMacAddress).intValue());
//            }
//        }
//        return "1"; // Use client 1 as fallback
//    }
//
//    private int getEncryptionLevel(String securityLevelProperty) {
//        String encryptionLevel = securityLevelProperty.substring(securityLevelProperty.indexOf(':') + 1);
//        try {
//            return Integer.parseInt(encryptionLevel);
//        } catch (NumberFormatException e) {
//            throw new IllegalArgumentException(String.format("Failed to extract EncryptionDeviceAccessLevel from SecurityProperty '%s': %s could not be converted to int",
//                    securityLevelProperty, encryptionLevel));
//        }
//    }
//
//    private int getAuthenticationLevel(String securityLevelProperty) {
//        String authLevel = securityLevelProperty.substring(0, securityLevelProperty.indexOf(':'));
//        try {
//            return Integer.parseInt(authLevel);
//        } catch (NumberFormatException e) {
//            throw new IllegalArgumentException(String.format("Failed to extract AuthenticationDeviceAccessLevel from SecurityProperty '%s': %s could not be converted to int",
//                    securityLevelProperty, authLevel));
//        }
//    }
//
//    protected String getDataTransportAuthenticationKeyLegacyPropertyname() {
//   	    return DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME;
//   	    }
//
//    @Override
//    public Optional<PropertySpec> getClientSecurityPropertySpec() {
//        return Optional.of(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService, thesaurus));
//    }
//
//    protected String getDataTransportEncryptionKeyLegacyPropertyName() {
//        return DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME;
//    }
//
//    /**
//     * An encryption level where no encryption is done, no properties must be set
//     */
//    protected class NoMessageEncryption implements EncryptionDeviceAccessLevel {
//
//        @Override
//        public int getId() {
//            return EncryptionAccessLevelIds.NO_MESSAGE_ENCRYPTION.getAccessLevel();
//        }
//
//        @Override
//        public String getTranslation() {
//            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_0).format();
//        }
//
//        @Override
//        public List<PropertySpec> getSecurityProperties() {
//            return Collections.emptyList();
//        }
//    }
//
//    /**
//     * An encryption level where all APDU's are encrypted using
//     * the encryption key and authentication key
//     */
//    protected class MessageEncryption implements EncryptionDeviceAccessLevel {
//
//        @Override
//        public int getId() {
//            return EncryptionAccessLevelIds.MESSAGE_ENCRYPTION.getAccessLevel();
//        }
//
//        @Override
//        public String getTranslation() {
//            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_2).format();
//        }
//
//        @Override
//        public List<PropertySpec> getSecurityProperties() {
//            return Arrays.asList(
//                    DeviceSecurityProperty.ENCRYPTION_KEY_WITH_KEY_ACCESSOR.getPropertySpec(propertySpecService, thesaurus),
//                    DeviceSecurityProperty.AUTHENTICATION_KEY_WITH_KEY_ACCESSOR.getPropertySpec(propertySpecService, thesaurus));
//        }
//    }
//
//    /**
//     * An encryption level where all APDU's are authenticated using
//     * the encryption key and authentication key
//     */
//    protected class MessageAuthentication implements EncryptionDeviceAccessLevel {
//
//        @Override
//        public int getId() {
//            return EncryptionAccessLevelIds.MESSAGE_AUTHENTICATION.getAccessLevel();
//        }
//
//        @Override
//        public String getTranslation() {
//            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_1).format();
//        }
//
//        @Override
//        public List<PropertySpec> getSecurityProperties() {
//            return Arrays.asList(
//                    DeviceSecurityProperty.ENCRYPTION_KEY_WITH_KEY_ACCESSOR.getPropertySpec(propertySpecService, thesaurus),
//                    DeviceSecurityProperty.AUTHENTICATION_KEY_WITH_KEY_ACCESSOR.getPropertySpec(propertySpecService, thesaurus));
//        }
//    }
//
//    /**
//     * An encryption level where all APDU's are authenticated <b>AND</b>
//     * encrypted using the encryption key and authentication key
//     */
//    protected class MessageEncryptionAndAuthentication implements EncryptionDeviceAccessLevel {
//
//        @Override
//        public int getId() {
//            return EncryptionAccessLevelIds.MESSAGE_ENCRYPTION_AUTHENTICATION.getAccessLevel();
//        }
//
//        @Override
//        public String getTranslation() {
//            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORT_ENCRYPTIONLEVEL_3).format();
//        }
//
//        @Override
//        public List<PropertySpec> getSecurityProperties() {
//            return Arrays.asList(
//                    DeviceSecurityProperty.ENCRYPTION_KEY_WITH_KEY_ACCESSOR.getPropertySpec(propertySpecService, thesaurus),
//                    DeviceSecurityProperty.AUTHENTICATION_KEY_WITH_KEY_ACCESSOR.getPropertySpec(propertySpecService, thesaurus));
//        }
//    }
//
//    /**
//     * An authentication level which indicate that no authentication is required
//     * for communication with the device.
//     */
//    protected class NoAuthentication implements AuthenticationDeviceAccessLevel {
//
//        @Override
//        public int getId() {
//            return AuthenticationAccessLevelIds.NO_AUTHENTICATION.getAccessLevel();
//        }
//
//        @Override
//        public String getTranslation() {
//            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_0).format();
//        }
//
//        @Override
//        public List<PropertySpec> getSecurityProperties() {
//            return Collections.emptyList();
//        }
//    }
//
//    /**
//     * An authentication level which indicates that a plain text password
//     * can be used to authenticate ourselves with the device.
//     */
//    protected class LowLevelAuthentication implements AuthenticationDeviceAccessLevel {
//
//        @Override
//        public int getId() {
//            return AuthenticationAccessLevelIds.LOW_LEVEL_AUTHENTICATION.getAccessLevel();
//        }
//
//        @Override
//        public String getTranslation() {
//            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_1).format();
//        }
//
//        @Override
//        public List<PropertySpec> getSecurityProperties() {
//            return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService, thesaurus));
//        }
//    }
//
//    /**
//     * An authentication level which indicates that a manufacturer specific
//     * algorithm has to be used to authenticate with the device
//     * <p/>
//     * If this level should be used by your protocol, then make sure the provide the necessary properties.
//     * As this is a manufacturer specific level, we can not <b>guess</b> what properties will be required.
//     */
//    protected class ManufactureAuthentication implements AuthenticationDeviceAccessLevel {
//
//        @Override
//        public int getId() {
//            return AuthenticationAccessLevelIds.MANUFACTURER_SPECIFIC_AUTHENTICATION.getAccessLevel();
//        }
//
//        @Override
//        public String getTranslation() {
//            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_2).format();
//        }
//
//        @Override
//        public List<PropertySpec> getSecurityProperties() {
//            return Collections.emptyList();
//        }
//    }
//
//    /**
//     * An authentication level specifying that an MD5 algorithm will be
//     * used together with the password to authenticate ourselves with
//     * the device.
//     */
//    protected class Md5Authentication implements AuthenticationDeviceAccessLevel {
//
//        @Override
//        public int getId() {
//            return AuthenticationAccessLevelIds.MD5_AUTHENTICATION.getAccessLevel();
//        }
//
//        @Override
//        public String getTranslation() {
//            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_3).format();
//        }
//
//        @Override
//        public List<PropertySpec> getSecurityProperties() {
//            return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService, thesaurus));
//        }
//    }
//
//    /**
//     * An authentication level specifying that an SHA1 algorithm will be
//     * used together with the password to authenticate ourselves with
//     * the device.
//     */
//    protected class Sha1Authentication implements AuthenticationDeviceAccessLevel {
//
//        @Override
//        public int getId() {
//            return AuthenticationAccessLevelIds.SHA1_AUTHENTICATION.getAccessLevel();
//        }
//
//        @Override
//        public String getTranslation() {
//            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_4).format();
//        }
//
//        @Override
//        public List<PropertySpec> getSecurityProperties() {
//            return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService, thesaurus));
//
//        }
//    }
//
//    /**
//     * An authentication level specifying that an GMAC algorithm will be
//     * used together with the specific challenge to authenticate ourselves with
//     * the device.
//     */
//    protected class GmacAuthentication implements AuthenticationDeviceAccessLevel {
//
//        @Override
//        public int getId() {
//            return AuthenticationAccessLevelIds.GMAC_AUTHENTICATION.getAccessLevel();
//        }
//
//        @Override
//        public String getTranslation() {
//            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORT_AUTHENTICATIONLEVEL_5).format();
//        }
//
//        @Override
//        public List<PropertySpec> getSecurityProperties() {
//            return Arrays.asList(
//                    DeviceSecurityProperty.ENCRYPTION_KEY_WITH_KEY_ACCESSOR.getPropertySpec(propertySpecService, thesaurus),
//                    DeviceSecurityProperty.AUTHENTICATION_KEY_WITH_KEY_ACCESSOR.getPropertySpec(propertySpecService, thesaurus));
//        }
//    }
//
//}