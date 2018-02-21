/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.nls.TranslationKey;

public enum CSRImporterTranslatedProperty implements TranslationKey {
    IMPORT_SECURITY_ACCESSOR("importSecurityAccessor", "Import security accessor"),
    IMPORT_SECURITY_ACCESSOR_DESCRIPTION("importSecurityAccessor.description", "Security accessor used to decrypt imported zip"),
    EXPORT_CERTIFICATES("exportCertificates", "Export certificates"),
    EXPORT_CERTIFICATES_DESCRIPTION("exportCertificates.description", "Whether to export result certificates or not"),
    EXPORT_SECURITY_ACCESSOR("exportSecurityAccessor", "Export security accessor"),
    EXPORT_SECURITY_ACCESSOR_DESCRIPTION("exportSecurityAccessor.description", "Security accessor used to encrypt exported zip"),
    EXPORT_TRUST_STORE("exportTrustStore", "Trust store"),
    EXPORT_TRUST_STORE_DESCRIPTION("exportTrustStore.description", "All certificates from this trust store are added to the exported zip file"),
    EXPORT_HOSTNAME("exportHostname", "Hostname"),
    EXPORT_HOSTNAME_DESCRIPTION("exportHostname.description", "Destination hostname"),
    EXPORT_PORT("exportPort", "Port"),
    EXPORT_PORT_DESCRIPTION("exportPort.description", "Destination port"),
    EXPORT_USER("exportUser", "User"),
    EXPORT_USER_DESCRIPTION("exportUser.description", "User for destination ftp resource"),
    EXPORT_PASSWORD("exportPassword", "Password"),
    EXPORT_PASSWORD_DESCRIPTION("exportPassword.description", "Password for destination ftp resource"),
    EXPORT_FILE_NAME("exportFileName", "File name"),
    EXPORT_FILE_NAME_DESCRIPTION("exportFileName.description", "Destination file name"),
    EXPORT_FILE_EXTENSION("exportFileExtension", "File extension"),
    EXPORT_FILE_EXTENSION_DESCRIPTION("exportFileExtension.description", "Destination file extension"),
    EXPORT_FILE_LOCATION("exportFileLocation", "File location"),
    EXPORT_FILE_LOCATION_DESCRIPTION("exportFileLocation.description", "Destination file location on the provided ftp resource");

    private final String key;
    private final String defaultFormat;

    CSRImporterTranslatedProperty(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public String getPropertyKey() {
        return CSRImporterFactory.NAME + "." + getKey();
    }

    public static CSRImporterTranslatedProperty from(String key) {
        if (key != null) {
            for (CSRImporterTranslatedProperty translationKey : CSRImporterTranslatedProperty.values()) {
                if (translationKey.getKey().equals(key)) {
                    return translationKey;
                }
            }
        }
        return null;
    }
}
