/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.pki.impl.importers.csr.CSRImporterFactory;
import com.elster.jupiter.pki.impl.importers.csr.CSRImporterMessageHandlerFactory;

public enum TranslationKeys implements TranslationKey {
    ALIAS("Alias", "Alias"),
    AVAILABLE("Available", "Available"),
    REQUESTED("Requested", "Requested"),
    EXPIRED("Expired", "Expired"),
    OBSOLETE("Obsolete", "Obsolete"),
    REVOKED("Revoked", "Revoked"),
    NOT_YET_VALID("notYetValid", "Not yet valid"),
    TRUSTSTORE("TrustStore", "Trust store"),
    PUBLIC_KEY("PublicKey", "Public key"),
    CSR_IMPORTER(CSRImporterFactory.NAME, "CSR importer [STD]"),
    CSR_IMPORTER_MESSAGE_HANDLER(CSRImporterMessageHandlerFactory.SUBSCRIBER_NAME, "Handle CSR import"),
    CSR_IMPORT_SUCCESS("CSRImportSuccess", "Finished successfully."),
    CSR_IMPORT_FAILED("CSRImportFailed", "Failed to complete, no CSR has been processed."),
    KEY("Key", "Key"),
    LABEL("Label", "Label"),
    SM_KEY("Smart meter key", "Smart meter key");

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
