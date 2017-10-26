/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.pki.impl.importers.CertificateImporterFactory;
import com.elster.jupiter.pki.impl.importers.CertificateImporterMessageHandler;

public enum TranslationKeys implements TranslationKey {

    ALIAS("Alias", "Alias"),
    AVAILABLE("Available", "Available"),
    REQUESTED("Requested", "Requested"),
    EXPIRED("Expired", "Expired"),
    NOT_YET_VALID("notYetValid", "Not yet valid"),
    TRUSTSTORE("TrustStore", "Trust store"),

    CERTIFICATES_FILE_IMPORTER(CertificateImporterFactory.NAME, "Certificate importer [STD]"),
    CERTIFICATE_MESSAGE_SUBSCRIBER(CertificateImporterMessageHandler.SUBSCRIBER_NAME, "Handle certificate import");

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static TranslationKeys from(String key) {
        if (key != null) {
            for (TranslationKeys translationKey : TranslationKeys.values()) {
                if (translationKey.getKey().equals(key)) {
                    return translationKey;
                }
            }
        }
        return null;
    }

}