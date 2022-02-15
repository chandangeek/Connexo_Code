package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceGroup;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.KeyAccessorType;
import com.energyict.protocolimplv2.messages.enums.ClientSecuritySetup;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.EI67KeyExchangeObjects;
import com.energyict.protocolimplv2.messages.enums.UserNames;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;


/**
 * Provides a summary of all <i>Security</i> related messages.
 * Remark: If the message has a property spec of type reference<KeyAccessorType>, unless otherwise stated the new key value is taken from the <b>passive</b>
 * value of the KeyAccessor of the Device.
 * <p>
 * <p>
 * Copyrights EnergyICT
 * Date: 13/03/13
 * Time: 15:18
 */
public enum SecurityMessage implements DeviceMessageSpecSupplier {

    /**
     * Note that this message will write the security_policy of the SecuritySetup object, DLMS version 0.
     * It is not forwards compatible with DLMS version 1.
     */
    ACTIVATE_DLMS_ENCRYPTION(7001, "Activate encryption") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.stringSpec(
                            service,
                            DeviceMessageConstants.encryptionLevelAttributeName, DeviceMessageConstants.encryptionLevelAttributeDefaultTranslation,
                            DlmsEncryptionLevelMessageValues.getNames()));
        }
    },
    CHANGE_DLMS_AUTHENTICATION_LEVEL(7002, "Change authentication level") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.stringSpec(
                            service,
                            DeviceMessageConstants.authenticationLevelAttributeName, DeviceMessageConstants.authenticationLevelAttributeDefaultTranslation,
                            DlmsAuthenticationLevelMessageValues.getNames()));
        }
    },
    CHANGE_CLIENT_PASSWORDS(7004, "Change client passwords") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newReadingClientPasswordAttributeName, DeviceMessageConstants.newReadingClientPasswordAttributeDefaultTranslation),
                    this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newManagementClientPasswordAttributeName, DeviceMessageConstants.newManagementClientPasswordAttributeDefaultTranslation),
                    this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newFirmwareClientPasswordAttributeName, DeviceMessageConstants.newFirmwareClientPasswordAttributeDefaultTranslation)
            );
        }
    },
    WRITE_PSK(7005, "Write PSK") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.pskAttributeName, DeviceMessageConstants.pskAttributeDefaultTranslation));
        }
    },
    CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY(7006, "Change encryption key with value") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newEncryptionKeyAttributeName, DeviceMessageConstants.newEncryptionKeyAttributeDefaultTranslation));
        }
    },
    CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY(7008, "Change authentication key with value") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newAuthenticationKeyAttributeName, DeviceMessageConstants.newAuthenticationKeyAttributeDefaultTranslation));
        }
    },
    CHANGE_PASSWORD(7009, "Change password") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    CHANGE_PASSWORD_WITH_NEW_PASSWORD(7010, "Change password with value") {  // ASCII password

        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newPasswordAttributeName, DeviceMessageConstants.newPasswordAttributeDefaultTranslation));
        }
    },
    CHANGE_LLS_SECRET(7011, "Change LLS secret") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    CHANGE_LLS_SECRET_HEX(7012, "Change LLS secret with value") { //Password value parsed by protocols as hex string

        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newHexPasswordAttributeName, DeviceMessageConstants.newHexPasswordAttributeDefaultTranslation));
        }
    },
    @Deprecated
    /**
     * For backwards compatibility
     */
            CHANGE_HLS_SECRET(7013, "Change HLS secret") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    CHANGE_HLS_SECRET_HEX(7014, "Change HLS secret with value") { //Password value parsed by protocols as hex string

        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newHexPasswordAttributeName, DeviceMessageConstants.newHexPasswordAttributeDefaultTranslation));
        }
    },
    ACTIVATE_DEACTIVATE_TEMPORARY_ENCRYPTION_KEY(7015, "Enable/disable temporary encryption key") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.keyTActivationStatusAttributeName, DeviceMessageConstants.keyTActivationStatusAttributeDefaultTranslation)
                            .setDefaultValue(KeyTUsage.ENABLE.getDescription())
                            .addValues(KeyTUsage.getAllDescriptions())
                            .finish(),
                    this.boundedBigDecimalSpec(
                            service,
                            DeviceMessageConstants.SecurityTimeDurationAttributeName, DeviceMessageConstants.SecurityTimeDurationAttributeDefaultTranslation,
                            BigDecimal.ZERO, new BigDecimal(255))
            );
        }
    },
    CHANGE_EXECUTION_KEY(7016, "Change execution key") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.executionKeyAttributeName, DeviceMessageConstants.executionKeyAttributeDefaultTranslation));
        }
    },
    CHANGE_TEMPORARY_KEY(7017, "Change temporary key") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.temporaryKeyAttributeName, DeviceMessageConstants.temporaryKeyAttributeDefaultTranslation));
        }
    },
    BREAK_OR_RESTORE_SEALS(7018, "Break or restore seals") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.eventLogResetSealAttributeName, DeviceMessageConstants.eventLogResetSealAttributeDefaultTranslation)
                            .setDefaultValue(SealActions.UNCHANGED.getDescription())
                            .addValues(SealActions.getAllDescriptions())
                            .finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.restoreFactorySettingsSealAttributeName, DeviceMessageConstants.restoreFactorySettingsSealAttributeDefaultTranslation)
                            .setDefaultValue(SealActions.UNCHANGED.getDescription())
                            .addValues(SealActions.getAllDescriptions())
                            .finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.restoreDefaultSettingsSealAttributeName, DeviceMessageConstants.restoreDefaultSettingsSealAttributeDefaultTranslation)
                            .setDefaultValue(SealActions.UNCHANGED.getDescription())
                            .addValues(SealActions.getAllDescriptions())
                            .finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.statusChangeSealAttributeName, DeviceMessageConstants.statusChangeSealAttributeDefaultTranslation)
                            .setDefaultValue(SealActions.UNCHANGED.getDescription())
                            .addValues(SealActions.getAllDescriptions())
                            .finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.remoteConversionParametersConfigSealAttributeName, DeviceMessageConstants.remoteConversionParametersConfigSealAttributeDefaultTranslation)
                            .setDefaultValue(SealActions.UNCHANGED.getDescription())
                            .addValues(SealActions.getAllDescriptions())
                            .finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.remoteAnalysisParametersConfigSealAttributeName, DeviceMessageConstants.remoteAnalysisParametersConfigSealAttributeDefaultTranslation)
                            .setDefaultValue(SealActions.UNCHANGED.getDescription())
                            .addValues(SealActions.getAllDescriptions())
                            .finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.downloadProgramSealAttributeName, DeviceMessageConstants.downloadProgramSealAttributeDefaultTranslation)
                            .setDefaultValue(SealActions.UNCHANGED.getDescription())
                            .addValues(SealActions.getAllDescriptions())
                            .finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.restoreDefaultPasswordSealAttributeName, DeviceMessageConstants.restoreDefaultPasswordSealAttributeDefaultTranslation)
                            .setDefaultValue(SealActions.UNCHANGED.getDescription())
                            .addValues(SealActions.getAllDescriptions())
                            .finish()
            );
        }
    },
    TEMPORARY_BREAK_SEALS(7019, "Temporary break the seals") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.eventLogResetSealBreakTimeAttributeName, DeviceMessageConstants.eventLogResetSealBreakTimeAttributeDefaultTranslation, BigDecimal.ZERO),
                    this.bigDecimalSpec(service, DeviceMessageConstants.restoreFactorySettingsSealBreakTimeAttributeName, DeviceMessageConstants.restoreFactorySettingsSealBreakTimeAttributeDefaultTranslation, BigDecimal.ZERO),
                    this.bigDecimalSpec(service, DeviceMessageConstants.restoreDefaultSettingsSealBreakTimeAttributeName, DeviceMessageConstants.restoreDefaultSettingsSealBreakTimeAttributeDefaultTranslation, BigDecimal.ZERO),
                    this.bigDecimalSpec(service, DeviceMessageConstants.statusChangeSealBreakTimeAttributeName, DeviceMessageConstants.statusChangeSealBreakTimeAttributeDefaultTranslation, BigDecimal.ZERO),
                    this.bigDecimalSpec(service, DeviceMessageConstants.remoteConversionParametersConfigSealBreakTimeAttributeName, DeviceMessageConstants.remoteConversionParametersConfigSealBreakTimeAttributeDefaultTranslation, BigDecimal.ZERO),
                    this.bigDecimalSpec(service, DeviceMessageConstants.remoteAnalysisParametersConfigSealBreakTimeAttributeName, DeviceMessageConstants.remoteAnalysisParametersConfigSealBreakTimeAttributeDefaultTranslation, BigDecimal.ZERO),
                    this.bigDecimalSpec(service, DeviceMessageConstants.downloadProgramSealBreakTimeAttributeName, DeviceMessageConstants.downloadProgramSealBreakTimeAttributeDefaultTranslation, BigDecimal.ZERO),
                    this.bigDecimalSpec(service, DeviceMessageConstants.restoreDefaultPasswordSealBreakTimeAttributeName, DeviceMessageConstants.restoreDefaultPasswordSealBreakTimeAttributeDefaultTranslation, BigDecimal.ZERO)
            );
        }
    },
    GENERATE_NEW_PUBLIC_KEY(7020, "Generate new public key") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    GENERATE_NEW_PUBLIC_KEY_FROM_RANDOM(7021, "Generate new public key from random value") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.hexStringSpec(service, DeviceMessageConstants.randomBytesAttributeName, DeviceMessageConstants.randomBytesAttributeDefaultTranslation));
        }
    },
    SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP(7022, "Set public key of aggregation group") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceGroupSpec(service, DeviceMessageConstants.deviceGroupAttributeName, DeviceMessageConstants.deviceGroupAttributeDefaultTranslation));
        }
    },
    DISABLE_DLMS_AUTHENTICATION_LEVEL_P0(7023, "Disable authentication level P0") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.stringSpec(
                            service,
                            DeviceMessageConstants.authenticationLevelAttributeName, DeviceMessageConstants.authenticationLevelAttributeDefaultTranslation,
                            DlmsAuthenticationLevelMessageValues.getNames()));
        }
    },
    DISABLE_DLMS_AUTHENTICATION_LEVEL_P3(7035, "Disable authentication level P3") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.stringSpec(
                            service,
                            DeviceMessageConstants.authenticationLevelAttributeName, DeviceMessageConstants.authenticationLevelAttributeDefaultTranslation,
                            DlmsAuthenticationLevelMessageValues.getNames()));
        }
    },
    ENABLE_DLMS_AUTHENTICATION_LEVEL_P0(7025, "Enable authentication level P0") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.stringSpec(
                            service,
                            DeviceMessageConstants.authenticationLevelAttributeName, DeviceMessageConstants.authenticationLevelAttributeDefaultTranslation,
                            DlmsAuthenticationLevelMessageValues.getNames()));
        }
    },
    ENABLE_DLMS_AUTHENTICATION_LEVEL_P3(7036, "Enable authentication level P3") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.stringSpec(
                            service,
                            DeviceMessageConstants.authenticationLevelAttributeName, DeviceMessageConstants.authenticationLevelAttributeDefaultTranslation,
                            DlmsAuthenticationLevelMessageValues.getNames()));
        }
    },
    CHANGE_HLS_SECRET_USING_SERVICE_KEY(7027, "Change HLS secret using service key") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.hexStringSpec(service, DeviceMessageConstants.preparedDataAttributeName, DeviceMessageConstants.preparedDataAttributeDefaultTranslation),
                    this.hexStringSpec(service, DeviceMessageConstants.signatureAttributeName, DeviceMessageConstants.signatureAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.verificationKeyAttributeName, DeviceMessageConstants.verificationKeyAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY(7028, "Change authentication key using service key") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.hexStringSpec(service, DeviceMessageConstants.preparedDataAttributeName, DeviceMessageConstants.preparedDataAttributeDefaultTranslation),
                    this.hexStringSpec(service, DeviceMessageConstants.signatureAttributeName, DeviceMessageConstants.signatureAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.verificationKeyAttributeName, DeviceMessageConstants.verificationKeyAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY(7029, "Change encryption key using service key") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.hexStringSpec(service, DeviceMessageConstants.preparedDataAttributeName, DeviceMessageConstants.preparedDataAttributeDefaultTranslation),
                    this.hexStringSpec(service, DeviceMessageConstants.signatureAttributeName, DeviceMessageConstants.signatureAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.verificationKeyAttributeName, DeviceMessageConstants.verificationKeyAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_WEBPORTAL_PASSWORD1(7049, "Change webportal password 1") {    //ASCII password

        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newPasswordAttributeName, DeviceMessageConstants.newPasswordAttributeDefaultTranslation));
        }
    },
    CHANGE_WEBPORTAL_PASSWORD2(7032, "Change webportal password 2") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newPasswordAttributeName, DeviceMessageConstants.newPasswordAttributeDefaultTranslation));
        }
    },
    CHANGE_HLS_SECRET_PASSWORD(7030, "Change HLS secret") {    //Password field

        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newPasswordAttributeName, DeviceMessageConstants.newPasswordAttributeDefaultTranslation));
        }
    },
    CHANGE_SECURITY_KEYS(7037, "Change security keys") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpecBuilder(service, DeviceMessageConstants.clientMacAddress, DeviceMessageConstants.clientMacAddressDefaultTranslation).finish(),
                    this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newAuthenticationKeyAttributeName, DeviceMessageConstants.newAuthenticationKeyAttributeDefaultTranslation),
                    this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newEncryptionKeyAttributeName, DeviceMessageConstants.newEncryptionKeyAttributeDefaultTranslation)
            );
        }
    },
    /**
     * Note that this message will write the security_policy of the SecuritySetup object, DLMS version 1.
     * It is not backwards compatible with DLMS version 0.
     */
    ACTIVATE_DLMS_SECURITY_VERSION1(7038, "Activate advanced security") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.booleanSpec(service, DeviceMessageConstants.authenticatedRequestsAttributeName, DeviceMessageConstants.authenticatedRequestsAttributeDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.encryptedRequestsAttributeName, DeviceMessageConstants.encryptedRequestsAttributeDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.signedRequestsAttributeName, DeviceMessageConstants.signedRequestsAttributeDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.authenticatedResponsesAttributeName, DeviceMessageConstants.authenticatedResponsesAttributeDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.encryptedResponsesAttributeName, DeviceMessageConstants.encryptedResponsesAttributeDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.signedResponsesAttributeName, DeviceMessageConstants.signedResponsesAttributeDefaultTranslation)
            );
        }
    },
    /**
     * Used to agree on one or more symmetric keys using the key
     * agreement algorithm as specified by the security suite. In the case of
     * suites 1 and 2 the ECDH key agreement algorithm is used with the
     * Ephemeral Unified Model C(2e, 0s, ECC CDH) scheme.
     */
    AGREE_NEW_ENCRYPTION_KEY(7039, "Agree on new encryption key") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    AGREE_NEW_AUTHENTICATION_KEY(7040, "Agree on new authentication key") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    CHANGE_SECURITY_SUITE(7041, "Change security suite") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.bigDecimalSpecBuilder(
                            service,
                            DeviceMessageConstants.securitySuiteAttributeName, DeviceMessageConstants.securitySuiteAttributeDefaultTranslation)
                            .addValues(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(2))
                            .markExhaustive()
                            .finish());
        }
    },
    EXPORT_END_DEVICE_CERTIFICATE(7042, "Export certificate of the end device") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.stringSpec(
                            service,
                            DeviceMessageConstants.certificateTypeAttributeName, DeviceMessageConstants.certificateTypeAttributeDefaultTranslation,
                            CertificateType.getPossibleValues()));
        }
    },
    EXPORT_SUB_CA_CERTIFICATES(7043, "Export sub-CA certificates") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    EXPORT_ROOT_CA_CERTIFICATE(7044, "Export root-CA certificate") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },

    DELETE_CERTIFICATE_BY_TYPE(7045, "Delete certificate by type") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.certificateEntityAttributeName, DeviceMessageConstants.certificateEntityAttributeDefaultTranslation, CertificateEntity.getPossibleValues()),
                    this.stringSpec(service, DeviceMessageConstants.certificateTypeAttributeName, DeviceMessageConstants.certificateTypeAttributeDefaultTranslation, CertificateType.getPossibleValues()),
                    this.stringSpec(service, DeviceMessageConstants.commonNameAttributeName, DeviceMessageConstants.commonNameAttributeDefaultTranslation)
            );
        }
    },
    DELETE_CERTIFICATE_BY_SERIAL_NUMBER(7046, "Delete certificate by serial number") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.meterSerialNumberAttributeName, DeviceMessageConstants.meterSerialNumberAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.certificateIssuerAttributeName, DeviceMessageConstants.certificateIssuerAttributeDefaultTranslation));
        }
    },
    GENERATE_KEY_PAIR(7047, "Generate EC key pair") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.stringSpec(
                            service,
                            DeviceMessageConstants.certificateTypeAttributeName, DeviceMessageConstants.certificateTypeAttributeDefaultTranslation,
                            CertificateType.getPossibleValues()));
        }
    },
    GENERATE_CSR(7048, "Generate certificate signing request") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.stringSpec(
                            service,
                            DeviceMessageConstants.certificateTypeAttributeName, DeviceMessageConstants.certificateTypeAttributeDefaultTranslation,
                            CertificateType.getPossibleValues()));
        }
    },
    CHANGE_WEBPORTAL_PASSWORD(7031, "Change webportal password") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.usernameAttributeName, DeviceMessageConstants.usernameAttributeDefaultTranslation, UserNames.getAllNames()),
                    this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.passwordAttributeName, DeviceMessageConstants.passwordAttributeDefaultTranslation));
        }
    },
    IMPORT_CA_CERTIFICATE(7050, "Import CA certificate") {
        //TODO: see how this CA will be stored in Connexo and from where to get this information. In EIServer an alias is used instead of certificate wrapper
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            //Referring to a CertificateWrapper in the database. Can be a CA certificate
            return Collections.singletonList(this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.certificateWrapperAttributeName, DeviceMessageConstants.certificateWrapperAttributeDefaultTranslation));
        }
    },
    IMPORT_CLIENT_END_DEVICE_CERTIFICATE(7051, "Import client end device certificate") {
        //TODO: see how this CA will be stored in Connexo and from where to get this information. In EIServer an alias is used instead of certificate wrapper
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            //Referring to a CertificateWrapper in the database. Can be a device certificate.
            return Collections.singletonList(this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.certificateWrapperAttributeName, DeviceMessageConstants.certificateWrapperAttributeDefaultTranslation));
        }
    },
    IMPORT_SERVER_END_DEVICE_CERTIFICATE(7052, "Import server end device certificate") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            //Referring to a CertificateWrapper in the database. Can be a device certificate.
            return Collections.singletonList(this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.certificateWrapperAttributeName, DeviceMessageConstants.certificateWrapperAttributeDefaultTranslation));
        }
    },
    CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY(7053, "Change authentication key using service key and new plain key") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    hexStringSpec(service, DeviceMessageConstants.preparedDataAttributeName, DeviceMessageConstants.preparedDataAttributeDefaultTranslation),
                    hexStringSpec(service, DeviceMessageConstants.signatureAttributeName, DeviceMessageConstants.signatureAttributeDefaultTranslation),
                    stringSpec(service, DeviceMessageConstants.verificationKeyAttributeName, DeviceMessageConstants.verificationKeyAttributeDefaultTranslation),
                    hexStringSpec(service, DeviceMessageConstants.newAuthenticationKeyAttributeName, DeviceMessageConstants.newAuthenticationKeyAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY(7054, "Change encryption key using service key and new plain key") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    hexStringSpec(service, DeviceMessageConstants.preparedDataAttributeName, DeviceMessageConstants.preparedDataAttributeDefaultTranslation),
                    hexStringSpec(service, DeviceMessageConstants.signatureAttributeName, DeviceMessageConstants.signatureAttributeDefaultTranslation),
                    stringSpec(service, DeviceMessageConstants.verificationKeyAttributeName, DeviceMessageConstants.verificationKeyAttributeDefaultTranslation),
                    hexStringSpec(service, DeviceMessageConstants.newEncryptionKeyAttributeName, DeviceMessageConstants.newEncryptionKeyAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY_FOR_CLIENT(7055, "Change authentication key with new keys, for client") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    bigDecimalSpec(service, DeviceMessageConstants.clientMacAddress, DeviceMessageConstants.clientMacAddressDefaultTranslation, BigDecimal.ONE),
                    keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newAuthenticationKeyAttributeName, DeviceMessageConstants.newAuthenticationKeyAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY_FOR_CLIENT(7056, "Change encryption key with new keys, for client") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    bigDecimalSpec(service, DeviceMessageConstants.clientMacAddress, DeviceMessageConstants.clientMacAddressDefaultTranslation, BigDecimal.ONE),
                    keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newEncryptionKeyAttributeName, DeviceMessageConstants.newEncryptionKeyAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_HLS_SECRET_PASSWORD_FOR_CLIENT(7057, "Change HLS secret, for client") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    bigDecimalSpec(service, DeviceMessageConstants.clientMacAddress, DeviceMessageConstants.clientMacAddressDefaultTranslation, BigDecimal.ONE),
                    keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newPasswordAttributeName, DeviceMessageConstants.newPasswordAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_MASTER_KEY_WITH_NEW_KEY(7058, "Change master key with new keys") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newMasterKeyAttributeName, DeviceMessageConstants.newMasterKeyAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_MASTER_KEY_WITH_NEW_KEY_FOR_CLIENT(7059, "Change master key with new keys, for client") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    bigDecimalSpec(service, DeviceMessageConstants.clientMacAddress, DeviceMessageConstants.clientMacAddressDefaultTranslation, BigDecimal.ONE),
                    keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newMasterKeyAttributeName, DeviceMessageConstants.newMasterKeyAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY_FOR_PREDEFINED_CLIENT(7060, "Change authentication key with new keys for predefined client") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    stringSpecBuilder(service, DeviceMessageConstants.client, DeviceMessageConstants.clientDefaultTranslation)
                            .addValues(getClients())
                            .finish(),
                    keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newAuthenticationKeyAttributeName, DeviceMessageConstants.newAuthenticationKeyAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY_FOR_PREDEFINED_CLIENT(7061, "Change encryption key with new keys for predefined client") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    stringSpecBuilder(service, DeviceMessageConstants.client, DeviceMessageConstants.clientDefaultTranslation)
                            .addValues(getClients())
                            .finish(),
                    keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newEncryptionKeyAttributeName, DeviceMessageConstants.newEncryptionKeyAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_MASTER_KEY_WITH_NEW_KEY_FOR_PREDEFINED_CLIENT(7062, "Change master key with new keys for predefined client") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    stringSpecBuilder(service, DeviceMessageConstants.client, DeviceMessageConstants.clientDefaultTranslation)
                            .addValues(getClients())
                            .finish(),
                    keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newMasterKeyAttributeName, DeviceMessageConstants.newMasterKeyAttributeDefaultTranslation)
            );
        }
    },
    SET_REQUIRED_PROTECTION_FOR_DATA_PROTECTION_SETUP(7063, "Set required protection for data protection setup") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(bigDecimalSpec(service, DeviceMessageConstants.requiredProtection, DeviceMessageConstants.requiredProtectionDefaultTranslation));
        }
    },
    KEY_RENEWAL(7064, "Renew key") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.keyAccessorTypeAttributeName, DeviceMessageConstants.keyAccessorTypeAttributeNameDefaultTranslation));
        }
    },
    UPDATE_CRL(7065, "Update CRL") {
        //TODO: see if we should use a string with max value spec to extend the maximum allowed string length (4000 for standard string properties)
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(stringSpec(service, DeviceMessageConstants.pemCRL, DeviceMessageConstants.pemCRLDefaultTranslation));
        }
    },
    REMOVE_CRL(7066, "Remove CRL") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(stringSpec(service, DeviceMessageConstants.crlIssuerName, DeviceMessageConstants.crlIssuerNameDefaultTranslation));
        }
    },
    UPDATE_DEVICE_CRL_USING_TRUSTED_CERT_CRL(7067, "Update device CRL using trusted certificate CRL") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(bigDecimalSpec(service, DeviceMessageConstants.trustedCertificateWrapperAttributeName, DeviceMessageConstants.trustedCertificateWrapperDefaultTranslation));
        }
    },

    //This messages only allows a set of predefined clients, as a String.
    CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_PREDEFINED_CLIENT(7068, "Change authentication key using service key for given client") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    hexStringSpec(service, DeviceMessageConstants.preparedDataAttributeName, DeviceMessageConstants.preparedDataAttributeDefaultTranslation),
                    hexStringSpec(service, DeviceMessageConstants.signatureAttributeName, DeviceMessageConstants.signatureAttributeDefaultTranslation),
                    stringSpec(service, DeviceMessageConstants.verificationKeyAttributeName, DeviceMessageConstants.verificationKeyAttributeDefaultTranslation),
                    hexStringSpec(service, DeviceMessageConstants.newAuthenticationKeyAttributeName, DeviceMessageConstants.newAuthenticationKeyAttributeDefaultTranslation),
                    stringSpec(service, DeviceMessageConstants.client, DeviceMessageConstants.clientDefaultTranslation, getClients()));
        }
    },

    //This messages only allows a set of predefined clients, as a String.
    CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_PREDEFINED_CLIENT(7069, "Change encryption key using service key for given client") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    hexStringSpec(service, DeviceMessageConstants.preparedDataAttributeName, DeviceMessageConstants.preparedDataAttributeDefaultTranslation),
                    hexStringSpec(service, DeviceMessageConstants.signatureAttributeName, DeviceMessageConstants.signatureAttributeDefaultTranslation),
                    stringSpec(service, DeviceMessageConstants.verificationKeyAttributeName, DeviceMessageConstants.verificationKeyAttributeDefaultTranslation),
                    hexStringSpec(service, DeviceMessageConstants.newEncryptionKeyAttributeName, DeviceMessageConstants.newEncryptionKeyAttributeDefaultTranslation),
                    stringSpec(service, DeviceMessageConstants.client, DeviceMessageConstants.clientDefaultTranslation, getClients()));
        }
    },

    //This messages allows any client, as a BigDecimal
    CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_CLIENT(7070, "Change authentication key using service key for client") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    hexStringSpec(service, DeviceMessageConstants.preparedDataAttributeName, DeviceMessageConstants.preparedDataAttributeDefaultTranslation),
                    hexStringSpec(service, DeviceMessageConstants.signatureAttributeName, DeviceMessageConstants.signatureAttributeDefaultTranslation),
                    stringSpec(service, DeviceMessageConstants.verificationKeyAttributeName, DeviceMessageConstants.verificationKeyAttributeDefaultTranslation),
                    hexStringSpec(service, DeviceMessageConstants.newAuthenticationKeyAttributeName, DeviceMessageConstants.newAuthenticationKeyAttributeDefaultTranslation),
                    bigDecimalSpec(service, DeviceMessageConstants.client, DeviceMessageConstants.clientDefaultTranslation, BigDecimal.valueOf(1)));
        }
    },

    //This messages allows any client, as a BigDecimal
    CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_CLIENT(7071, "Change encryption key using service key for client") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    hexStringSpec(service, DeviceMessageConstants.preparedDataAttributeName, DeviceMessageConstants.preparedDataAttributeDefaultTranslation),
                    hexStringSpec(service, DeviceMessageConstants.signatureAttributeName, DeviceMessageConstants.signatureAttributeDefaultTranslation),
                    stringSpec(service, DeviceMessageConstants.verificationKeyAttributeName, DeviceMessageConstants.verificationKeyAttributeDefaultTranslation),
                    hexStringSpec(service, DeviceMessageConstants.newEncryptionKeyAttributeName, DeviceMessageConstants.newEncryptionKeyAttributeDefaultTranslation),
                    bigDecimalSpec(service, DeviceMessageConstants.client, DeviceMessageConstants.clientDefaultTranslation, BigDecimal.valueOf(1)));
        }
    },

    CHANGE_PSK_WITH_NEW_KEYS(7072, "Change PSK") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newPSKAttributeName, DeviceMessageConstants.newPSKDefaultTranslation));
        }
    },

    CHANGE_PSK_USING_SERVICE_KEY(7073, "Change PSK using service key") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    hexStringSpec(service, DeviceMessageConstants.preparedDataAttributeName, DeviceMessageConstants.preparedDataAttributeDefaultTranslation),
                    hexStringSpec(service, DeviceMessageConstants.signatureAttributeName, DeviceMessageConstants.signatureAttributeDefaultTranslation),
                    stringSpec(service, DeviceMessageConstants.verificationKeyAttributeName, DeviceMessageConstants.verificationKeyAttributeDefaultTranslation));
        }
    },

    EXPORT_ALL_DEVICE_CERTIFICATES(7074, "Export all device certificates") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.stringSpec(service, DeviceMessageConstants.trustStoreNameAttributeName, DeviceMessageConstants.trustedStoreNameDefaultTranslation)
            );
        }
    },

    CHANGE_PSK_KEK(7075, "Change PSK KEK") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newPSKKEKAttributeName, DeviceMessageConstants.newPSKKEKDefaultTranslation)
            );
        }
    },

    //from eiserver 8.11
    MBUS_TRANSFER_FUAK(7076, "Set M-Bus Firmware Update Authentication Key (FUAK)") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },

    CHANGE_HLS_SECRET_USING_SERVICE_KEY_PROCESS(7077, "Change HLS secret using service key process") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newPasswordAttributeName, DeviceMessageConstants.newPasswordAttributeDefaultTranslation)
            );
        }
    },

    CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_PROCESS(7078, "Change authentication key using service key process") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newAuthenticationKeyAttributeName, DeviceMessageConstants.newAuthenticationKeyAttributeDefaultTranslation)
            );
        }
    },

    CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_PROCESS(7079, "Change encryption key using service key process") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newEncryptionKeyAttributeName, DeviceMessageConstants.newEncryptionKeyAttributeDefaultTranslation)
            );
        }
    },
    KEY_RENEWAL_EI6_7(7080, "Renew key EI6/7") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newAuthenticationKeyAttributeName, DeviceMessageConstants.newAuthenticationKeyAttributeDefaultTranslation),
                    keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.newEncryptionKeyAttributeName, DeviceMessageConstants.newEncryptionKeyAttributeDefaultTranslation),
                    stringSpec(service, DeviceMessageConstants.obisCode, DeviceMessageConstants.obisCodeDefaultTranslation, EI67KeyExchangeObjects.getAllObjects()));
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    SecurityMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }


    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    protected PropertySpec keyAccessorTypeReferenceSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .referenceSpec(KeyAccessorType.class.getName())
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec deviceGroupSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .referenceSpec(DeviceGroup.class.getName())
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec boundedBigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal lowerLimit, BigDecimal upperLimit) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .boundedBigDecimalSpec(lowerLimit, upperLimit)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    private String getNameResourceKey() {
        return SecurityMessage.class.getSimpleName() + "." + this.toString();
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
            return filterWith(each -> each.name().equals(name));
        }

        public static CertificateEntity fromId(int id) {
            return filterWith(each -> each.getId() == id);
        }

        public static CertificateEntity filterWith(Predicate<CertificateEntity> predicate) {
            return Stream
                    .of(values())
                    .filter(predicate)
                    .findFirst()
                    .orElse(Invalid);
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
            return filterWith(each -> each.name().equals(name));
        }

        public static CertificateType fromId(int id) {
            return filterWith(each -> each.getId() == id);
        }

        public static CertificateType filterWith(Predicate<CertificateType> predicate) {
            return Stream
                    .of(values())
                    .filter(predicate)
                    .findFirst()
                    .orElse(Invalid);
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
            return Stream
                    .of(values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .map(SealActions::getAction)
                    .orElse(null);
        }

        public static String[] getAllDescriptions() {
            return Stream.of(values()).map(SealActions::getDescription).toArray(String[]::new);
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
            return Stream
                    .of(values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .map(KeyTUsage::getStatus)
                    .orElse(null);
        }

        public static String[] getAllDescriptions() {
            return Stream.of(values()).map(KeyTUsage::getDescription).toArray(String[]::new);
        }

        public String getDescription() {
            return description;
        }

        public boolean getStatus() {
            return status;
        }
    }

    @Override
    public long id() {
        return this.id;
    }

    public enum KeyID {
        GLOBAL_UNICAST_ENCRYPTION_KEY(0),
        GLOBAL_BROADCAST_ENCRYPTION_KEY(1),
        AUTHENTICATION_KEY(2),
        MASTER_KEY(3),
        HLS_SECRET(4);

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

    public static String[] getClients() {
        return ClientSecuritySetup.getClients();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.SECURITY,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

}