package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.util.exception.MessageSeed;

import aQute.bnd.annotation.ProviderType;

import java.util.logging.Level;

/**
 * Defines all the {@link MessageSeed}s of the PKI module.
 */
@ProviderType
public enum MessageSeeds implements MessageSeed {
    NAME_IS_REQUIRED(1000, Keys.NAME_REQUIRED, "The name is required"),
    NAME_IS_UNIQUE(1001, Keys.NAME_UNIQUE, "Name must be unique"),
    FIELD_TOO_LONG(1002, Keys.FIELD_TOO_LONG, "Field length must not exceed {max} characters"),
    FIELD_IS_REQUIRED(1003, Keys.FIELD_IS_REQUIRED, "This field is required"),
    NO_SUCH_ENCRYPTION_METHOD(1004, Keys.NO_SUCH_ENCRYPTION_METHOD, "Storage method for a secret value is unknown or not registered yet"),
    DUPLICATE_KEY_ENCRYPTION_REGISTRATION(1005, Keys.DUPLICATE_KEY_ENCRYPTION_REGISTRATION, "A key storage method with the same name for this type of elements has already been registered"),
    ALGORITHM_NOT_SUPPORTED(1006, "NoSuchAlgorithm", "The required algorithm is not supported in the environment at this time"),
    INVALID_KEY(1007, "InvalidKeyException", "The key could not be recreated due to erroneous encoding"),
    INVALID_KEY_SPECIFICATION(1007, "InvalidKeySpecificationException", "The key specification is invalid"),
    INVALID_ALGORITHM_PARAMETERS(1009, "InvalidAlgorithmException", "Invalid or inappropriate algorithm parameters were provided"),
    UNKNOWN_PROVIDER(1010, "UnknownSecurityProvider", "The requested security provider is not available in the environment"),
    CRL_EXCEPTION(1011, "CrlException", "Could not read CRL"),
    CERTIFICATE_EXCEPTION(1012, "CertificateException", "Could not read certificate"),
    CSR_EXCEPTION(1012, "CsrException", "Could not read CSR"),
    CERTIFICATE_ENCODING_EXCEPTION(1013, "CertificateEncodingException", "The certificate could not be properly encoded"),
    ALIAS_IS_UNIQUE(1014, Keys.ALIAS_UNIQUE, "Alias must be unique"),
    SIGNATURE_EXCEPTION(1015, "SignatureException", "An error occurred while signing"),
    FAILED_TO_GENERATE_CSR(1016, "FailedToConstructCSR", "CSR could not be created"),
    INCORRECT_KEY_ENCRYTION_METHOD(1017, "IncorrectKeyEncryptionMethod", "Can not handle non-plaintext keys"),
    FAILED_TO_WRAP_WK(1018, "FailedToWrapWK", "Failed to AES wrap the WK: {0}"),
    GENERAL_KEYSTORE_FAILURE(1019, "GeneralKeystoreFailure", "The keystore could not be read: {0}"),
    COULD_NOT_READ_KEY_USAGES(1020, "ErrorReadingKeyUsages", "Could not read key usages"),
    CERTIFICATE_PUBLIC_KEY_MISMATCH(1021, "PublicKeyMismatchCSR", "The certificate''s public key does not match the CSR"),
    UNSUPPORTED_KEY_TYPE(1022, "UnsupportedKeyType", "The key type you requested is not supported: {0}"),
    CERTIFICATE_SUBJECT_DN_MISMATCH(1023, "SubjectMismatchCSR", "The certificate''s subject distinguished name does not match the CSR"),
    CERTIFICATE_KEY_USAGE_MISMATCH(1024, "KeyUsagesMismatchCSR", "The certificate''s key usage extension does not match the CSR"),
    CERTIFICATE_EXTENDED_KEY_USAGES_MISMATCH(1025, "ExtendedKeyUsagesMismatchCSR", "The certificate''s extended key usage extension does not match the CSR"),
    INVALID_VALUE(1026,Keys.INVALID_VALUE, "This value is invalid"),
    NO_POSSIBLE_CHARS_IN_PASSWORD(1027, Keys.NOVALIDCHARACTERS, "The passphrase type has no characters sets to choose from"),
    INVALID_PASSWORD_LENGTH(1028, Keys.INVALIDPASSPHRASELENGTH, "Invalid passphrase length"),
    INVALID_KEY_SIZE(1029, Keys.INVALID_KEY_SIZE, "Invalid key size"),
    INVALID_HEX_VALUE(1030,Keys.INVALID_HEX_VALUE, "Not a properly hex encoded key"),
    UNSUPPORTED_IMPORT_TYPE(1031, "UnsupportedImportType", "Connexo can currently not import device secrets of type ''{0}'' (security accessor ''{1}'')"),
    NO_IMPORT_KEY_DEFINED(1032, "NoImportKeyDefined", "DataVault importer requires an import key to be defined in the config file, however the property ''{0}'' was not set."),
    IMPORT_KEY_NOT_FOUND(1033, "ImportKeyNotFound", "DataVault importer could not find the keypair ''{0}'' with associated private key, required during import"),
    INCORRECT_IMPORT_KEY(1034, "IncorrectImportKey", "The keypair ''{0}'' does not seem to be associated with a private key and can not be used by the importer"),
    DEVICE_KEY_IMPORT_FAILED(1035, "FailedToImportDeviceKey", "DataVault importer failed to import the device key: ''{0}''"),
    PUBLIC_KEY_INVALID(1036, "CouldNotConstructKey", "The public key could not be created: {0}"),
    CAN_NOT_GENERATE_PUBLIC(1037, "CanNotGeneratePublic", "A public key can not be generated, only a public/private keypair"),
    NO_PUBLIC_KEY_TO_VERIFY(1038, "NoPublicKeyForVerification", "The public key can not be verified: no public key counterpart for verification"),
    PUBLIC_KEY_DOES_NOT_MATCH(1039, "PublicKeyDoesNotMatch", "Incorrect public key used to encrypt wrap key");

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
        public static final String CERTIFICATE_DOES_NOT_MATCH_CSR = "certificate.mismatch.csr";
        public static final String INVALID_VALUE = "validation.invalid.value";
        public static final String INVALID_HEX_VALUE = "validation.invalid.hex.value";
        public static final String NOVALIDCHARACTERS = "NoValidCharacters";
        public static final String INVALIDPASSPHRASELENGTH = "InvalidPassphraseLength";
        public static final String INVALID_KEY_SIZE = "InvalidKeySize";
    }

}