/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    NO_SUCH_TRUSTSTORE(1, "noSuchTrustStore", "Trust store {0} does not exist"),
    FILE_TOO_BIG(2, "fileTooBig", "File size should be less than 250 kB"),
    COULD_NOT_CREATE_CERTIFICATE_FACTORY(3, "CertificateFactoryFail", "Could not create the certificate factory: {0}"),
    COULD_NOT_CREATE_CERTIFICATE(4, "CertificateCreationFailed", "Could not create the certificate: {0}"),
    COULD_NOT_READ_KEYSTORE(5, "KeystoreReadError", "Could not read the keystore: {0}"),
    NO_SUCH_CERTIFICATE(6, "NoSuchCertificate", "No certificate wrapper with alias {0} could be located"),
    NO_SUCH_KEY_TYPE(7, "NoSuchKeyType", "No such key type"),
    CERTIFICATE_TOO_BIG(8, "fileTooBig", "File size should be less than 2 kB"),
    FIELD_IS_REQUIRED(9, "FieldIsrequired", "This field is required");
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
