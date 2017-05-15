package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.LegacyDeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.properties.nls.PropertyTranslationKeys;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides general security <b>capabilities</b> for a DLMS protocol, which
 * has a SecurityObject for each possible client.
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/06/13
 * Time: 15:02
 */
//TODO: as we have introduced client support (#getClientSecurityPropertySpec) is this class still relevant? Check to review/update!
public class DlmsSecuritySupportPerClient extends AbstractSecuritySupport implements LegacyDeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    private static final String authenticationTranslationKeyConstant = "DlmsSecuritySupportPerClient.authenticationlevel.";
    private static final String encryptionTranslationKeyConstant = "DlmsSecuritySupportPerClient.encryptionlevel.";
    private static final String DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME = "DataTransportEncryptionKey";
    private static final String DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME = "DataTransportAuthenticationKey";

    public DlmsSecuritySupportPerClient(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    private PropertySpec getEncryptionKeyPublicPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_PUBLIC.toString(), true, PropertyTranslationKeys.SECURITY_ENCRYPTION_KEY_PUBLIC, propertySpecService::encryptedStringSpec)
                .finish();
    }

    private PropertySpec getEncryptionKeyDataPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_DATA.toString(), true, PropertyTranslationKeys.SECURITY_ENCRYPTION_KEY_DATA, propertySpecService::encryptedStringSpec)
                .finish();
    }

    private PropertySpec getEncryptionKeyExtDataPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_EXT_DATA.toString(), true, PropertyTranslationKeys.SECURITY_ENCRYPTION_KEY_EXT_DATA, propertySpecService::encryptedStringSpec)
                .finish();
    }

    private PropertySpec getEncryptionKeyManagementPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_MANAGEMENT.toString(), true, PropertyTranslationKeys.SECURITY_ENCRYPTION_KEY_MANAGEMENT, propertySpecService::encryptedStringSpec)
                .finish();
    }

    private PropertySpec getEncryptionKeyFirmwarePropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_FIRMWARE.toString(), true, PropertyTranslationKeys.SECURITY_ENCRYPTION_KEY_FIRMWARE, propertySpecService::encryptedStringSpec)
                .finish();
    }

    private PropertySpec getEncryptionKeyManufacturerPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_MANUFACTURER.toString(), true, PropertyTranslationKeys.SECURITY_ENCRYPTION_KEY_MANUFACTURER, propertySpecService::encryptedStringSpec)
                .finish();
    }

    private PropertySpec getAuthenticationKeyPublicPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_PUBLIC.toString(), true, PropertyTranslationKeys.SECURITY_AUTHENTICATION_KEY_PUBLIC, propertySpecService::encryptedStringSpec)
                .finish();
    }

    private PropertySpec getAuthenticationKeyDataPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_DATA.toString(), true, PropertyTranslationKeys.SECURITY_AUTHENTICATION_KEY_DATA, propertySpecService::encryptedStringSpec)
                .finish();
    }

    private PropertySpec getAuthenticationKeyExtDataPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_EXT_DATA.toString(), true, PropertyTranslationKeys.SECURITY_AUTHENTICATION_KEY_EXT_DATA, propertySpecService::encryptedStringSpec)
                .finish();
    }

    private PropertySpec getAuthenticationKeyManagementPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_MANAGEMENT.toString(), true, PropertyTranslationKeys.SECURITY_AUTHENTICATION_KEY_MANAGEMENT, propertySpecService::encryptedStringSpec)
                .finish();
    }

    private PropertySpec getAuthenticationKeyFirmwarePropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_FIRMWARE.toString(), true, PropertyTranslationKeys.SECURITY_AUTHENTICATION_KEY_FIRMWARE, propertySpecService::encryptedStringSpec)
                .finish();
    }

    private PropertySpec getAuthenticationKeyManufacturerPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_MANUFACTURER.toString(), true, PropertyTranslationKeys.SECURITY_AUTHENTICATION_KEY_MANUFACTURER, propertySpecService::encryptedStringSpec)
                .finish();
    }

    private PropertySpec getPasswordPublicPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.PASSWORD_PUBLIC.toString(), true, PropertyTranslationKeys.SECURITY_PASSWORD_PUBLIC, propertySpecService::encryptedStringSpec)
                .finish();
    }

    private PropertySpec getPasswordDataPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.PASSWORD_DATA.toString(), true, PropertyTranslationKeys.SECURITY_PASSWORD_DATA, propertySpecService::encryptedStringSpec)
                .finish();
    }

    private PropertySpec getPasswordExtDataPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.PASSWORD_EXT_DATA.toString(), true, PropertyTranslationKeys.SECURITY_PASSWORD_EXT_DATA, propertySpecService::encryptedStringSpec)
                .finish();
    }

    private PropertySpec getPasswordManagementPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.PASSWORD_MANAGEMENT.toString(), true, PropertyTranslationKeys.SECURITY_PASSWORD_MANAGEMENT, propertySpecService::encryptedStringSpec)
                .finish();
    }

    private PropertySpec getPasswordFirmwarePropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.PASSWORD_FIRMWARE.toString(), true, PropertyTranslationKeys.SECURITY_PASSWORD_FIRMWARE, propertySpecService::encryptedStringSpec)
                .finish();
    }

    private PropertySpec getPasswordManufacturerPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(SecurityPropertySpecTranslationKeys.PASSWORD_MANUFACTURER.toString(), true, PropertyTranslationKeys.SECURITY_PASSWORD_MANUFACTURER, propertySpecService::encryptedStringSpec)
                .finish();
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                getAuthenticationKeyDataPropertySpec(),
                getAuthenticationKeyExtDataPropertySpec(),
                getAuthenticationKeyFirmwarePropertySpec(),
                getAuthenticationKeyManagementPropertySpec(),
                getAuthenticationKeyManufacturerPropertySpec(),
                getAuthenticationKeyPublicPropertySpec(),
                getEncryptionKeyDataPropertySpec(),
                getEncryptionKeyExtDataPropertySpec(),
                getEncryptionKeyFirmwarePropertySpec(),
                getEncryptionKeyManagementPropertySpec(),
                getEncryptionKeyManufacturerPropertySpec(),
                getEncryptionKeyPublicPropertySpec(),
                getPasswordDataPropertySpec(),
                getPasswordExtDataPropertySpec(),
                getPasswordFirmwarePropertySpec(),
                getPasswordManagementPropertySpec(),
                getPasswordManufacturerPropertySpec(),
                getPasswordPublicPropertySpec()
        );
    }

    @Override
    public List<String> getLegacySecurityProperties() {
        return Arrays.asList(
                SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS.toString(),
                DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME,
                SECURITY_LEVEL_PROPERTY_NAME,
                DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME
        );
    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return Optional.of(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(this.propertySpecService));
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.<AuthenticationDeviceAccessLevel>asList(
                new NoAuthenticationPublic(),
                new NoAuthenticationDataCollection(),
                new NoAuthenticationExtendedDataCollection(),
                new NoAuthenticationManagement(),
                new NoAuthenticationFirmware(),
                new NoAuthenticationManufacturer(),
                new LowLevelAuthenticationPublic(),
                new LowLevelAuthenticationData(),
                new LowLevelAuthenticationExtendedData(),
                new LowLevelAuthenticationManagement(),
                new LowLevelAuthenticationFirmware(),
                new LowLevelAuthenticationManufacturer(),
                new Md5AuthenticationPublic(),
                new Md5AuthenticationData(),
                new Md5AuthenticationExtendedData(),
                new Md5AuthenticationManagement(),
                new Md5AuthenticationFirmware(),
                new Md5AuthenticationManufacturer(),
                new ShaAuthenticationPublic(),
                new ShaAuthenticationData(),
                new ShaAuthenticationExtendedData(),
                new ShaAuthenticationManagement(),
                new ShaAuthenticationFirmware(),
                new ShaAuthenticationManufacturer(),
                new GmacAuthenticationPublic(),
                new GmacAuthenticationData(),
                new GmacAuthenticationExtendedData(),
                new GmacAuthenticationManagement(),
                new GmacAuthenticationFirmware(),
                new GmacAuthenticationManufacturer()
        );
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Arrays.<EncryptionDeviceAccessLevel>asList(
                new NoEncryptionPublic(),
                new NoEncryptionData(),
                new NoEncryptionExtendedData(),
                new NoEncryptionManagement(),
                new NoEncryptionFirmware(),
                new NoEncryptionManufacturer(),
                new MessageEncryptionPublic(),
                new MessageEncryptionData(),
                new MessageEncryptionExtendedData(),
                new MessageEncryptionManagement(),
                new MessageEncryptionFirmware(),
                new MessageEncryptionManufacturer(),
                new MessageAuthenticationPublic(),
                new MessageAuthenticationData(),
                new MessageAuthenticationExtendedData(),
                new MessageAuthenticationManagement(),
                new MessageAuthenticationFirmware(),
                new MessageAuthenticationManufacturer(),
                new MessageEncryptionAndAuthenticationPublic(),
                new MessageEncryptionAndAuthenticationData(),
                new MessageEncryptionAndAuthenticationExtendedData(),
                new MessageEncryptionAndAuthenticationManagement(),
                new MessageEncryptionAndAuthenticationFirmware(),
                new MessageEncryptionAndAuthenticationManufacturer()
        );
    }

    @Override
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = com.energyict.protocolimpl.properties.TypedProperties.empty();
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
            typedProperties.setProperty(SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS.toString(), deviceProtocolSecurityPropertySet.getClient()); // Add the ClientMacAddress
            convertToProperPassword(deviceProtocolSecurityPropertySet, typedProperties);

            final int clientId = AuthenticationAccessLevelIds.getClientIdFor(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel());
            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME,
                    AuthenticationAccessLevelIds.getSimpleAuthenticationAccessLevelForClientMacAndNewAccessLevel(clientId, deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()) +
                            ":" +
                            EncryptionAccessLevelIds.getSimpleEncryptionAccessLevelForClientMacAndNewAccessLevel(clientId, deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()));
            typedProperties.setProperty(DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME,
                    getDataTransportEncryptionKeyPropertyValue(deviceProtocolSecurityPropertySet));
            typedProperties.setProperty(DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME,
                    getDataTransportAuthenticationKeyPropertyValue(deviceProtocolSecurityPropertySet));
            typedProperties.setProperty(SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS.toString(),
                    clientId
            );
        }
        return typedProperties;
    }

    private Object getDataTransportAuthenticationKeyPropertyValue(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        List<String> authenticationKeyNames = Arrays.asList(
                SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_PUBLIC.toString(), SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_DATA.toString(), SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_EXT_DATA.toString(),
                SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_MANAGEMENT.toString(), SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_FIRMWARE.toString(), SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_MANUFACTURER.toString());
        return getValueFrom(authenticationKeyNames, deviceProtocolSecurityPropertySet);
    }

    private Object getDataTransportEncryptionKeyPropertyValue(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        List<String> encryptionKeyNames = Arrays.asList(
                SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_PUBLIC.toString(), SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_DATA.toString(), SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_EXT_DATA.toString(),
                SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_MANAGEMENT.toString(), SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_FIRMWARE.toString(), SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_MANUFACTURER.toString());
        return getValueFrom(encryptionKeyNames, deviceProtocolSecurityPropertySet);
    }

    private Object getValueFrom(final List<String> propertyNames, final DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        for (String propertyName : propertyNames) {
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(propertyName);
            if (property != null) {
                return property;
            }
        }
        return "";
    }

    private void convertToProperPassword(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet, TypedProperties typedProperties) {
        List<String> passWordPropertyNames = Arrays.asList(
                SecurityPropertySpecTranslationKeys.PASSWORD_PUBLIC.toString(), SecurityPropertySpecTranslationKeys.PASSWORD_DATA.toString(), SecurityPropertySpecTranslationKeys.PASSWORD_EXT_DATA.toString(),
                SecurityPropertySpecTranslationKeys.PASSWORD_MANAGEMENT.toString(), SecurityPropertySpecTranslationKeys.PASSWORD_FIRMWARE.toString(), SecurityPropertySpecTranslationKeys.PASSWORD_MANUFACTURER.toString());
        boolean notFound = true;
        for (String passWordPropertyName : passWordPropertyNames) {
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(passWordPropertyName);
            if (property != null && notFound) {
                typedProperties.setProperty(SecurityPropertySpecTranslationKeys.PASSWORD.toString(), property);
                notFound = false;
            }
        }
        if (notFound) {
            typedProperties.setProperty(SecurityPropertySpecTranslationKeys.PASSWORD.toString(), "");
        }
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        String securityLevelProperty = typedProperties.getTypedProperty(SECURITY_LEVEL_PROPERTY_NAME);
        if (securityLevelProperty == null) {
            securityLevelProperty = "0:0";
        }
        if (!securityLevelProperty.contains(":")) {
            throw new IllegalStateException("Cannot convert TypedProperties: expected property " + SECURITY_LEVEL_PROPERTY_NAME + " to have format '<auth>:<encryption>', but found " + securityLevelProperty);
        }
        final int clientMacAddressValue = getClientMacAddressValue(typedProperties);

        final int authenticationLevelPropertyValue = AuthenticationAccessLevelIds.getAuthenticationAccessLevelForClientMacAndOriginalAccessLevel(clientMacAddressValue, getAuthenticationLevel(securityLevelProperty));
        final int encryptionLevelPropertyValue = EncryptionAccessLevelIds.getEncryptionAccessLevelForClientMacAndOriginalAccessLevel(clientMacAddressValue, getEncryptionLevel(securityLevelProperty));
        final TypedProperties securityRelatedTypedProperties = com.energyict.protocolimpl.properties.TypedProperties.empty();
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, authenticationLevelPropertyValue, getAuthenticationAccessLevels()));
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, encryptionLevelPropertyValue, getEncryptionAccessLevels()));

        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public String getName() {
                return "security";
            }

            @Override
            public String getClient() {
                return Integer.toString(clientMacAddressValue);
            }

            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return authenticationLevelPropertyValue;
            }

            @Override
            public int getEncryptionDeviceAccessLevel() {
                return encryptionLevelPropertyValue;
            }

            @Override
            public TypedProperties getSecurityProperties() {
                return securityRelatedTypedProperties;
            }
        };
    }

    private int getClientMacAddressValue(TypedProperties typedProperties) {
        final Object clientMacAddress = typedProperties.getProperty(SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS.toString());
        if (clientMacAddress != null) {
            if (String.class.isAssignableFrom(clientMacAddress.getClass())) {
                return Integer.valueOf((String) clientMacAddress);
            } else {
                return ((BigDecimal) clientMacAddress).intValue();
            }
        } else {
            return 16;  // returning the public client if none was configured
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

    /**
     * Summarizes the used ID for the Encryption- and AuthenticationLevels.
     */
    protected enum AuthenticationAccessLevelIds {

        PUBLIC_CLIENT_NO_AUTHENTICATION(0, 16),
        PUBLIC_CLIENT_LOW_LEVEL_AUTHENTICATION(1, 16),
        //        PUBLIC_CLIENT_MANUFACTURER_SPECIFIC_AUTHENTICATION(2, 16),
        PUBLIC_CLIENT_MD5_AUTHENTICATION(3, 16),
        PUBLIC_CLIENT_SHA1_AUTHENTICATION(4, 16),
        PUBLIC_CLIENT_GMAC_AUTHENTICATION(5, 16),

        DATA_CLIENT_NO_AUTHENTICATION(6, 32),
        DATA_CLIENT_LOW_LEVEL_AUTHENTICATION(7, 32),
        //        DATA_CLIENT_MANUFACTURER_SPECIFIC_AUTHENTICATION(8, 32),
        DATA_CLIENT_MD5_AUTHENTICATION(9, 32),
        DATA_CLIENT_SHA1_AUTHENTICATION(10, 32),
        DATA_CLIENT_GMAC_AUTHENTICATION(11, 32),

        EXT_DATA_CLIENT_NO_AUTHENTICATION(12, 48),
        EXT_DATA_CLIENT_LOW_LEVEL_AUTHENTICATION(13, 48),
        //        EXT_DATA_CLIENT_MANUFACTURER_SPECIFIC_AUTHENTICATION(14, 48),
        EXT_DATA_CLIENT_MD5_AUTHENTICATION(15, 48),
        EXT_DATA_CLIENT_SHA1_AUTHENTICATION(16, 48),
        EXT_DATA_CLIENT_GMAC_AUTHENTICATION(17, 48),

        MANAGEMENT_CLIENT_NO_AUTHENTICATION(18, 64),
        MANAGEMENT_CLIENT_LOW_LEVEL_AUTHENTICATION(19, 64),
        //        MANAGEMENT_CLIENT_MANUFACTURER_SPECIFIC_AUTHENTICATION(20, 64),
        MANAGEMENT_CLIENT_MD5_AUTHENTICATION(21, 64),
        MANAGEMENT_CLIENT_SHA1_AUTHENTICATION(22, 64),
        MANAGEMENT_CLIENT_GMAC_AUTHENTICATION(23, 64),

        FIRMWARE_CLIENT_NO_AUTHENTICATION(24, 80),
        FIRMWARE_CLIENT_LOW_LEVEL_AUTHENTICATION(25, 80),
        //        FIRMWARE_CLIENT_MANUFACTURER_SPECIFIC_AUTHENTICATION(26, 80),
        FIRMWARE_CLIENT_MD5_AUTHENTICATION(27, 80),
        FIRMWARE_CLIENT_SHA1_AUTHENTICATION(28, 80),
        FIRMWARE_CLIENT_GMAC_AUTHENTICATION(29, 80),

        MANUFACTURER_CLIENT_NO_AUTHENTICATION(30, 0),
        MANUFACTURER_CLIENT_LOW_LEVEL_AUTHENTICATION(31, 0),
        //        MANUFACTURER_CLIENT_MANUFACTURER_SPECIFIC_AUTHENTICATION(32, 0),
        MANUFACTURER_CLIENT_MD5_AUTHENTICATION(33, 0),
        MANUFACTURER_CLIENT_SHA1_AUTHENTICATION(34, 0),
        MANUFACTURER_CLIENT_GMAC_AUTHENTICATION(35, 0);

        private static final int clientIDMultiple = 16;
        private static final int numberOfLevelsPerClient = 6;

        private final int accessLevel;
        private final int clientId;

        AuthenticationAccessLevelIds(int accessLevel, int clientId) {
            this.accessLevel = accessLevel;
            this.clientId = clientId;
        }

        public static int getClientIdFor(int authenticationDeviceAccessLevel) {
            for (AuthenticationAccessLevelIds authenticationAccessLevelId : values()) {
                if (authenticationAccessLevelId.accessLevel == authenticationDeviceAccessLevel) {
                    return authenticationAccessLevelId.clientId;
                }
            }
            return 0;
        }

        public static int getAuthenticationAccessLevelForClientMacAndOriginalAccessLevel(final int clientId, final int originalAccessLevel) {
            for (AuthenticationAccessLevelIds authenticationAccessLevelId : values()) {
                final int offset = ((authenticationAccessLevelId.clientId / clientIDMultiple) - 1) * numberOfLevelsPerClient;
                if (authenticationAccessLevelId.clientId == clientId && (authenticationAccessLevelId.accessLevel - offset) == originalAccessLevel) {
                    return authenticationAccessLevelId.accessLevel;
                }
            }
            return 0;
        }

        public static int getSimpleAuthenticationAccessLevelForClientMacAndNewAccessLevel(final int clientId, final int newAccessLevel) {
            for (AuthenticationAccessLevelIds authenticationAccessLevelId : values()) {
                final int offset = ((authenticationAccessLevelId.clientId / clientIDMultiple) - 1) * numberOfLevelsPerClient;
                if (authenticationAccessLevelId.clientId == clientId && authenticationAccessLevelId.accessLevel == newAccessLevel) {
                    return authenticationAccessLevelId.accessLevel - offset;
                }
            }
            return 0;
        }

        public int getAccessLevel() {
            return accessLevel;
        }
    }

    protected enum EncryptionAccessLevelIds {

        PUBLIC_CLIENT_NO_MESSAGE_ENCRYPTION(0, 16),
        PUBLIC_CLIENT_MESSAGE_AUTHENTICATION(1, 16),
        PUBLIC_CLIENT_MESSAGE_ENCRYPTION(2, 16),
        PUBLIC_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION(3, 16),

        DATA_CLIENT_NO_MESSAGE_ENCRYPTION(4, 32),
        DATA_CLIENT_MESSAGE_AUTHENTICATION(5, 32),
        DATA_CLIENT_MESSAGE_ENCRYPTION(6, 32),
        DATA_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION(7, 32),

        EXT_DATA_CLIENT_NO_MESSAGE_ENCRYPTION(8, 48),
        EXT_DATA_CLIENT_MESSAGE_AUTHENTICATION(9, 48),
        EXT_DATA_CLIENT_MESSAGE_ENCRYPTION(10, 48),
        EXT_DATA_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION(11, 48),

        MANAGEMENT_CLIENT_NO_MESSAGE_ENCRYPTION(12, 64),
        MANAGEMENT_CLIENT_MESSAGE_AUTHENTICATION(13, 64),
        MANAGEMENT_CLIENT_MESSAGE_ENCRYPTION(14, 64),
        MANAGEMENT_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION(15, 64),

        FIRMWARE_CLIENT_NO_MESSAGE_ENCRYPTION(16, 80),
        FIRMWARE_CLIENT_MESSAGE_AUTHENTICATION(17, 80),
        FIRMWARE_CLIENT_MESSAGE_ENCRYPTION(18, 80),
        FIRMWARE_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION(19, 80),

        MANUFACTURER_CLIENT_NO_MESSAGE_ENCRYPTION(20, 0),
        MANUFACTURER_CLIENT_MESSAGE_AUTHENTICATION(21, 0),
        MANUFACTURER_CLIENT_MESSAGE_ENCRYPTION(22, 0),
        MANUFACTURER_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION(23, 0);

        private static final int clientIDMultiple = 16;
        private static final int numberOfLevelsPerClient = 4;

        private final int accessLevel;
        private final int clientId;

        EncryptionAccessLevelIds(int accessLevel, int clientId) {
            this.accessLevel = accessLevel;
            this.clientId = clientId;
        }

        public static int getEncryptionAccessLevelForClientMacAndOriginalAccessLevel(final int clientId, final int originalAccessLevel) {
            for (EncryptionAccessLevelIds encryptionAccessLevelId : values()) {
                final int offset = ((encryptionAccessLevelId.clientId / clientIDMultiple) - 1) * numberOfLevelsPerClient;
                if (encryptionAccessLevelId.clientId == clientId && (encryptionAccessLevelId.accessLevel - offset) == originalAccessLevel) {
                    return encryptionAccessLevelId.accessLevel;
                }
            }
            return 0;
        }

        public static int getSimpleEncryptionAccessLevelForClientMacAndNewAccessLevel(final int clientId, final int newAccessLevel) {
            for (EncryptionAccessLevelIds encryptionAccessLevelId : values()) {
                final int offset = ((encryptionAccessLevelId.clientId / clientIDMultiple) - 1) * numberOfLevelsPerClient;
                if (encryptionAccessLevelId.clientId == clientId && encryptionAccessLevelId.accessLevel == newAccessLevel) {
                    return encryptionAccessLevelId.accessLevel - offset;
                }
            }
            return 0;
        }

        int getAccessLevel() {
            return this.accessLevel;
        }


    }

    private enum DefaultTranslations {
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_0(authenticationTranslationKeyConstant + "0", "No Authentication Public client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_1(authenticationTranslationKeyConstant + "1", "Low level Authentication Public client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_10(authenticationTranslationKeyConstant + "10", "SHA - 1Authentication DataCollection client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_11(authenticationTranslationKeyConstant + "11", "GMAC Authentication DataCollection client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_12(authenticationTranslationKeyConstant + "12", "No Authentication Extended DataCollection client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_13(authenticationTranslationKeyConstant + "13", "Low level Authentication Extended DataCollection client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_15(authenticationTranslationKeyConstant + "15", "Md5 Authentication Extended DataCollection client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_16(authenticationTranslationKeyConstant + "16", "SHA - 1Authentication Extended DataCollection client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_17(authenticationTranslationKeyConstant + "17", "GMAC Authentication Extended DataCollection client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_18(authenticationTranslationKeyConstant + "18", "No Authentication Management client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_19(authenticationTranslationKeyConstant + "19", "Low level Authentication Management client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_21(authenticationTranslationKeyConstant + "21", "Md5 Authentication Management client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_22(authenticationTranslationKeyConstant + "22", "SHA - 1Authentication Management client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_23(authenticationTranslationKeyConstant + "23", "GMAC Authentication Management client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_24(authenticationTranslationKeyConstant + "24", "No Authentication Firmware client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_25(authenticationTranslationKeyConstant + "25", "Low level Authentication Firmware client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_27(authenticationTranslationKeyConstant + "27", "Md5 Authentication Firmware client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_28(authenticationTranslationKeyConstant + "28", "SHA - 1Authentication Firmware client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_29(authenticationTranslationKeyConstant + "29", "GMAC Authentication Firmware client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_3(authenticationTranslationKeyConstant + "3", "Md5 Authentication Public client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_30(authenticationTranslationKeyConstant + "30", "No Authentication Manufacturer client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_31(authenticationTranslationKeyConstant + "31", "Low level Authentication Manufacturer client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_33(authenticationTranslationKeyConstant + "33", "Md5 Authentication Manufacturer client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_34(authenticationTranslationKeyConstant + "34", "SHA - 1Authentication Manufacturer client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_35(authenticationTranslationKeyConstant + "35", "GMAC Authentication Manufacturer client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_4(authenticationTranslationKeyConstant + "4", "SHA - 1Authentication Public client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_5(authenticationTranslationKeyConstant + "5", "GMAC Authentication Public client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_6(authenticationTranslationKeyConstant + "6", "No Authentication DataCollection client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_7(authenticationTranslationKeyConstant + "7", "Low level Authentication DataCollection client"),
        DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_9(authenticationTranslationKeyConstant + "9", "Md5 Authentication DataCollection client"),

        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_0(encryptionTranslationKeyConstant + "0", "No Encryption Public client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_1(encryptionTranslationKeyConstant + "1", "Message Encryption Public client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_10(encryptionTranslationKeyConstant + "10", "Message Authentication Extended DataCollection client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_11(encryptionTranslationKeyConstant + "11", "Message Encryption and Authentication Extended DataCollection client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_12(encryptionTranslationKeyConstant + "12", "No Encryption Management client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_13(encryptionTranslationKeyConstant + "13", "Message Encryption Management client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_14(encryptionTranslationKeyConstant + "14", "Message Authentication Management client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_15(encryptionTranslationKeyConstant + "15", "Message Encryption and Authentication Management client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_16(encryptionTranslationKeyConstant + "16", "No Encryption Firmware client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_17(encryptionTranslationKeyConstant + "17", "Message Encryption Firmware client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_18(encryptionTranslationKeyConstant + "18", "Message Authentication Firmware client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_19(encryptionTranslationKeyConstant + "19", "Message Encryption and Authentication Firmware client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_2(encryptionTranslationKeyConstant + "2", "Message Authentication Public client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_20(encryptionTranslationKeyConstant + "20", "No Encryption Manufacturer client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_21(encryptionTranslationKeyConstant + "21", "Message Encryption Manufacturer client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_22(encryptionTranslationKeyConstant + "22", "Message Authentication Manufacturer client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_23(encryptionTranslationKeyConstant + "23", "Message Encryption and Authentication Manufacturer client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_3(encryptionTranslationKeyConstant + "3", "Message Encryption and Authentication Public client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_4(encryptionTranslationKeyConstant + "4", "No Encryption DataCollection client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_5(encryptionTranslationKeyConstant + "5", "Message Encryption DataCollection client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_6(encryptionTranslationKeyConstant + "6", "Message Authentication DataCollection client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_7(encryptionTranslationKeyConstant + "7", "Message Encryption and Authentication DataCollection client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_8(encryptionTranslationKeyConstant + "8", "No Encryption Extended DataCollection client"),
        DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_9(encryptionTranslationKeyConstant + "9", "Message Encryption Extended DataCollection client");

        private final String key;
        private final String defaultTranslation;

        DefaultTranslations(String key, String defaultTranslation) {
            this.key = key;
            this.defaultTranslation = defaultTranslation;
        }

        public static String fromKey(String key) {
            return Stream.of(values())
                    .filter(entry -> entry.getKey().equals(key))
                    .findAny()
                    .map(DefaultTranslations::getDefaultTranslation)
                    .orElse(key);
        }

        public String getKey() {
            return key;
        }

        public String getDefaultTranslation() {
            return defaultTranslation;
        }
    }


    private abstract class AbstractAuthenticationAccessLevel implements AuthenticationDeviceAccessLevel {

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return DefaultTranslations.fromKey(getTranslationKey());
        }
    }

    private abstract class AbstractNoAuthentication extends AbstractAuthenticationAccessLevel {

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }
    }

    protected class NoAuthenticationPublic extends AbstractNoAuthentication {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_NO_AUTHENTICATION.accessLevel;
        }
    }

    protected class NoAuthenticationDataCollection extends AbstractNoAuthentication {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_NO_AUTHENTICATION.accessLevel;
        }
    }

    protected class NoAuthenticationExtendedDataCollection extends AbstractNoAuthentication {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_NO_AUTHENTICATION.accessLevel;
        }
    }

    protected class NoAuthenticationManagement extends AbstractNoAuthentication {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_NO_AUTHENTICATION.accessLevel;
        }
    }

    protected class NoAuthenticationFirmware extends AbstractNoAuthentication {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_NO_AUTHENTICATION.accessLevel;
        }
    }

    protected class NoAuthenticationManufacturer extends AbstractNoAuthentication {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_NO_AUTHENTICATION.accessLevel;
        }
    }

    protected class LowLevelAuthenticationPublic extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordPublicPropertySpec());
        }
    }

    protected class LowLevelAuthenticationData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordDataPropertySpec());
        }
    }

    protected class LowLevelAuthenticationExtendedData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordExtDataPropertySpec());
        }
    }

    protected class LowLevelAuthenticationManagement extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordManagementPropertySpec());
        }
    }

    protected class LowLevelAuthenticationFirmware extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordFirmwarePropertySpec());
        }
    }

    protected class LowLevelAuthenticationManufacturer extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordManufacturerPropertySpec());
        }
    }

    protected class Md5AuthenticationPublic extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordPublicPropertySpec());
        }
    }

    protected class Md5AuthenticationData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordDataPropertySpec());
        }
    }

    protected class Md5AuthenticationExtendedData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordExtDataPropertySpec());
        }
    }

    protected class Md5AuthenticationManagement extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordManagementPropertySpec());
        }
    }

    protected class Md5AuthenticationFirmware extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordFirmwarePropertySpec());
        }
    }

    protected class Md5AuthenticationManufacturer extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordManufacturerPropertySpec());
        }
    }

    protected class ShaAuthenticationPublic extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordPublicPropertySpec());
        }
    }

    protected class ShaAuthenticationData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordDataPropertySpec());
        }
    }

    protected class ShaAuthenticationExtendedData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordExtDataPropertySpec());
        }
    }

    protected class ShaAuthenticationManagement extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordManagementPropertySpec());
        }
    }

    protected class ShaAuthenticationFirmware extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordFirmwarePropertySpec());
        }
    }

    protected class ShaAuthenticationManufacturer extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordManufacturerPropertySpec());
        }
    }

    protected class GmacAuthenticationPublic extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_GMAC_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getPasswordPublicPropertySpec(),
                    getEncryptionKeyPublicPropertySpec(),
                    getAuthenticationKeyPublicPropertySpec());
        }
    }

    protected class GmacAuthenticationData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_GMAC_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getPasswordDataPropertySpec(),
                    getEncryptionKeyDataPropertySpec(),
                    getAuthenticationKeyDataPropertySpec());
        }
    }

    protected class GmacAuthenticationExtendedData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_GMAC_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getPasswordExtDataPropertySpec(),
                    getEncryptionKeyExtDataPropertySpec(),
                    getAuthenticationKeyExtDataPropertySpec());
        }
    }

    protected class GmacAuthenticationManagement extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_GMAC_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getPasswordManagementPropertySpec(),
                    getEncryptionKeyManagementPropertySpec(),
                    getAuthenticationKeyManagementPropertySpec());
        }
    }

    protected class GmacAuthenticationFirmware extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_GMAC_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getPasswordFirmwarePropertySpec(),
                    getEncryptionKeyFirmwarePropertySpec(),
                    getAuthenticationKeyFirmwarePropertySpec());
        }
    }

    protected class GmacAuthenticationManufacturer extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_GMAC_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getPasswordManufacturerPropertySpec(),
                    getEncryptionKeyManufacturerPropertySpec(),
                    getAuthenticationKeyManufacturerPropertySpec());
        }
    }

    private abstract class AbstractEncryptionAccessLevel implements EncryptionDeviceAccessLevel {

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
        }

        @Override
        public String getDefaultTranslation() {
            return DefaultTranslations.fromKey(getTranslationKey());
        }
    }

    private abstract class NoPropertiesEncryptionAccessLevel extends AbstractEncryptionAccessLevel {

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return new ArrayList<>();
        }
    }

    protected class NoEncryptionPublic extends NoPropertiesEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.PUBLIC_CLIENT_NO_MESSAGE_ENCRYPTION.getAccessLevel();
        }
    }

    protected class NoEncryptionData extends NoPropertiesEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.DATA_CLIENT_NO_MESSAGE_ENCRYPTION.getAccessLevel();
        }
    }

    protected class NoEncryptionExtendedData extends NoPropertiesEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.EXT_DATA_CLIENT_NO_MESSAGE_ENCRYPTION.getAccessLevel();
        }
    }

    protected class NoEncryptionManagement extends NoPropertiesEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MANAGEMENT_CLIENT_NO_MESSAGE_ENCRYPTION.getAccessLevel();
        }
    }

    protected class NoEncryptionFirmware extends NoPropertiesEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.FIRMWARE_CLIENT_NO_MESSAGE_ENCRYPTION.getAccessLevel();
        }
    }

    protected class NoEncryptionManufacturer extends NoPropertiesEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MANUFACTURER_CLIENT_NO_MESSAGE_ENCRYPTION.getAccessLevel();
        }
    }

    protected class MessageAuthenticationPublic extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.PUBLIC_CLIENT_MESSAGE_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyPublicPropertySpec(),
                    getAuthenticationKeyPublicPropertySpec()
            );
        }
    }

    protected class MessageAuthenticationData extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.DATA_CLIENT_MESSAGE_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyDataPropertySpec(),
                    getAuthenticationKeyDataPropertySpec()
            );
        }
    }

    protected class MessageAuthenticationExtendedData extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.EXT_DATA_CLIENT_MESSAGE_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyExtDataPropertySpec(),
                    getAuthenticationKeyExtDataPropertySpec()
            );
        }
    }

    protected class MessageAuthenticationManagement extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MANAGEMENT_CLIENT_MESSAGE_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyManagementPropertySpec(),
                    getAuthenticationKeyManagementPropertySpec()
            );
        }
    }

    protected class MessageAuthenticationFirmware extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.FIRMWARE_CLIENT_MESSAGE_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyFirmwarePropertySpec(),
                    getAuthenticationKeyFirmwarePropertySpec()
            );
        }
    }

    protected class MessageAuthenticationManufacturer extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MANUFACTURER_CLIENT_MESSAGE_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyManufacturerPropertySpec(),
                    getAuthenticationKeyManufacturerPropertySpec()
            );
        }
    }

    protected class MessageEncryptionPublic extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.PUBLIC_CLIENT_MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyPublicPropertySpec(),
                    getAuthenticationKeyPublicPropertySpec()
            );
        }
    }

    protected class MessageEncryptionData extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.DATA_CLIENT_MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyDataPropertySpec(),
                    getAuthenticationKeyDataPropertySpec()
            );
        }
    }

    protected class MessageEncryptionExtendedData extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.EXT_DATA_CLIENT_MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyExtDataPropertySpec(),
                    getAuthenticationKeyExtDataPropertySpec()
            );
        }
    }

    protected class MessageEncryptionManagement extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MANAGEMENT_CLIENT_MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyManagementPropertySpec(),
                    getAuthenticationKeyManagementPropertySpec()
            );
        }
    }

    protected class MessageEncryptionFirmware extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.FIRMWARE_CLIENT_MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyFirmwarePropertySpec(),
                    getAuthenticationKeyFirmwarePropertySpec()
            );
        }
    }

    protected class MessageEncryptionManufacturer extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MANUFACTURER_CLIENT_MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyManufacturerPropertySpec(),
                    getAuthenticationKeyManufacturerPropertySpec()
            );
        }
    }

    protected class MessageEncryptionAndAuthenticationPublic extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.PUBLIC_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyPublicPropertySpec(),
                    getAuthenticationKeyPublicPropertySpec()
            );
        }
    }

    protected class MessageEncryptionAndAuthenticationData extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.DATA_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyDataPropertySpec(),
                    getAuthenticationKeyDataPropertySpec()
            );
        }
    }

    protected class MessageEncryptionAndAuthenticationExtendedData extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.EXT_DATA_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyExtDataPropertySpec(),
                    getAuthenticationKeyExtDataPropertySpec()
            );
        }
    }

    protected class MessageEncryptionAndAuthenticationManagement extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MANAGEMENT_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyManagementPropertySpec(),
                    getAuthenticationKeyManagementPropertySpec()
            );
        }
    }

    protected class MessageEncryptionAndAuthenticationFirmware extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.FIRMWARE_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyFirmwarePropertySpec(),
                    getAuthenticationKeyFirmwarePropertySpec()
            );
        }
    }

    protected class MessageEncryptionAndAuthenticationManufacturer extends AbstractEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MANUFACTURER_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    getEncryptionKeyManufacturerPropertySpec(),
                    getAuthenticationKeyManufacturerPropertySpec()
            );
        }
    }
}