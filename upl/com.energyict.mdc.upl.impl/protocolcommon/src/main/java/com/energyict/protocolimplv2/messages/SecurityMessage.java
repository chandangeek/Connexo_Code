package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.protocolimplv2.messages.enums.ClientSecuritySetup;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.UserNames;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


/**
 * Provides a summary of all <i>Security</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/03/13
 * Time: 15:18
 */
public enum SecurityMessage implements DeviceMessageSpec {

    /**
     * Note that this message will write the security_policy of the SecuritySetup object, DLMS version 0.
     * It is not forwards compatible with DLMS version 1.
     */
    ACTIVATE_DLMS_ENCRYPTION(0, PropertySpecFactory.stringPropertySpecWithValues(
            DeviceMessageConstants.encryptionLevelAttributeName,
            DlmsEncryptionLevelMessageValues.getNames())),
    CHANGE_DLMS_AUTHENTICATION_LEVEL(1,
            PropertySpecFactory.stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS(2,
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newEncryptionKeyAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newWrappedEncryptionKeyAttributeName)
    ),
    CHANGE_CLIENT_PASSWORDS(3,
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newReadingClientPasswordAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newManagementClientPasswordAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newFirmwareClientPasswordAttributeName)
    ),
    WRITE_PSK(4, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.pskAttributeName)),
    CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY(5, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newEncryptionKeyAttributeName)),
    CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS(6,
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newAuthenticationKeyAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newWrappedAuthenticationKeyAttributeName)
    ),
    CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY(7, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newAuthenticationKeyAttributeName)),
    CHANGE_PASSWORD(8),
    CHANGE_PASSWORD_WITH_NEW_PASSWORD(9, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newPasswordAttributeName)),   //ASCII password
    CHANGE_LLS_SECRET(10),
    CHANGE_LLS_SECRET_HEX(11, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newHexPasswordAttributeName)),          //Password value parsed by protocols as hex string
    @Deprecated
    /**
     * For backwards compatibility
     */
            CHANGE_HLS_SECRET(12),
    CHANGE_HLS_SECRET_HEX(13, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newHexPasswordAttributeName)),          //Password value parsed by protocols as hex string
    ACTIVATE_DEACTIVATE_TEMPORARY_ENCRYPTION_KEY(14,
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(
                    DeviceMessageConstants.keyTActivationStatusAttributeName,
                    KeyTUsage.ENABLE.getDescription(),
                    KeyTUsage.getAllDescriptions()
            ),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.SecurityTimeDurationAttributeName, new BigDecimal(0), new BigDecimal(255))),
    CHANGE_EXECUTION_KEY(15, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.executionKeyAttributeName)),
    CHANGE_TEMPORARY_KEY(16, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.temporaryKeyAttributeName)),
    BREAK_OR_RESTORE_SEALS(17,
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.eventLogResetSealAttributeName, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.restoreFactorySettingsSealAttributeName, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.restoreDefaultSettingsSealAttributeName, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.statusChangeSealAttributeName, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.remoteConversionParametersConfigSealAttributeName, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.remoteAnalysisParametersConfigSealAttributeName, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.downloadProgramSealAttributeName, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.restoreDefaultPasswordSealAttributeName, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions())),
    TEMPORARY_BREAK_SEALS(18,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.eventLogResetSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.restoreFactorySettingsSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.restoreDefaultSettingsSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.statusChangeSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.remoteConversionParametersConfigSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.remoteAnalysisParametersConfigSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.downloadProgramSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.restoreDefaultPasswordSealBreakTimeAttributeName, new BigDecimal(0))),
    GENERATE_NEW_PUBLIC_KEY(19),
    GENERATE_NEW_PUBLIC_KEY_FROM_RANDOM(20, PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.randomBytesAttributeName)),
    SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP(21, PropertySpecFactory.groupReferencePropertySpec(DeviceMessageConstants.deviceGroupAttributeName)),
    DISABLE_DLMS_AUTHENTICATION_LEVEL_P0(22,
            PropertySpecFactory.stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    DISABLE_DLMS_AUTHENTICATION_LEVEL_P3(23,
            PropertySpecFactory.stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    ENABLE_DLMS_AUTHENTICATION_LEVEL_P0(24,
            PropertySpecFactory.stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    ENABLE_DLMS_AUTHENTICATION_LEVEL_P3(25,
            PropertySpecFactory.stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    CHANGE_HLS_SECRET_USING_SERVICE_KEY(26,
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.preparedDataAttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.signatureAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.verificationKeyAttributeName)
    ),
    CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY(27,
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.preparedDataAttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.signatureAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.verificationKeyAttributeName)
    ),
    CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY(28,
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.preparedDataAttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.signatureAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.verificationKeyAttributeName)
    ),
    CHANGE_WEBPORTAL_PASSWORD1(29, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newPasswordAttributeName)),  //ASCII password
    CHANGE_WEBPORTAL_PASSWORD2(30, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newPasswordAttributeName)),
    CHANGE_HLS_SECRET_PASSWORD(31, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newPasswordAttributeName)), //Password field
    CHANGE_SECURITY_KEYS(32,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.clientMacAddress),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.masterKey),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newAuthenticationKeyAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newEncryptionKeyAttributeName)
    ),
    /**
     * Note that this message will write the security_policy of the SecuritySetup object, DLMS version 1.
     * It is not backwards compatible with DLMS version 0.
     */
    ACTIVATE_DLMS_SECURITY_VERSION1(33,
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.authenticatedRequestsAttributeName),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.encryptedRequestsAttributeName),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.signedRequestsAttributeName),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.authenticatedResponsesAttributeName),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.encryptedResponsesAttributeName),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.signedResponsesAttributeName)
    ),
    /**
     * Used to agree on one or more symmetric keys using the key
     * agreement algorithm as specified by the security suite. In the case of
     * suites 1 and 2 the ECDH key agreement algorithm is used with the
     * Ephemeral Unified Model C(2e, 0s, ECC CDH) scheme.
     */
    AGREE_NEW_ENCRYPTION_KEY(34),
    AGREE_NEW_AUTHENTICATION_KEY(35),
    CHANGE_SECURITY_SUITE(36,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.securitySuiteAttributeName, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(2))
    ),
    EXPORT_END_DEVICE_CERTIFICATE(37,
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.certificateTypeAttributeName, CertificateType.getPossibleValues())
    ),
    EXPORT_SUB_CA_CERTIFICATES(38),
    EXPORT_ROOT_CA_CERTIFICATE(39),

    DELETE_CERTIFICATE_BY_TYPE(41,
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.certificateEntityAttributeName, CertificateEntity.getPossibleValues()),
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.certificateTypeAttributeName, CertificateType.getPossibleValues()),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.commonNameAttributeName)
    ),
    DELETE_CERTIFICATE_BY_SERIAL_NUMBER(42,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.meterSerialNumberAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.certificateIssuerAttributeName)
    ),
    GENERATE_KEY_PAIR(43,
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.certificateTypeAttributeName, CertificateType.getPossibleValues())
    ),
    GENERATE_CSR(44,
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.certificateTypeAttributeName, CertificateType.getPossibleValues())
    ),
    CHANGE_WEBPORTAL_PASSWORD(45,
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.usernameAttributeName, UserNames.getAllNames()),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.passwordAttributeName)
    ),
    IMPORT_CA_CERTIFICATE(40,
            //Referring to an entry in the peristed trust store
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.CACertificateAliasAttributeName)
    ),
    IMPORT_CLIENT_END_DEVICE_CERTIFICATE(46,
            //Referring to an entry in the persisted key store
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.clientCertificateAliasAttributeName)
    ),
    IMPORT_SERVER_END_DEVICE_CERTIFICATE(47,
            //Referring to a certificateWrapper
            PropertySpecFactory.positiveDecimalPropertySpec(DeviceMessageConstants.certificateWrapperIdAttributeName)
    ),
    CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY(48,
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.preparedDataAttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.signatureAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.verificationKeyAttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.newAuthenticationKeyAttributeName)
    ),
    CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY(49,
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.preparedDataAttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.signatureAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.verificationKeyAttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.newEncryptionKeyAttributeName)
    ),
    CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS_FOR_CLIENT(50,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.clientMacAddress, BigDecimal.valueOf(1)),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newAuthenticationKeyAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newWrappedAuthenticationKeyAttributeName)
    ),
    CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS_FOR_CLIENT(51,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.clientMacAddress, BigDecimal.valueOf(1)),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newEncryptionKeyAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newWrappedEncryptionKeyAttributeName)
    ),
    CHANGE_HLS_SECRET_PASSWORD_FOR_CLIENT(52,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.clientMacAddress, BigDecimal.valueOf(1)),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newPasswordAttributeName)
    ),
    CHANGE_MASTER_KEY_WITH_NEW_KEYS(53,
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newMasterKeyAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newWrappedMasterKeyAttributeName)
    ),

    CHANGE_MASTER_KEY_WITH_NEW_KEYS_FOR_CLIENT(54,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.clientMacAddress, BigDecimal.valueOf(1)),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newMasterKeyAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newWrappedMasterKeyAttributeName)
    ),
    CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS_FOR_PREDEFINED_CLIENT(55,
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.client, getClients()),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newAuthenticationKeyAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newWrappedAuthenticationKeyAttributeName)
    ),
    CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS_FOR_PREDEFINED_CLIENT(56,
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.client, getClients()),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newEncryptionKeyAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newWrappedEncryptionKeyAttributeName)
    ),
    CHANGE_MASTER_KEY_WITH_NEW_KEYS_FOR_PREDEFINED_CLIENT(57,
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.client, getClients()),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newMasterKeyAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newWrappedMasterKeyAttributeName)
    ),
    SET_REQUIRED_PROTECTION_FOR_DATA_PROTECTION_SETUP(58, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.requiredProtection)),
    ;

    private static final DeviceMessageCategory securityCategory = DeviceMessageCategories.SECURITY;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    SecurityMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return securityCategory;
    }

    @Override
    public String getName() {
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return SecurityMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.deviceMessagePropertySpecs;
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }

    @Override
    public int getMessageId() {
        return id;
    }

    public enum CertificateEntity {
        Server(0),
        Client(1),
        CertificationAuthority(2),
        Other(3),
        Invalid(-1);

        int id;

        CertificateEntity(int id) {
            this.id = id;
        }

        public static String[] getPossibleValues() {
            return new String[]{Server.name(), Client.name(), CertificationAuthority.name()};
        }

        public static CertificateEntity fromName(String name) {
            for (CertificateEntity certificateEntity : values()) {
                if (certificateEntity.name().equals(name)) {
                    return certificateEntity;
                }
            }
            return Invalid;
        }

        public static CertificateEntity fromId(int id) {
            for (CertificateEntity certificateEntity : values()) {
                if (certificateEntity.getId() == id) {
                    return certificateEntity;
                }
            }
            return Invalid;
        }

        public int getId() {
            return id;
        }
    }

    public enum CertificateType {
        DigitalSignature(0),
        KeyAgreement(1),
        TLS(2),
        Other(3),
        Invalid(-1);

        int id;

        CertificateType(int id) {
            this.id = id;
        }

        public static String[] getPossibleValues() {
            return new String[]{DigitalSignature.name(), KeyAgreement.name(), TLS.name()};
        }

        public static CertificateType fromName(String name) {
            for (CertificateType certificateType : values()) {
                if (certificateType.name().equals(name)) {
                    return certificateType;
                }
            }
            return Invalid;
        }

        public static CertificateType fromId(int id) {
            for (CertificateType certificateType : values()) {
                if (certificateType.getId() == id) {
                    return certificateType;
                }
            }
            return Invalid;
        }

        public int getId() {
            return id;
        }
    }

    public enum SealActions {

        UNCHANGED(null, "Unchanged"),
        ENABLE_SEAL(true, "Enable seal"),
        DISABLE_SEAL(false, "Disable seal");

        private final Boolean action;
        private final String description;

        SealActions(Boolean action, String description) {
            this.action = action;
            this.description = description;
        }

        public static Boolean fromDescription(String description) {
            for (SealActions actions : values()) {
                if (actions.getDescription().equals(description)) {
                    return actions.getAction();
                }
            }
            return null;
        }

        public static String[] getAllDescriptions() {
            String[] result = new String[values().length];
            for (int index = 0; index < values().length; index++) {
                result[index] = values()[index].getDescription();
            }
            return result;
        }

        public String getDescription() {
            return description;
        }

        public Boolean getAction() {
            return action;
        }
    }

    public enum KeyTUsage {

        DISABLE(false, "Disabled"),
        ENABLE(true, "Enabled");

        private final boolean status;
        private final String description;

        KeyTUsage(boolean status, String description) {
            this.status = status;
            this.description = description;
        }

        public static Boolean fromDescription(String description) {
            for (KeyTUsage usage : values()) {
                if (usage.getDescription().equals(description)) {
                    return usage.getStatus();
                }
            }
            return null;
        }

        public static String[] getAllDescriptions() {
            String[] result = new String[values().length];
            for (int index = 0; index < values().length; index++) {
                result[index] = values()[index].getDescription();
            }
            return result;
        }

        public String getDescription() {
            return description;
        }

        public boolean getStatus() {
            return status;
        }
    }

    public enum KeyID {
        GLOBAL_UNICAST_ENCRYPTION_KEY(0),
        GLOBAL_BROADCAST_ENCRYPTION_KEY(1),
        AUTHENTICATION_KEY(2),
        MASTER_KEY(3);

        private final int id;

        private KeyID(int id) {
            this.id = id;
        }

        public static String[] getKeyId() {
            KeyID[] keyID = values();
            String[] result = new String[keyID.length];
            for (int index = 0; index < keyID.length; index++) {
                result[index] = keyID[index].name();
            }
            return result;
        }

        public int getId() {
            return id;
        }
    }

    public static String[] getClients(){
        return ClientSecuritySetup.getClients();
    }
}
