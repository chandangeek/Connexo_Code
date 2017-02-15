/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.LegacySecurityPropertyConverter;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;
import com.energyict.protocols.naming.SecurityPropertySpecName;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    enum AuthenticationAccessLevelIds {

        PUBLIC_CLIENT_NO_AUTHENTICATION(0, 16),
        PUBLIC_CLIENT_LOW_LEVEL_AUTHENTICATION(1, 16),
        PUBLIC_CLIENT_MD5_AUTHENTICATION(3, 16),
        PUBLIC_CLIENT_SHA1_AUTHENTICATION(4, 16),
        PUBLIC_CLIENT_GMAC_AUTHENTICATION(5, 16),

        DATA_CLIENT_NO_AUTHENTICATION(6, 32),
        DATA_CLIENT_LOW_LEVEL_AUTHENTICATION(7, 32),
        DATA_CLIENT_MD5_AUTHENTICATION(9, 32),
        DATA_CLIENT_SHA1_AUTHENTICATION(10, 32),
        DATA_CLIENT_GMAC_AUTHENTICATION(11, 32),

        EXT_DATA_CLIENT_NO_AUTHENTICATION(12, 48),
        EXT_DATA_CLIENT_LOW_LEVEL_AUTHENTICATION(13, 48),
        EXT_DATA_CLIENT_MD5_AUTHENTICATION(15, 48),
        EXT_DATA_CLIENT_SHA1_AUTHENTICATION(16, 48),
        EXT_DATA_CLIENT_GMAC_AUTHENTICATION(17, 48),

        MANAGEMENT_CLIENT_NO_AUTHENTICATION(18, 64),
        MANAGEMENT_CLIENT_LOW_LEVEL_AUTHENTICATION(19, 64),
        MANAGEMENT_CLIENT_MD5_AUTHENTICATION(21, 64),
        MANAGEMENT_CLIENT_SHA1_AUTHENTICATION(22, 64),
        MANAGEMENT_CLIENT_GMAC_AUTHENTICATION(23, 64),

        FIRMWARE_CLIENT_NO_AUTHENTICATION(24, 80),
        FIRMWARE_CLIENT_LOW_LEVEL_AUTHENTICATION(25, 80),
        FIRMWARE_CLIENT_MD5_AUTHENTICATION(27, 80),
        FIRMWARE_CLIENT_SHA1_AUTHENTICATION(28, 80),
        FIRMWARE_CLIENT_GMAC_AUTHENTICATION(29, 80),

        MANUFACTURER_CLIENT_NO_AUTHENTICATION(30, 0),
        MANUFACTURER_CLIENT_LOW_LEVEL_AUTHENTICATION(31, 0),
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

        int getAccessLevel() {
            return accessLevel;
        }

        int getClientId() {
            return clientId;
        }

        static int getClientIdFor(int authenticationDeviceAccessLevel) {
            for (AuthenticationAccessLevelIds authenticationAccessLevelId : values()) {
                if (authenticationAccessLevelId.getAccessLevel() == authenticationDeviceAccessLevel) {
                    return authenticationAccessLevelId.getClientId();
                }
            }
            return 0;
        }

        static int getAuthenticationAccessLevelForClientMacAndOriginalAccessLevel(final int clientId, final int originalAccessLevel) {
            for (AuthenticationAccessLevelIds authenticationAccessLevelId : values()) {
                final int offset = ((authenticationAccessLevelId.getClientId() / clientIDMultiple) - 1) * numberOfLevelsPerClient;
                if (authenticationAccessLevelId.getClientId() == clientId && (authenticationAccessLevelId.getAccessLevel() - offset) == originalAccessLevel) {
                    return authenticationAccessLevelId.getAccessLevel();
                }
            }
            return 0;
        }

        static int getSimpleAuthenticationAccessLevelForClientMacAndNewAccessLevel(final int clientId, final int newAccessLevel) {
            for (AuthenticationAccessLevelIds authenticationAccessLevelId : values()) {
                final int offset = ((authenticationAccessLevelId.getClientId() / clientIDMultiple) - 1) * numberOfLevelsPerClient;
                if (authenticationAccessLevelId.getClientId() == clientId && authenticationAccessLevelId.getAccessLevel() == newAccessLevel) {
                    return authenticationAccessLevelId.getAccessLevel() - offset;
                }
            }
            return 0;
        }
    }

    enum EncryptionAccessLevelIds {

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

        int getClientId() {
            return clientId;
        }

        static int getEncryptionAccessLevelForClientMacAndOriginalAccessLevel(final int clientId, final int originalAccessLevel) {
            for (EncryptionAccessLevelIds encryptionAccessLevelId : values()) {
                final int offset = ((encryptionAccessLevelId.getClientId() / clientIDMultiple) - 1) * numberOfLevelsPerClient;
                if (encryptionAccessLevelId.getClientId() == clientId && (encryptionAccessLevelId.getAccessLevel() - offset) == originalAccessLevel) {
                    return encryptionAccessLevelId.getAccessLevel();
                }
            }
            return 0;
        }

        static int getSimpleEncryptionAccessLevelForClientMacAndNewAccessLevel(final int clientId, final int newAccessLevel) {
            for (EncryptionAccessLevelIds encryptionAccessLevelId : values()) {
                final int offset = ((encryptionAccessLevelId.getClientId() / clientIDMultiple) - 1) * numberOfLevelsPerClient;
                if (encryptionAccessLevelId.getClientId() == clientId && encryptionAccessLevelId.getAccessLevel() == newAccessLevel) {
                    return encryptionAccessLevelId.getAccessLevel() - offset;
                }
            }
            return 0;
        }
    }

    @Override
    public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
        return Optional.of(new DlmsSecurityPerClientCustomPropertySet(this.thesaurus, this.propertySpecService));
    }

    private PropertySpec getEncryptionKeyPublicPropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.PUBLIC_ENCRYPTION_KEY.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getEncryptionKeyDataPropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.DATA_ENCRYPTION_KEY.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getEncryptionKeyExtDataPropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.EXTRA_DATA_ENCRYPTION_KEY.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getEncryptionKeyManagementPropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.MANAGEMENT_ENCRYPTION_KEY.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getEncryptionKeyFirmwarePropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.FIRMWARE_ENCRYPTION_KEY.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getEncryptionKeyManufacturerPropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.MANUFACTURER_ENCRYPTION_KEY.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getAuthenticationKeyPublicPropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.PUBLIC_AUTHENTICATION_KEY.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getAuthenticationKeyDataPropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.DATA_AUTHENTICATION_KEY.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getAuthenticationKeyExtDataPropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.EXTRA_DATA_AUTHENTICATION_KEY.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getAuthenticationKeyManagementPropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.MANAGEMENT_AUTHENTICATION_KEY.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getAuthenticationKeyFirmwarePropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.FIRMWARE_AUTHENTICATION_KEY.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getAuthenticationKeyManufacturerPropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.MANUFACTURER_AUTHENTICATION_KEY.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getPasswordPublicPropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.PUBLIC_PASSWORD.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getPasswordDataPropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.DATA_PASSWORD.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getPasswordExtDataPropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.EXTRA_DATA_PASSWORD.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getPasswordManagementPropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.MANAGEMENT_PASSWORD.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getPasswordFirmwarePropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.FIRMWARE_PASSWORD.propertySpec(this.propertySpecService, this.thesaurus);
    }

    private PropertySpec getPasswordManufacturerPropertySpec() {
        return DlmsSecurityPerClientProperties.ActualFields.MANUFACTURER_PASSWORD.propertySpec(this.propertySpecService, this.thesaurus);
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
            typedProperties.setProperty("DataTransportEncryptionKey",
                    getDataTransportEncryptionKeyPropertyValue(deviceProtocolSecurityPropertySet));
            typedProperties.setProperty("DataTransportAuthenticationKey",
                    getDataTransportAuthenticationKeyPropertyValue(deviceProtocolSecurityPropertySet));
            typedProperties.setProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.getKey(),
                    clientId
            );
        }
        return typedProperties;
    }

    private Object getDataTransportAuthenticationKeyPropertyValue(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        List<String> authenticationKeyNames = Arrays.asList(
                SecurityPropertySpecName.AUTHENTICATION_KEY_PUBLIC.getKey(), SecurityPropertySpecName.AUTHENTICATION_KEY_DATA.getKey(), SecurityPropertySpecName.AUTHENTICATION_KEY_EXT_DATA.getKey(),
                SecurityPropertySpecName.AUTHENTICATION_KEY_MANAGEMENT.getKey(), SecurityPropertySpecName.AUTHENTICATION_KEY_FIRMWARE.getKey(), SecurityPropertySpecName.AUTHENTICATION_KEY_MANUFACTURER
                .getKey());
        return getValueFrom(authenticationKeyNames, deviceProtocolSecurityPropertySet);
    }

    private Object getDataTransportEncryptionKeyPropertyValue(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        List<String> encryptionKeyNames = Arrays.asList(
                SecurityPropertySpecName.ENCRYPTION_KEY_PUBLIC.getKey(), SecurityPropertySpecName.ENCRYPTION_KEY_DATA.getKey(), SecurityPropertySpecName.ENCRYPTION_KEY_EXT_DATA.getKey(),
                SecurityPropertySpecName.ENCRYPTION_KEY_MANAGEMENT.getKey(), SecurityPropertySpecName.ENCRYPTION_KEY_FIRMWARE.getKey(), SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER.getKey());
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
                SecurityPropertySpecName.PASSWORD_PUBLIC.getKey(), SecurityPropertySpecName.PASSWORD_DATA.getKey(), SecurityPropertySpecName.PASSWORD_EXT_DATA.getKey(),
                SecurityPropertySpecName.PASSWORD_MANAGEMENT.getKey(), SecurityPropertySpecName.PASSWORD_FIRMWARE.getKey(), SecurityPropertySpecName.PASSWORD_MANUFACTURER.getKey());
        boolean notFound = true;
        for (String passWordPropertyName : passWordPropertyNames) {
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(passWordPropertyName);
            if (property != null && notFound) {
                if (Password.class.isAssignableFrom(property.getClass())) {
                    typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.getKey(), ((Password) property).getValue());
                }
                else {
                    typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.getKey(), property);
                }
                notFound = false;
            }
        }
        if (notFound) {
            typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.getKey(), new Password(""));
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
        final Object clientMacAddress = typedProperties.getProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.getKey());
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
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_NO_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_0).format();
        }
    }

    protected class NoAuthenticationDataCollection extends AbstractNoAuthentication {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_NO_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_6).format();
        }
    }

    protected class NoAuthenticationExtendedDataCollection extends AbstractNoAuthentication {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_NO_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_12).format();
        }
    }

    protected class NoAuthenticationManagement extends AbstractNoAuthentication {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_NO_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_18).format();
        }
    }

    protected class NoAuthenticationFirmware extends AbstractNoAuthentication {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_NO_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_24).format();
        }
    }

    protected class NoAuthenticationManufacturer extends AbstractNoAuthentication {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_NO_AUTHENTICATION.getAccessLevel();
        }
        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_30).format();
        }
    }

    protected class LowLevelAuthenticationPublic implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_LOW_LEVEL_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordPublicPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_1).format();
        }

    }

    protected class LowLevelAuthenticationData implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_LOW_LEVEL_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordDataPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_7).format();
        }

    }

    protected class LowLevelAuthenticationExtendedData implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_LOW_LEVEL_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordExtDataPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_13).format();
        }

    }

    protected class LowLevelAuthenticationManagement implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_LOW_LEVEL_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordManagementPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_19).format();
        }

    }

    protected class LowLevelAuthenticationFirmware implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_LOW_LEVEL_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordFirmwarePropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_25).format();
        }

    }

    protected class LowLevelAuthenticationManufacturer implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_LOW_LEVEL_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordManufacturerPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_31).format();
        }
    }

    protected class Md5AuthenticationPublic implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_MD5_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordPublicPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_3).format();
        }
    }

    protected class Md5AuthenticationData implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_MD5_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordDataPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_9).format();
        }
    }

    protected class Md5AuthenticationExtendedData implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_MD5_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordExtDataPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_15).format();
        }
    }

    protected class Md5AuthenticationManagement implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_MD5_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordManagementPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_21).format();
        }
    }

    protected class Md5AuthenticationFirmware implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_MD5_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordFirmwarePropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_27).format();
        }
    }

    protected class Md5AuthenticationManufacturer implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_MD5_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordManufacturerPropertySpec());
        }


        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_33).format();
        }
    }

    protected class ShaAuthenticationPublic implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_SHA1_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordPublicPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_4).format();
        }
    }

    protected class ShaAuthenticationData implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.DATA_CLIENT_SHA1_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordDataPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_10).format();
        }
    }

    protected class ShaAuthenticationExtendedData implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_SHA1_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordExtDataPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_16).format();
        }
    }

    protected class ShaAuthenticationManagement implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_SHA1_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordManagementPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_22).format();
        }
    }

    protected class ShaAuthenticationFirmware implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_SHA1_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordFirmwarePropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_28).format();
        }
    }

    protected class ShaAuthenticationManufacturer implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_SHA1_AUTHENTICATION.getAccessLevel();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.singletonList(getPasswordManufacturerPropertySpec());
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_34).format();
        }
    }

    protected class GmacAuthenticationPublic implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.PUBLIC_CLIENT_GMAC_AUTHENTICATION.getAccessLevel();
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
            return AuthenticationAccessLevelIds.DATA_CLIENT_GMAC_AUTHENTICATION.getAccessLevel();
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
            return AuthenticationAccessLevelIds.EXT_DATA_CLIENT_GMAC_AUTHENTICATION.getAccessLevel();
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
            return AuthenticationAccessLevelIds.MANAGEMENT_CLIENT_GMAC_AUTHENTICATION.getAccessLevel();
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
            return AuthenticationAccessLevelIds.FIRMWARE_CLIENT_GMAC_AUTHENTICATION.getAccessLevel();
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
            return AuthenticationAccessLevelIds.MANUFACTURER_CLIENT_GMAC_AUTHENTICATION.getAccessLevel();
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
