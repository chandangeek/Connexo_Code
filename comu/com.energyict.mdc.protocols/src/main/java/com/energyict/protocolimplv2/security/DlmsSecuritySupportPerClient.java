package com.energyict.protocolimplv2.security;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.EncryptedStringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.LegacySecurityPropertyConverter;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for a DLMS protocol, which
 * has a SecurityObject for each possible client.
 * <p>
 * Copyrights EnergyICT
 * Date: 18/06/13
 * Time: 15:02
 */
public class DlmsSecuritySupportPerClient implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    @Inject
    public DlmsSecuritySupportPerClient(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
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

        private int getAccessLevel() {
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
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_PUBLIC.toString())
                .markRequired()
                .finish();
    }

    private PropertySpec getEncryptionKeyDataPropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_DATA.toString())
                .markRequired()
                .finish();
    }

    private PropertySpec getEncryptionKeyExtDataPropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_EXT_DATA.toString())
                .markRequired()
                .finish();
    }

    private PropertySpec getEncryptionKeyManagementPropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_MANAGEMENT.toString())
                .markRequired()
                .finish();
    }

    private PropertySpec getEncryptionKeyFirmwarePropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_FIRMWARE.toString())
                .markRequired()
                .finish();
    }

    private PropertySpec getEncryptionKeyManufacturerPropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER.toString())
                .markRequired()
                .finish();
    }

    private PropertySpec getAuthenticationKeyPublicPropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_PUBLIC.toString())
                .markRequired()
                .finish();
    }

    private PropertySpec getAuthenticationKeyDataPropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_DATA.toString())
                .markRequired()
                .finish();
    }

    private PropertySpec getAuthenticationKeyExtDataPropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_EXT_DATA.toString())
                .markRequired()
                .finish();
    }

    private PropertySpec getAuthenticationKeyManagementPropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_MANAGEMENT.toString())
                .markRequired()
                .finish();
    }

    private PropertySpec getAuthenticationKeyFirmwarePropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_FIRMWARE.toString())
                .markRequired()
                .finish();
    }

    private PropertySpec getAuthenticationKeyManufacturerPropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.AUTHENTICATION_KEY_MANUFACTURER.toString())
                .markRequired()
                .finish();
    }

    private PropertySpec getPasswordPublicPropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.PASSWORD_PUBLIC.toString())
                .markRequired()
                .finish();
    }

    private PropertySpec getPasswordDataPropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.PASSWORD_DATA.toString())
                .markRequired()
                .finish();
    }

    private PropertySpec getPasswordExtDataPropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.PASSWORD_EXT_DATA.toString())
                .markRequired()
                .finish();
    }

    private PropertySpec getPasswordManagementPropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.PASSWORD_MANAGEMENT.toString())
                .markRequired()
                .finish();
    }

    private PropertySpec getPasswordFirmwarePropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.PASSWORD_FIRMWARE.toString())
                .markRequired()
                .finish();
    }

    private PropertySpec getPasswordManufacturerPropertySpec() {
        return this.propertySpecService.newPropertySpecBuilder(EncryptedStringFactory.class)
                .name(SecurityPropertySpecName.PASSWORD_MANUFACTURER.toString())
                .markRequired()
                .finish();
    }

    @Override
    public List<PropertySpec> getSecurityPropertySpecs() {
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
                SecurityPropertySpecName.AUTHENTICATION_KEY_MANAGEMENT.toString(), SecurityPropertySpecName.AUTHENTICATION_KEY_FIRMWARE.toString(), SecurityPropertySpecName.AUTHENTICATION_KEY_MANUFACTURER
                .toString());
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
                }
                else {
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
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
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
            }
            else {
                return ((BigDecimal) clientMacAddress).intValue();
            }
        }
        else {
            return 16;  // returning the public client if none was configured
        }
    }

    private int getEncryptionLevel(String securityLevelProperty) {
        String encryptionLevel = securityLevelProperty.substring(securityLevelProperty.indexOf(':') + 1);
        try {
            return Integer.parseInt(encryptionLevel);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Failed to extract EncryptionDeviceAccessLevel from SecurityProperty '%s': %s could not be converted to int",
                    securityLevelProperty, encryptionLevel));
        }
    }

    private int getAuthenticationLevel(String securityLevelProperty) {
        String authLevel = securityLevelProperty.substring(0, securityLevelProperty.indexOf(':'));
        try {
            return Integer.parseInt(authLevel);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Failed to extract AuthenticationDeviceAccessLevel from SecurityProperty '%s': %s could not be converted to int",
                    securityLevelProperty, authLevel));
        }
    }

    private abstract class AbstractNoAuthentication implements AuthenticationDeviceAccessLevel {

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

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_0).format();
        }
    }

    protected class NoAuthenticationDataCollection extends AbstractNoAuthentication {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_NO_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_6).format();
        }
    }

    protected class NoAuthenticationExtendedDataCollection extends AbstractNoAuthentication {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_NO_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_12).format();
        }
    }

    protected class NoAuthenticationManagement extends AbstractNoAuthentication {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_NO_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_18).format();
        }
    }

    protected class NoAuthenticationFirmware extends AbstractNoAuthentication {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_NO_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_24).format();
        }
    }

    protected class NoAuthenticationManufacturer extends AbstractNoAuthentication {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_NO_AUTHENTICATION.accessLevel;
        }
        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_30).format();
        }
    }

    protected class LowLevelAuthenticationPublic implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordPublicPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_1).format();
        }

    }

    protected class LowLevelAuthenticationData implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordDataPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_7).format();
        }

    }

    protected class LowLevelAuthenticationExtendedData implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordExtDataPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_13).format();
        }

    }

    protected class LowLevelAuthenticationManagement implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordManagementPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_19).format();
        }

    }

    protected class LowLevelAuthenticationFirmware implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordFirmwarePropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_25).format();
        }

    }

    protected class LowLevelAuthenticationManufacturer implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_LOW_LEVEL_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordManufacturerPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_31).format();
        }
    }

    protected class Md5AuthenticationPublic implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordPublicPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_3).format();
        }
    }

    protected class Md5AuthenticationData implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordDataPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_9).format();
        }
    }

    protected class Md5AuthenticationExtendedData implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordExtDataPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_15).format();
        }
    }

    protected class Md5AuthenticationManagement implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordManagementPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_21).format();
        }
    }

    protected class Md5AuthenticationFirmware implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordFirmwarePropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_27).format();
        }
    }

    protected class Md5AuthenticationManufacturer implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_MD5_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordManufacturerPropertySpec());
        }


        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_33).format();
        }
    }

    protected class ShaAuthenticationPublic implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordPublicPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_4).format();
        }
    }

    protected class ShaAuthenticationData implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordDataPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_10).format();
        }
    }

    protected class ShaAuthenticationExtendedData implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordExtDataPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_16).format();
        }
    }

    protected class ShaAuthenticationManagement implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordManagementPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_22).format();
        }
    }

    protected class ShaAuthenticationFirmware implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordFirmwarePropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_28).format();
        }
    }

    protected class ShaAuthenticationManufacturer implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_SHA1_AUTHENTICATION.accessLevel;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(getPasswordManufacturerPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_34).format();
        }
    }

    protected class GmacAuthenticationPublic implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_GMAC_AUTHENTICATION.accessLevel;
        }


        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_5).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getPasswordPublicPropertySpec(),
                    getEncryptionKeyPublicPropertySpec(),
                    getAuthenticationKeyPublicPropertySpec());
        }
    }

    protected class GmacAuthenticationData implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_GMAC_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_11).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getPasswordDataPropertySpec(),
                    getEncryptionKeyDataPropertySpec(),
                    getAuthenticationKeyDataPropertySpec());
        }
    }

    protected class GmacAuthenticationExtendedData implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_GMAC_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_17).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getPasswordExtDataPropertySpec(),
                    getEncryptionKeyExtDataPropertySpec(),
                    getAuthenticationKeyExtDataPropertySpec());
        }
    }

    protected class GmacAuthenticationManagement implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_GMAC_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_23).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getPasswordManagementPropertySpec(),
                    getEncryptionKeyManagementPropertySpec(),
                    getAuthenticationKeyManagementPropertySpec());
        }
    }

    protected class GmacAuthenticationFirmware implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_GMAC_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_29).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getPasswordFirmwarePropertySpec(),
                    getEncryptionKeyFirmwarePropertySpec(),
                    getAuthenticationKeyFirmwarePropertySpec());
        }
    }

    protected class GmacAuthenticationManufacturer implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_GMAC_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_35).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getPasswordManufacturerPropertySpec(),
                    getEncryptionKeyManufacturerPropertySpec(),
                    getAuthenticationKeyManufacturerPropertySpec());
        }
    }

    private abstract class NoPropertiesEncryptionAccessLevel implements EncryptionDeviceAccessLevel {

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

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_0).format();
        }
    }

    protected class NoEncryptionData extends NoPropertiesEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.DATA_CLIENT_NO_MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_4).format();
        }
    }

    protected class NoEncryptionExtendedData extends NoPropertiesEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.EXT_DATA_CLIENT_NO_MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_8).format();
        }
    }

    protected class NoEncryptionManagement extends NoPropertiesEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MANAGEMENT_CLIENT_NO_MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_12).format();
        }
    }

    protected class NoEncryptionFirmware extends NoPropertiesEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.FIRMWARE_CLIENT_NO_MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_16).format();
        }
    }

    protected class NoEncryptionManufacturer extends NoPropertiesEncryptionAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MANUFACTURER_CLIENT_NO_MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_20).format();
        }
    }

    protected class MessageAuthenticationPublic implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.PUBLIC_CLIENT_MESSAGE_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_2).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyPublicPropertySpec(),
                    getAuthenticationKeyPublicPropertySpec()
            );
        }
    }

    protected class MessageAuthenticationData implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.DATA_CLIENT_MESSAGE_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_6).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyDataPropertySpec(),
                    getAuthenticationKeyDataPropertySpec()
            );
        }
    }

    protected class MessageAuthenticationExtendedData implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.EXT_DATA_CLIENT_MESSAGE_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_10).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyExtDataPropertySpec(),
                    getAuthenticationKeyExtDataPropertySpec()
            );
        }
    }

    protected class MessageAuthenticationManagement implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MANAGEMENT_CLIENT_MESSAGE_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_14).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyManagementPropertySpec(),
                    getAuthenticationKeyManagementPropertySpec()
            );
        }
    }

    protected class MessageAuthenticationFirmware implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.FIRMWARE_CLIENT_MESSAGE_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_18).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyFirmwarePropertySpec(),
                    getAuthenticationKeyFirmwarePropertySpec()
            );
        }
    }

    protected class MessageAuthenticationManufacturer implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MANUFACTURER_CLIENT_MESSAGE_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_22).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyManufacturerPropertySpec(),
                    getAuthenticationKeyManufacturerPropertySpec()
            );
        }
    }

    protected class MessageEncryptionPublic implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.PUBLIC_CLIENT_MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_1).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyPublicPropertySpec(),
                    getAuthenticationKeyPublicPropertySpec()
            );
        }
    }

    protected class MessageEncryptionData implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.DATA_CLIENT_MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_5).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyDataPropertySpec(),
                    getAuthenticationKeyDataPropertySpec()
            );
        }
    }

    protected class MessageEncryptionExtendedData implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.EXT_DATA_CLIENT_MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_9).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyExtDataPropertySpec(),
                    getAuthenticationKeyExtDataPropertySpec()
            );
        }
    }

    protected class MessageEncryptionManagement implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MANAGEMENT_CLIENT_MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_13).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyManagementPropertySpec(),
                    getAuthenticationKeyManagementPropertySpec()
            );
        }
    }

    protected class MessageEncryptionFirmware implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.FIRMWARE_CLIENT_MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_17).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyFirmwarePropertySpec(),
                    getAuthenticationKeyFirmwarePropertySpec()
            );
        }
    }

    protected class MessageEncryptionManufacturer implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MANUFACTURER_CLIENT_MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_21).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyManufacturerPropertySpec(),
                    getAuthenticationKeyManufacturerPropertySpec()
            );
        }
    }

    protected class MessageEncryptionAndAuthenticationPublic implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.PUBLIC_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_3).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyPublicPropertySpec(),
                    getAuthenticationKeyPublicPropertySpec()
            );
        }
    }

    protected class MessageEncryptionAndAuthenticationData implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.DATA_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_7).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyDataPropertySpec(),
                    getAuthenticationKeyDataPropertySpec()
            );
        }
    }

    protected class MessageEncryptionAndAuthenticationExtendedData implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.EXT_DATA_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_11).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyExtDataPropertySpec(),
                    getAuthenticationKeyExtDataPropertySpec()
            );
        }
    }

    protected class MessageEncryptionAndAuthenticationManagement implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MANAGEMENT_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_15).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyManagementPropertySpec(),
                    getAuthenticationKeyManagementPropertySpec()
            );
        }
    }

    protected class MessageEncryptionAndAuthenticationFirmware implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.FIRMWARE_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_19).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.<PropertySpec>asList(
                    getEncryptionKeyFirmwarePropertySpec(),
                    getAuthenticationKeyFirmwarePropertySpec()
            );
        }
    }

    protected class MessageEncryptionAndAuthenticationManufacturer implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MANUFACTURER_CLIENT_MESSAGE_ENCRYPTION_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_ENCRYPTIONLEVEL_23).format();
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
