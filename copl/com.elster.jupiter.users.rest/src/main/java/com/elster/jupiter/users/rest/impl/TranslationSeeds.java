package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationSeeds implements TranslationKey {

    // key - name of the field, defaultFormat - name of the column on grid

    NAME("authenticationName", "Username"),
    DESCRIPTION("description", "Description"),
    DOMAIN("domain", "Domain"),
    STATUS("active", "Status"),
    EMAIL("email", "Email"),
    LANGUAGE("language", "Language"),
    CREATETIME("createdOn", "Created on"),
    MODTIME("modifiedOn", "Modified on"),
    LASTSUCCESSFULLOGIN("lastSuccessfulLogin", "Last successful login"),
    LASTUNSUCCESSFULLOGIN("lastUnSuccessfulLogin", "Last unsuccessful login"),
    ISUSERLOCKED("isUserLocked", "Locked");

    private final String key;
    private final String defaultFormat;

    TranslationSeeds(String key, String defaultFormat) {
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

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getString(this.key, this.defaultFormat);
    }
}
