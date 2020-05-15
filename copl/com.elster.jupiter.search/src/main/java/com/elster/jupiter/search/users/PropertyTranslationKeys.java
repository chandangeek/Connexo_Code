package com.elster.jupiter.search.users;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum PropertyTranslationKeys implements TranslationKey {

    USER_DOMAIN("user.domain", "User"),

    USER_NAME("user.username", "Username"),
    USER_DESCRIPTION("user.description", "Description"),
    USER_DIRECTORY("user.userDirectory", "User directory"),
    USER_STATUS("user.active", "Active"),
    USER_ROLES("user.roles", "Roles"),
    USER_LANGUAGE("user.language", "Language"),
    USER_CREATION_DATE("user.creationDate", "Created on"),
    USER_MODIFICATION_DATE("user.modificationDate", "Modified on"),
    USER_LAST_SUCCESSFUL_LOGIN("user.lastSuccessfulLogin", "Last successful login"),
    USER_LAST_UNSUCCESSFUL_LOGIN("user.lastUnsuccessfulLogin", "Last unsuccessful login");

    private String key;
    private String defaultFormat;

    PropertyTranslationKeys(String key, String defaultFormat) {
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

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    }
}