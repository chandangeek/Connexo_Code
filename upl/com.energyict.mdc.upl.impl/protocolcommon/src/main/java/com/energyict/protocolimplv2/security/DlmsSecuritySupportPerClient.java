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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for a DLMS protocol, which
 * has a SecurityObject for each possible client.
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/06/13
 * Time: 15:02
 */
public class DlmsSecuritySupportPerClient implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    private final String authenticationTranslationKeyConstant = "DlmsSecuritySupportPerClient.authenticationlevel.";
    private final String encryptionTranslationKeyConstant = "DlmsSecuritySupportPerClient.encryptionlevel.";

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

        private AuthenticationAccessLevelIds(int accessLevel, int clientId) {
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

        public static int getAuthenticationAccessLevelForClientMacAndOriginalAccessLevel(final int clientId, final int originalAccessLevel){
            for (AuthenticationAccessLevelIds authenticationAccessLevelId : values()) {
                final int offset = ((authenticationAccessLevelId.clientId / clientIDMultiple) - 1) * numberOfLevelsPerClient;
                if(authenticationAccessLevelId.clientId == clientId && (authenticationAccessLevelId.accessLevel - offset) == originalAccessLevel){
                    return authenticationAccessLevelId.accessLevel;
                }
            }
            return 0;
        }

        public static int getSimpleAuthenticationAccessLevelForClientMacAndNewAccessLevel(final int clientId, final int newAccessLevel){
            for (AuthenticationAccessLevelIds authenticationAccessLevelId : values()) {
                final int offset = ((authenticationAccessLevelId.clientId / clientIDMultiple) - 1) * numberOfLevelsPerClient;
                if(authenticationAccessLevelId.clientId == clientId && authenticationAccessLevelId.accessLevel == newAccessLevel){
                    return authenticationAccessLevelId.accessLevel - offset;
                }
            }
            return 0;
        }
    }

    protected enum EncryptionAccessLevelIds {

        PUBLIC_CLIENT_NO_MESSAGE_ENCRYPTION(0, 16),
        PUBLIC_CLIENT_MESSAGE_ENCRYPTION(1, 16),
        PUBLIC_CLIENT_MESSAGE_AUTHENTICATION(2, 16),
        PUBLIC_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION(3, 16),

        DATA_CLIENT_NO_MESSAGE_ENCRYPTION(4, 32),
        DATA_CLIENT_MESSAGE_ENCRYPTION(5, 32),
        DATA_CLIENT_MESSAGE_AUTHENTICATION(6, 32),
        DATA_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION(7, 32),

        EXT_DATA_CLIENT_NO_MESSAGE_ENCRYPTION(8, 48),
        EXT_DATA_CLIENT_MESSAGE_ENCRYPTION(9, 48),
        EXT_DATA_CLIENT_MESSAGE_AUTHENTICATION(10, 48),
        EXT_DATA_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION(11, 48),

        MANAGEMENT_CLIENT_NO_MESSAGE_ENCRYPTION(12, 64),
        MANAGEMENT_CLIENT_MESSAGE_ENCRYPTION(13, 64),
        MANAGEMENT_CLIENT_MESSAGE_AUTHENTICATION(14, 64),
        MANAGEMENT_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION(15, 64),

        FIRMWARE_CLIENT_NO_MESSAGE_ENCRYPTION(16, 80),
        FIRMWARE_CLIENT_MESSAGE_ENCRYPTION(17, 80),
        FIRMWARE_CLIENT_MESSAGE_AUTHENTICATION(18, 80),
        FIRMWARE_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION(19, 80),

        MANUFACTURER_CLIENT_NO_MESSAGE_ENCRYPTION(20, 0),
        MANUFACTURER_CLIENT_MESSAGE_ENCRYPTION(21, 0),
        MANUFACTURER_CLIENT_MESSAGE_AUTHENTICATION(22, 0),
        MANUFACTURER_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION(23, 0);

        private static final int clientIDMultiple = 16;
        private static final int numberOfLevelsPerClient = 4;

        private final int accessLevel;
        private final int clientId;

        private EncryptionAccessLevelIds(int accessLevel, int clientId) {
            this.accessLevel = accessLevel;
            this.clientId = clientId;
        }

        protected int getAccessLevel() {
            return this.accessLevel;
        }

        public static int getEncryptionAccessLevelForClientMacAndOriginalAccessLevel(final int clientId, final int originalAccessLevel){
            for (EncryptionAccessLevelIds encryptionAccessLevelId : values()) {
                final int offset = ((encryptionAccessLevelId.clientId / clientIDMultiple) - 1) * numberOfLevelsPerClient;
                if(encryptionAccessLevelId.clientId == clientId && (encryptionAccessLevelId.accessLevel - offset) == originalAccessLevel){
                    return encryptionAccessLevelId.accessLevel;
                }
            }
            return 0;
        }

        public static int getSimpleEncryptionAccessLevelForClientMacAndNewAccessLevel(final int clientId, final int newAccessLevel){
            for (EncryptionAccessLevelIds encryptionAccessLevelId : values()) {
                final int offset = ((encryptionAccessLevelId.clientId / clientIDMultiple) - 1) * numberOfLevelsPerClient;
                if(encryptionAccessLevelId.clientId == clientId && encryptionAccessLevelId.accessLevel == newAccessLevel){
                    return encryptionAccessLevelId.accessLevel - offset;
                }
            }
            return 0;
        }


    }

    private PropertySpec<String> getEncryptionKeyPublicPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_PUBLIC.toString())
                .finish();
    }

    private PropertySpec<String> getEncryptionKeyDataPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_DATA.toString())
                .finish();
    }

    private PropertySpec<String> getEncryptionKeyExtDataPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_EXT_DATA.toString())
                .finish();
    }

    private PropertySpec<String> getEncryptionKeyManagementPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_MANAGEMENT.toString())
                .finish();
    }

    private PropertySpec<String> getEncryptionKeyFirmwarePropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_FIRMWARE.toString())
                .finish();
    }

    private PropertySpec<String> getEncryptionKeyManufacturerPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER.toString())
                .finish();
    }

    private PropertySpec<String> getAuthenticationKeyPublicPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_PUBLIC.toString())
                .finish();
    }

    private PropertySpec<String> getAuthenticationKeyDataPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_DATA.toString())
                .finish();
    }

    private PropertySpec<String> getAuthenticationKeyExtDataPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_EXT_DATA.toString())
                .finish();
    }

    private PropertySpec<String> getAuthenticationKeyManagementPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_MANAGEMENT.toString())
                .finish();
    }

    private PropertySpec<String> getAuthenticationKeyFirmwarePropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_FIRMWARE.toString())
                .finish();
    }

    private PropertySpec<String> getAuthenticationKeyManufacturerPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_MANUFACTURER.toString())
                .finish();
    }

    private PropertySpec<String> getPasswordPublicPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.PASSWORD_PUBLIC.toString())
                .finish();
    }

    private PropertySpec<String> getPasswordDataPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.PASSWORD_DATA.toString())
                .finish();
    }

    private PropertySpec<String> getPasswordExtDataPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.PASSWORD_EXT_DATA.toString())
                .finish();
    }

    private PropertySpec<String> getPasswordManagementPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.PASSWORD_MANAGEMENT.toString())
                .finish();
    }

    private PropertySpec<String> getPasswordFirmwarePropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.PASSWORD_FIRMWARE.toString())
                .finish();
    }

    private PropertySpec<String> getPasswordManufacturerPropertySpec() {
        return PropertySpecBuilder
                .forClass(String.class, new EncryptedStringFactory())
                .name(SecurityPropertySpecName.PASSWORD_MANUFACTURER.toString())
                .finish();
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.<PropertySpec>asList(
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
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.DLMS_SECURITY_PER_CLIENT.toString();
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
            convertToProperPassword(deviceProtocolSecurityPropertySet, typedProperties);

            final int clientId = AuthenticationAccessLevelIds.getClientIdFor(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel());
            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME,
                    AuthenticationAccessLevelIds.getSimpleAuthenticationAccessLevelForClientMacAndNewAccessLevel(clientId, deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()) +
                            ":" +
                            EncryptionAccessLevelIds.getSimpleEncryptionAccessLevelForClientMacAndNewAccessLevel(clientId, deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()));
            typedProperties.setProperty("DataTransportEncryptionKey",
                    getDataTransportEncryptionKeyPropertyValue(deviceProtocolSecurityPropertySet));
            typedProperties.setProperty("DataTransportAuthenticationKey",
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

    private Object getValueFrom(final List<String> propertyNames, final DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet){
        for (String propertyName : propertyNames) {
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(propertyName);
            if( property != null){
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
        boolean found = false;
        for (String passWordPropertyName : passWordPropertyNames) {
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(passWordPropertyName);
            if( property != null && !found){
                if (Password.class.isAssignableFrom(property.getClass())) {
                    typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), ((Password) property).getValue());
                } else {
                    typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), property);
                }
                found = true;
            }
        }
        if(!found){
            typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), new Password(""));
        }
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        String securityLevelProperty = typedProperties.getStringProperty(SECURITY_LEVEL_PROPERTY_NAME);
        if (securityLevelProperty == null) {
            securityLevelProperty = "0:0";
        }
        if (!securityLevelProperty.contains(":")) {
            throw new IllegalStateException("Cannot convert TypedProperties: expected property " + SECURITY_LEVEL_PROPERTY_NAME + " to have format '<auth>:<encryption>', but found " + securityLevelProperty);
        }
        final int clientMacAddressValue = getClientMacAddressValue(typedProperties);

        final int authenticationLevelPropertyValue = AuthenticationAccessLevelIds.getAuthenticationAccessLevelForClientMacAndOriginalAccessLevel(clientMacAddressValue,  getAuthenticationLevel(securityLevelProperty));
        final int encryptionLevelPropertyValue = EncryptionAccessLevelIds.getEncryptionAccessLevelForClientMacAndOriginalAccessLevel(clientMacAddressValue,  getEncryptionLevel(securityLevelProperty));
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
            if(String.class.isAssignableFrom(clientMacAddress.getClass())){
                return Integer.valueOf((String) clientMacAddress);
            } else {
                return ((BigDecimal)clientMacAddress).intValue();
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
        public List<PropertySpec> getSecurityProperties() {
            return new ArrayList<>();
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordPublicPropertySpec());
        }
    }

    protected class LowLevelAuthenticationData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordDataPropertySpec());
        }
    }

    protected class LowLevelAuthenticationExtendedData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordExtDataPropertySpec());
        }
    }

    protected class LowLevelAuthenticationManagement extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordManagementPropertySpec());
        }
    }

    protected class LowLevelAuthenticationFirmware extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordFirmwarePropertySpec());
        }
    }

    protected class LowLevelAuthenticationManufacturer extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordManufacturerPropertySpec());
        }
    }

    protected class Md5AuthenticationPublic extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordPublicPropertySpec());
        }
    }

    protected class Md5AuthenticationData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordDataPropertySpec());
        }
    }

    protected class Md5AuthenticationExtendedData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordExtDataPropertySpec());
        }
    }

    protected class Md5AuthenticationManagement extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordManagementPropertySpec());
        }
    }

    protected class Md5AuthenticationFirmware extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordFirmwarePropertySpec());
        }
    }

    protected class Md5AuthenticationManufacturer extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordManufacturerPropertySpec());
        }
    }

    protected class ShaAuthenticationPublic extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordPublicPropertySpec());
        }
    }

    protected class ShaAuthenticationData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordDataPropertySpec());
        }
    }

    protected class ShaAuthenticationExtendedData extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordExtDataPropertySpec());
        }
    }

    protected class ShaAuthenticationManagement extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordManagementPropertySpec());
        }
    }

    protected class ShaAuthenticationFirmware extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordFirmwarePropertySpec());
        }
    }

    protected class ShaAuthenticationManufacturer extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordManufacturerPropertySpec());
        }
    }

    protected class GmacAuthenticationPublic extends AbstractAuthenticationAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_GMAC_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
    }

    private abstract class NoPropertiesEncryptionAccessLevel extends AbstractEncryptionAccessLevel {

        @Override
        public List<PropertySpec> getSecurityProperties() {
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
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
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyManufacturerPropertySpec(),
                    getAuthenticationKeyManufacturerPropertySpec()
            );
        }
    }
}
