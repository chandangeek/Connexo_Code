/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    NO_SUCH_TRUSTSTORE(1, "noSuchTrustStore", "The trust store couldn''t be found."),
    FILE_TOO_BIG(2, "fileTooBig", "File size should be less than 250 kB."),
    COULD_NOT_CREATE_CERTIFICATE_FACTORY(3, "CertificateFactoryFail", "Couldn''t create the certificate factory: {0}."),
    COULD_NOT_CREATE_CERTIFICATE(4, "CertificateCreationFailed", "Couldn''t create the certificate: {0}."),
    COULD_NOT_IMPORT_KEYSTORE(5, "KeystoreReadError", "The keystore couldn''t be imported: {0}."),
    NO_SUCH_CERTIFICATE(6, "NoSuchCertificate", "No such certificate could be found."),
    NO_SUCH_KEY_TYPE(7, "NoSuchKeyType", "No such key type."),
    IMPORTFILE_TOO_BIG(8, "pkcsFileTooBig", "File size should be less than 2 kB."),
    FIELD_IS_REQUIRED(9, "FieldIsrequired", "This field is required."),
    NOT_POSSIBLE_TO_CREATE_CSR(10, "NotPossibleToCreateCSR" , "CSR can''t be generated for this type of certificate."),
    NO_CSR_PRESENT(11, "noCsrPresent", "No CSR found."),
    FAILED_TO_READ_CSR(12, "FailedToReadCSR", "The CSR couldn''t be read."),
    NO_CERTIFICATE_PRESENT(13, "NoCertificatePresent", "No certificate found."),
    FAILED_TO_READ_CERTIFICATE(14, "FailedToReadCertificate", "The certificate couldn''t be read."),
    NO_SUCH_CERTIFICATE_IN_STORE(15, "NoSuchCertificateInStore", "No certificate wrapper with id {0} could be found in trust store with id {1}."),
    NO_SUCH_CRYPTOGRAPHIC_TYPE(16, "NoSuchCryptographicType", "No such cryptographic type."),
    NOT_A_VALID_CERTIFICATE(17, "NotValidCertificate", "The provided file isn''t a valid DER-encoded certificate."),
    INVALID_DN(18, "InvalidDN", "Invalid distinguished name has been encountered."),
    NO_SUCH_KEYPAIR(19, "NoSuchKeypair", "No such keypair could be found."),
    NO_PUBLIC_KEY_PRESENT(20, "NoPublicKeyPresent", "No public key found."),
    INVALID_PUBLIC_KEY(21, "InvalidPublicKey", "The file doesn''t contain a valid public key."),
    INVALID_KEY(22, "InvalidKey", "Invalid key."),
    REVOCATION_FAILED(25, "RevocationFailed", "The certificate couldn''t be revoked by the CA."),
    NO_CERTIFICATES_WITH_IDS(26, "NoSCertificatesWithIds", "No certificate wrappers could be found with ids {0}."),
    CERTIFICATE_ALREADY_REVOKED(27, "CertificateAlreadyRevoked", "The certificate has already been revoked by the CA. The status has been updated to ''revoked''."),
    COULD_NOT_RECIEVE_CERTIFICATE_TIMEOUT(28, "CertificateRequestFromCAFailedTimeout", "A timeout has occurred. The certificate couldn''t be received from the CA."),
    COULD_NOT_SAVE_CERTIFICATE_FROM_CA(29, "CertificateSavingFromCAFailed", "The certificate has been received from the CA but an error has occurred while saving: {0}"),
    COULD_NOT_RECIEVE_CERTIFICATE_FROM_CA(30, "CertificateRequestFromCAFailed", "The certificate couldn''t be received from the CA: {0}"),
    KEYSTORE_SHOULD_BE_JKS(31, "KeystoreShouldBeJks", "Keystore file should be of type JKS.")
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;

    MessageSeeds(int number, String key, String defaultFormat) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getModule() {
        return SecurityManagementService.COMPONENTNAME;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

}
