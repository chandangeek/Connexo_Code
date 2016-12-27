package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.LegacyDeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import com.energyict.cbo.Password;
import com.energyict.cpo.MdwToUplPropertySpecAdapter;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecBuilder;
import com.energyict.cpo.TypedProperties;
import com.energyict.dynamicattributes.EncryptedStringFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for a DLMS protocol, which
 * has a SecurityObject for each possible client.
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/06/13
 * Time: 15:02
 */
public class DlmsSecuritySupportPerClient implements LegacyDeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    private static final String authenticationTranslationKeyConstant = "DlmsSecuritySupportPerClient.authenticationlevel.";
    private static final String encryptionTranslationKeyConstant = "DlmsSecuritySupportPerClient.encryptionlevel.";

    private static final String DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME = "DataTransportEncryptionKey";
    private static final String DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME = "DataTransportAuthenticationKey";

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

        public int getAccessLevel() {
            return accessLevel;
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

        int getAccessLevel() {
            return this.accessLevel;
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


    }

    private PropertySpec getEncryptionKeyPublicPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_PUBLIC.toString())
                .finish();
    }

    private PropertySpec getEncryptionKeyDataPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_DATA.toString())
                .finish();
    }

    private PropertySpec getEncryptionKeyExtDataPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_EXT_DATA.toString())
                .finish();
    }

    private PropertySpec getEncryptionKeyManagementPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_MANAGEMENT.toString())
                .finish();
    }

    private PropertySpec getEncryptionKeyFirmwarePropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_FIRMWARE.toString())
                .finish();
    }

    private PropertySpec getEncryptionKeyManufacturerPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER.toString())
                .finish();
    }

    private PropertySpec getAuthenticationKeyPublicPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_PUBLIC.toString())
                .finish();
    }

    private PropertySpec getAuthenticationKeyDataPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_DATA.toString())
                .finish();
    }

    private PropertySpec getAuthenticationKeyExtDataPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_EXT_DATA.toString())
                .finish();
    }

    private PropertySpec getAuthenticationKeyManagementPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_MANAGEMENT.toString())
                .finish();
    }

    private PropertySpec getAuthenticationKeyFirmwarePropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_FIRMWARE.toString())
                .finish();
    }

    private PropertySpec getAuthenticationKeyManufacturerPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_MANUFACTURER.toString())
                .finish();
    }

    private PropertySpec getPasswordPublicPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.PASSWORD_PUBLIC.toString())
                .finish();
    }

    private PropertySpec getPasswordDataPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.PASSWORD_DATA.toString())
                .finish();
    }

    private PropertySpec getPasswordExtDataPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.PASSWORD_EXT_DATA.toString())
                .finish();
    }

    private PropertySpec getPasswordManagementPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.PASSWORD_MANAGEMENT.toString())
                .finish();
    }

    private PropertySpec getPasswordFirmwarePropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.PASSWORD_FIRMWARE.toString())
                .finish();
    }

    private PropertySpec getPasswordManufacturerPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.PASSWORD_MANUFACTURER.toString())
                .finish();
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyDataPropertySpec()),
                MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyExtDataPropertySpec()),
                MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyFirmwarePropertySpec()),
                MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyManagementPropertySpec()),
                MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyManufacturerPropertySpec()),
                MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyPublicPropertySpec()),
                MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyDataPropertySpec()),
                MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyExtDataPropertySpec()),
                MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyFirmwarePropertySpec()),
                MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyManagementPropertySpec()),
                MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyManufacturerPropertySpec()),
                MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyPublicPropertySpec()),
                MdwToUplPropertySpecAdapter.adapt(getPasswordDataPropertySpec()),
                MdwToUplPropertySpecAdapter.adapt(getPasswordExtDataPropertySpec()),
                MdwToUplPropertySpecAdapter.adapt(getPasswordFirmwarePropertySpec()),
                MdwToUplPropertySpecAdapter.adapt(getPasswordManagementPropertySpec()),
                MdwToUplPropertySpecAdapter.adapt(getPasswordManufacturerPropertySpec()),
                MdwToUplPropertySpecAdapter.adapt(getPasswordPublicPropertySpec())
        );
    }

    @Override
    public List<String> getLegacySecurityProperties() {
        return Arrays.asList(
                SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(),
                DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME,
                SECURITY_LEVEL_PROPERTY_NAME,
                DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME
        );
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
        TypedProperties typedProperties = TypedProperties.empty();
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
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
            typedProperties.setProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(),
                    clientId
            );
        }
        return typedProperties;
    }

    private Object getDataTransportAuthenticationKeyPropertyValue(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        List<String> authenticationKeyNames = Arrays.asList(
                SecurityPropertySpecName.AUTHENTICATION_KEY_PUBLIC.toString(), SecurityPropertySpecName.AUTHENTICATION_KEY_DATA.toString(), SecurityPropertySpecName.AUTHENTICATION_KEY_EXT_DATA.toString(),
                SecurityPropertySpecName.AUTHENTICATION_KEY_MANAGEMENT.toString(), SecurityPropertySpecName.AUTHENTICATION_KEY_FIRMWARE.toString(), SecurityPropertySpecName.AUTHENTICATION_KEY_MANUFACTURER.toString());
        return getValueFrom(authenticationKeyNames, deviceProtocolSecurityPropertySet);
    }

    private Object getDataTransportEncryptionKeyPropertyValue(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        List<String> encryptionKeyNames = Arrays.asList(
                SecurityPropertySpecName.ENCRYPTION_KEY_PUBLIC.toString(), SecurityPropertySpecName.ENCRYPTION_KEY_DATA.toString(), SecurityPropertySpecName.ENCRYPTION_KEY_EXT_DATA.toString(),
                SecurityPropertySpecName.ENCRYPTION_KEY_MANAGEMENT.toString(), SecurityPropertySpecName.ENCRYPTION_KEY_FIRMWARE.toString(), SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER.toString());
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
        // override the password (as it is provided as a Password object instead of a String
        List<String> passWordPropertyNames = Arrays.asList(
                SecurityPropertySpecName.PASSWORD_PUBLIC.toString(), SecurityPropertySpecName.PASSWORD_DATA.toString(), SecurityPropertySpecName.PASSWORD_EXT_DATA.toString(),
                SecurityPropertySpecName.PASSWORD_MANAGEMENT.toString(), SecurityPropertySpecName.PASSWORD_FIRMWARE.toString(), SecurityPropertySpecName.PASSWORD_MANUFACTURER.toString());
        boolean notFound = true;
        for (String passWordPropertyName : passWordPropertyNames) {
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(passWordPropertyName);
            if (property != null && notFound) {
                if (Password.class.isAssignableFrom(property.getClass())) {
                    typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), ((Password) property).getValue());
                } else {
                    typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), property);
                }
                notFound = false;
            }
        }
        if (notFound) {
            typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), new Password(""));
        }
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        return this.convertFromTypedProperties((TypedProperties) typedProperties);
    }

    private DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        String securityLevelProperty = typedProperties.getStringProperty(SECURITY_LEVEL_PROPERTY_NAME);
        if (securityLevelProperty == null) {
            securityLevelProperty = "0:0";
        }
        if (!securityLevelProperty.contains(":")) {
            throw new IllegalStateException("Cannot convert TypedProperties: expected property " + SECURITY_LEVEL_PROPERTY_NAME + " to have format '<auth>:<encryption>', but found " + securityLevelProperty);
        }
        final int clientMacAddressValue = getClientMacAddressValue(typedProperties);

        final int authenticationLevelPropertyValue = AuthenticationAccessLevelIds.getAuthenticationAccessLevelForClientMacAndOriginalAccessLevel(clientMacAddressValue, getAuthenticationLevel(securityLevelProperty));
        final int encryptionLevelPropertyValue = EncryptionAccessLevelIds.getEncryptionAccessLevelForClientMacAndOriginalAccessLevel(clientMacAddressValue, getEncryptionLevel(securityLevelProperty));
        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, authenticationLevelPropertyValue, getAuthenticationAccessLevels()));
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, encryptionLevelPropertyValue, getEncryptionAccessLevels()));

        return new DeviceProtocolSecurityPropertySet() {
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
        final Object clientMacAddress = typedProperties.getProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString());
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

    private abstract class AbstractAuthenticationAccessLevel implements AuthenticationDeviceAccessLevel {

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
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
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordPublicPropertySpec()));
        }
    }

    protected class LowLevelAuthenticationData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordDataPropertySpec()));
        }
    }

    protected class LowLevelAuthenticationExtendedData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordExtDataPropertySpec()));
        }
    }

    protected class LowLevelAuthenticationManagement extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordManagementPropertySpec()));
        }
    }

    protected class LowLevelAuthenticationFirmware extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordFirmwarePropertySpec()));
        }
    }

    protected class LowLevelAuthenticationManufacturer extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordManufacturerPropertySpec()));
        }
    }

    protected class Md5AuthenticationPublic extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordPublicPropertySpec()));
        }
    }

    protected class Md5AuthenticationData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordDataPropertySpec()));
        }
    }

    protected class Md5AuthenticationExtendedData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordExtDataPropertySpec()));
        }
    }

    protected class Md5AuthenticationManagement extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordManagementPropertySpec()));
        }
    }

    protected class Md5AuthenticationFirmware extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordFirmwarePropertySpec()));
        }
    }

    protected class Md5AuthenticationManufacturer extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordManufacturerPropertySpec()));
        }
    }

    protected class ShaAuthenticationPublic extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordPublicPropertySpec()));
        }
    }

    protected class ShaAuthenticationData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordDataPropertySpec()));
        }
    }

    protected class ShaAuthenticationExtendedData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordExtDataPropertySpec()));
        }
    }

    protected class ShaAuthenticationManagement extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordManagementPropertySpec()));
        }
    }

    protected class ShaAuthenticationFirmware extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordFirmwarePropertySpec()));
        }
    }

    protected class ShaAuthenticationManufacturer extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getPasswordManufacturerPropertySpec()));
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
                    MdwToUplPropertySpecAdapter.adapt(getPasswordPublicPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyPublicPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyPublicPropertySpec()));
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
                    MdwToUplPropertySpecAdapter.adapt(getPasswordDataPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyDataPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyDataPropertySpec()));
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
                    MdwToUplPropertySpecAdapter.adapt(getPasswordExtDataPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyExtDataPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyExtDataPropertySpec()));
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
                    MdwToUplPropertySpecAdapter.adapt(getPasswordManagementPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyManagementPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyManagementPropertySpec()));
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
                    MdwToUplPropertySpecAdapter.adapt(getPasswordFirmwarePropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyFirmwarePropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyFirmwarePropertySpec()));
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
                    MdwToUplPropertySpecAdapter.adapt(getPasswordManufacturerPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyManufacturerPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyManufacturerPropertySpec()));
        }
    }

    private abstract class AbstractEncryptionAccessLevel implements EncryptionDeviceAccessLevel {

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant + getId();
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyPublicPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyPublicPropertySpec())
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyDataPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyDataPropertySpec())
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyExtDataPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyExtDataPropertySpec())
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyManagementPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyManagementPropertySpec())
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyFirmwarePropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyFirmwarePropertySpec())
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyManufacturerPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyManufacturerPropertySpec())
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyPublicPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyPublicPropertySpec())
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyDataPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyDataPropertySpec())
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyExtDataPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyExtDataPropertySpec())
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyManagementPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyManagementPropertySpec())
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyFirmwarePropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyFirmwarePropertySpec())
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyManufacturerPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyManufacturerPropertySpec())
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyPublicPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyPublicPropertySpec())
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyDataPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyDataPropertySpec())
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyExtDataPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyExtDataPropertySpec())
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyManagementPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyManagementPropertySpec())
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyFirmwarePropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyFirmwarePropertySpec())
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
                    MdwToUplPropertySpecAdapter.adapt(getEncryptionKeyManufacturerPropertySpec()),
                    MdwToUplPropertySpecAdapter.adapt(getAuthenticationKeyManufacturerPropertySpec())
            );
        }
    }
}
