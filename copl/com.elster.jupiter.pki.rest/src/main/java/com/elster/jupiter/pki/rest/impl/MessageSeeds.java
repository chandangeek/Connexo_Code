/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    NO_SUCH_TRUSTSTORE(1, "noSuchTrustStore", "The trust store could not be found"),
    FILE_TOO_BIG(2, "fileTooBig", "File size should be less than 250 kB"),
    COULD_NOT_CREATE_CERTIFICATE_FACTORY(3, "CertificateFactoryFail", "Could not create the certificate factory: {0}"),
    COULD_NOT_CREATE_CERTIFICATE(4, "CertificateCreationFailed", "Could not create the certificate: {0}"),
    COULD_NOT_IMPORT_KEYSTORE(5, "KeystoreReadError", "The keystore could not be imported"),
    NO_SUCH_CERTIFICATE(6, "NoSuchCertificate", "No such certificate could be located"),
    NO_SUCH_KEY_TYPE(7, "NoSuchKeyType", "No such key type"),
    CERTIFICATE_TOO_BIG(8, "fileTooBig", "File size should be less than 2 kB"),
    FIELD_IS_REQUIRED(9, "FieldIsrequired", "This field is required"),
    NOT_POSSIBLE_TO_CREATE_CSR(10, "NotPossibleToCreateCSR" , "CSR can not be generated for this type of certificate"),
    NO_CSR_PRESENT(11, "noCsrPresent", "No CSR found"),
    FAILED_TO_READ_CSR(12, "FailedToReadCSR", "The CSR could not be read"),
    NO_CERTIFICATE_PRESENT(13, "NoCertificatePresent", "No certificate found"),
    FAILED_TO_READ_CERTIFICATE(14, "FailedToReadCertificate", "The certificate could not be read"),
    NO_SUCH_CERTIFICATE_IN_STORE(15, "NoSuchCertificateInStore", "No certificate wrapper with id {0} could be located in trust store with id {1}"),
    NO_SUCH_CRYPTOGRAPHIC_TYPE(16, "NoSuchCryptographicType", "No such cryptographic type"),
    NOT_A_VALID_CERTIFICATE(17, "NotValidCertificate", "The provided file is a not a valid, DER-encoded certificate");

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
        return PkiService.COMPONENTNAME;
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
