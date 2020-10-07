/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Defines all the {@link MessageSeed}s of the PKI module.
 */
public enum MessageSeeds implements MessageSeed {
    NAME_IS_REQUIRED(1000, Keys.NAME_REQUIRED, "The name is required."),
    NAME_IS_UNIQUE(1001, Keys.NAME_UNIQUE, "Name must be unique"),
    FIELD_IS_REQUIRED(1003, Keys.FIELD_IS_REQUIRED, "This field is required."),
    NO_SUCH_ENCRYPTION_METHOD(1004, Keys.NO_SUCH_ENCRYPTION_METHOD, "Storage method for a secret value is unknown or not registered yet"),
    DUPLICATE_KEY_ENCRYPTION_REGISTRATION(1005, Keys.DUPLICATE_KEY_ENCRYPTION_REGISTRATION, "A key storage method with the same name for this type of elements has already been registered"),
    ALGORITHM_NOT_SUPPORTED(1006, "NoSuchAlgorithm", "The required algorithm isn''t supported in the environment at this time."),
    INVALID_KEY(1007, "InvalidKeyException", "The key couldn''t be recreated due to erroneous encoding."),
    INVALID_KEY_SPECIFICATION(1008, "InvalidKeySpecificationException", "The key specification is invalid"),
    INVALID_ALGORITHM_PARAMETERS(1009, "InvalidAlgorithmException", "Invalid or inappropriate algorithm parameters were provided"),
    UNKNOWN_PROVIDER(1010, "UnknownSecurityProvider", "The requested security provider isn''t available in the environment"),
    CRL_EXCEPTION(1011, "CrlException", "Couldn''t read CRL."),
    CERTIFICATE_EXCEPTION(1012, "CertificateException", "Couldn''t read certificate."),
    CERTIFICATE_ENCODING_EXCEPTION(1013, "CertificateEncodingException", "The certificate couldn''t be properly encoded."),
    ALIAS_IS_UNIQUE(1014, Keys.ALIAS_UNIQUE, "Alias must be unique"),
    SIGNATURE_EXCEPTION(1015, "SignatureException", "An error occurred while signing"),
    FAILED_TO_GENERATE_CSR(1016, "FailedToConstructCSR", "CSR couldn''t be created."),
    INCORRECT_KEY_ENCRYTION_METHOD(1017, "IncorrectKeyEncryptionMethod", "Can''t handle non-plaintext keys."),
    FAILED_TO_WRAP_WK(1018, "FailedToWrapWK", "Failed to AES wrap the WK: {0}"),
    GENERAL_KEYSTORE_FAILURE(1019, "GeneralKeystoreFailure", "The keystore couldn''t be read: {0}"),
    COULD_NOT_READ_KEY_USAGES(1020, "ErrorReadingKeyUsages", "Couldn''t read key usages."),
    CERTIFICATE_PUBLIC_KEY_MISMATCH(1021, "PublicKeyMismatchCSR", "The certificate''s public key doesn''t match the CSR."),
    UNSUPPORTED_KEY_TYPE(1022, "UnsupportedKeyType", "The key type you requested isn''t supported: {0}"),
    CERTIFICATE_SUBJECT_DN_MISMATCH(1023, "SubjectMismatchCSR", "The certificate''s subject distinguished name doesn''t match the CSR."),
    CERTIFICATE_KEY_USAGE_MISMATCH(1024, "KeyUsagesMismatchCSR", "The certificate''s key usage extension doesn''t match the CSR."),
    CERTIFICATE_EXTENDED_KEY_USAGES_MISMATCH(1025, "ExtendedKeyUsagesMismatchCSR", "The certificate''s extended key usage extension doesn''t match the CSR."),
    INVALID_VALUE(1026, Keys.INVALID_VALUE, "This value is invalid"),
    NO_POSSIBLE_CHARS_IN_PASSWORD(1027, Keys.NOVALIDCHARACTERS, "The passphrase type has no characters sets to choose from"),
    INVALID_PASSWORD_LENGTH(1028, Keys.INVALIDPASSPHRASELENGTH, "Invalid passphrase length"),
    INVALID_KEY_SIZE(1029, Keys.INVALID_KEY_SIZE, "Invalid key size"),
    INVALID_HEX_VALUE(1030, Keys.INVALID_HEX_VALUE, "Not a properly hex encoded key"),
    UNSUPPORTED_IMPORT_TYPE(1031, "UnsupportedImportType", "Connexo currently can''t import device secrets of type ''{0}'' (security accessor ''{1}'')."),
    NO_IMPORT_KEY_DEFINED(1032, "NoImportKeyDefined", "DataVault importer requires an import key to be defined in the config file, however the property ''{0}'' wasn''t set."),
    IMPORT_KEY_NOT_FOUND(1033, "ImportKeyNotFound", "DataVault importer couldn''t find the keypair ''{0}'' with an associated private key. It is required during import."),
    INCORRECT_IMPORT_KEY(1034, "IncorrectImportKey", "The keypair ''{0}'' isn''t associated with a private key and can''t be used by the importer."),
    DEVICE_KEY_IMPORT_FAILED(1035, "FailedToImportDeviceKey", "DataVault importer failed to import the device key: ''{0}''"),
    PUBLIC_KEY_INVALID(1036, "CouldNotConstructKey", "The public key couldn''t be created: {0}"),
    CAN_NOT_GENERATE_PUBLIC(1037, "CanNotGeneratePublic", "A public key can''t be generated, only a public/private keypair."),
    NO_PUBLIC_KEY_TO_VERIFY(1038, "NoPublicKeyForVerification", "The public key can''t be verified: no public key counterpart for verification"),
    PUBLIC_KEY_DOES_NOT_MATCH(1039, "PublicKeyDoesNotMatch", "Incorrect public key used to encrypt wrap key"),
    NO_SUCH_TRUSTSTORE(1040, "noSuchTrustStore", "The trust store couldn''t be found."),
    INVALID_DN(1041, "InvalidDN", "Invalid distinguished name encountered"),
    PROPERTY_VALUE_REQUIRED(1042, "InvalidPropertyValue", "Value for property {0} is required"),
    CA_RUNTIME_ERROR(1043, "CaError", "CA runtime error: {0}"),
    CA_RUNTIME_ERROR_NO_TRUSTSTORE(1044, "CaErrorNoTruststore", "CA runtime error: no truststore {0} found."),
    CA_RUNTIME_ERROR_NO_SELF_SIGNED_CERTIFICATE(1045, "CaErrorNoSelfSignedCertificate", "CA runtime error: no self signed certificate with alias {0} found."),
    CA_RUNTIME_ERROR_NO_CLIENT_CERTIFICATE(1046, "CaErrorNoClientCertificate", "CA runtime error: no client certificate with alias {0} found."),
    CA_RUNTIME_ERROR_NO_PRIVATE_KEY_FOR_CLIENT_CERTIFICATE(1047, "CaErrorNoPrivateKey", "CA runtime error: no private key for client certificate found."),
    INVALID_REVOCATION_REASON(1048, "InvalidRevocationReason", "Invalid revocation reason: {0}"),
    EXCESSIVE_TIME_DURATION(1049, Keys.EXCESSIVE_TIME_DURATION, "Validity period must be shorter than or equal to 30 years."),
    TRUSTSTORE_USED_ON_SECURITY_ACCESSOR(1050, "TrustStoreUsedOnSecurityAccessor", "The trust store couldn''t be removed because it is used on a security accessor."),
    TEMP_VALUE_NOT_SET(1051, "NoTempValue", "The security accessor doesn''t contain a temporary value."),
    NO_ACTUAL_CERTIFICATE(1052, "NoActualCertificate", "Certificate renewal requires an actual value in order to create a distinguished name"),
    ACTUAL_VALUE_NOT_SET(1053, "NoActualValue", "The security accessor doesn''t contain an actual value."),
    VETO_CERTIFICATE_DELETION(1054, "VetoCertificateDeletion", "Certificate ''{0}'' is still in use."),
    CSR_EXCEPTION(1055, "CsrException", "Couldn''t read CSR."),
    CERTIFICATE_USED_BY_DIRECTORY(1056, "CertificateUsedByDirectory", "The certificate couldn''t be removed because it is used on a user directory."),
    TRUSTSTORE_USED_BY_DIRECTORY(1057, "TrustStoreUsedByDirectory", "The trust store couldn''t be removed because it is used on a user directory."),
    CERTIFICATE_USED_ON_SECURITY_ACCESSOR(1058, "CertificateUsedOnSecurityAccessor", "The certificate couldn''t be removed because it is used on a centrally managed security accessor."),
    BAD_SIGNATURE(1059, "BadSignature", "File signature verification failed."),
    OK_SIGNATURE(1060, "OkSignature", "File signature verification completed successfully.", Level.INFO),
    NO_CERTIFICATE_IN_WRAPPER(1061, "NoCertificateInWrapper", "No actual certificate is kept under alias {0}."),
    CSR_IMPORT_EXCEPTION(1062, "CsrImportException", "Exception during CSR import: {0}"),
    NO_PRIVATE_KEY_FOR_SIGNING(1063, "NoPrivateKeyForCertificate", "The certificate {0} doesn''t contain a private key for signing the exported file."),
    INAPPROPRIATE_CERTIFICATE_TYPE(1064, "InappropriateCertificateType", "The certificate {0} isn''t of type {1}."),
    FAILED_TO_SIGN(1065, "FailedToSign", "Failed to sign the exported file."),
    SOME_CERTIFICATES_NOT_SIGNED(1066, "CertificatesNotSigned", "Some certificates from the imported file haven''t been signed by the CA."),
    SUBDIRECTORIES_IN_ZIP_FILE(1067, "IncorrectFileStructureSubdirectories", "Subdirectory ''{0}'' is found in CSR entry directory."),
    NO_DIRECTORY_FOR_CSR_FILE(1068, "IncorrectFileStructureNoDirectory", "No directory is found for CSR file ''{0}''."),
    NOT_CSR_FILE_EXTENSION(1069, "IncorrectFileExtensionCSR", "Incorrect extension is found for CSR file ''{0}''."),
    WRONG_FILE_NAME_FORMAT(1070, "WrongFileNameFormat", "Unexpected file name format in the imported zip. File name should contain non-empty file prefix and file system separated with a hyphen."),
    CSR_IS_IN_USE(1071, "CsrIsInUse", "Can''t import CSR for certificate with alias {0}: it is currently in use."),
    CSR_IMPORTED_SUCCESSFULLY(1072, "CsrImportedSuccessfully", "CSR {0} has been imported to Connexo.", Level.INFO),
    SIGN_CSR_BY_CA_TIMED_OUT(1073, "SignCsrByCaTimedOut", "Certificate signing request to CA has timed out for alias {0}. The certificate isn''t signed."),
    SIGN_CSR_BY_CA_FAILED(1074, "SignCsrByCaFailed", "Certificate signing request to CA has failed for alias {0}: {1}"),
    CSR_SIGNED_SUCCESSFULLY(1075, "SignCsrByCaSucceeded", "Certificate {0} has been signed.", Level.INFO),
    CERTIFICATE_IMPORTED_SUCCESSFULLY(1076, "CertificateImportedSuccessfully", "Signed certificate {0} has been imported to Connexo.", Level.INFO),
    SECURITY_ACCESSOR_USED_BY_IMPORT(1077, "SecurityAccessorUsedByImport", "The security accessor couldn''t be removed because it is used on import services."),
    TRUSTSTORE_USED_BY_IMPORT(1078, "TrustStoreUsedByImport", "The trust store couldn''t be removed because it is used on import services."),
    POSITIVE_VALUE_IS_REQUIRED(1079, "PositiveValueIsRequired", "Positive value is required."),
    CA_RUNTIME_ERROR_NOT_CONFIGURED_PROPERLY(1080, "CaErrorNotConfigured", "CA service isn''t properly configured."),
    CERTIFICATES_EXPORTED_SUCCESSFULLY(1081, "CertificatesExportedSuccessfully", "''{0}'' has been successfully exported to the destination ''{1}''.", Level.INFO),
    NO_TRUSTED_CERTIFICATE_IN_KEYSTORE(1082, "NoTrustedCertificateInKeystore", "No trusted certificate(s) found in keystore"),
    HEXBINARY_EVEN_LENGTH(1083, Keys.HEXBINARY_EVEN_LENGTH, "The key size needs to be even-length"),
    ENCRYPTED_KEY_INVALID(1084, "CouldNotRenewKey", "The encrypted key couldn''t be created"),
    INVALID_LABEL(1085, Keys.INVALID_LABEL, "This label is invalid (non empty label required)"),
    SKIPPING_SIGNATURE_VERIFICATION(1086, "SkipSignatureVerification", "Skipping signature verification"),
    PROCESSING_CSR(1087, "ProcessingCSR", "Processing CSR for serial {0} and alias {1}" , Level.INFO),
    FUAK_RENEW_NOT_SUPPORTED(1088, "FUAKRenewNotSupported", "FUAK key renew is currently not supported by this functionality. Key will be renewed from the protocol command.", Level.INFO),
    ALIAS_NOT_FOUND_EXCEPTION(1089, "AliasNotFound", "Alias not found: {0}", Level.WARNING),
    ALIAS_NOT_UNIQUE_EXCEPTION(1090, "AliasNotUnique", "Alias {0} is not unique", Level.WARNING),
    CANNOT_EXTRACT_COMMON_NAME(1090, "CannotExtractCommonName", "Cannot extract CN from {0}", Level.WARNING),
    CANNOT_RENEW_REVERSIBLE_KEY(1091, "cannotRenewReversibleKey", "Cannot renew reversible key. Please check if the label can be used for reversible keys or any length.", Level.SEVERE),
    NO_WRAPPER_ACTUAL_VALUE(1092, "noWrapperValue" , "A wrapper key is necessary to renew this key!", Level.SEVERE);

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public String getModule() {
        return SecurityManagementService.COMPONENTNAME;
    }

    public static class Keys {
        public static final String NAME_REQUIRED = "X.name.required";
        public static final String NAME_UNIQUE = "X.name.unique";
        public static final String ALIAS_UNIQUE = "X.alias.unique";
        public static final String FIELD_TOO_LONG = "fieldTooLong";
        public static final String FIELD_IS_REQUIRED = "field.required";
        public static final String NO_SUCH_ENCRYPTION_METHOD = "no.such.encryption.method";
        public static final String DUPLICATE_KEY_ENCRYPTION_REGISTRATION = "key.encryption.duplication";
        public static final String INVALID_LABEL = "validation.invalid.label";
        public static final String INVALID_VALUE = "validation.invalid.value";
        public static final String INVALID_HEX_VALUE = "validation.invalid.hex.value";
        public static final String NOVALIDCHARACTERS = "NoValidCharacters";
        public static final String INVALIDPASSPHRASELENGTH = "InvalidPassphraseLength";
        public static final String INVALID_KEY_SIZE = "InvalidKeySize";
        public static final String EXCESSIVE_TIME_DURATION = "excessiveTimeDuration";
        public static final String HEXBINARY_EVEN_LENGTH="HexBinaryEvenLength";
    }

}
