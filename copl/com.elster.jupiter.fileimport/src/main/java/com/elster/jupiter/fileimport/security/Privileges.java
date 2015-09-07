package com.elster.jupiter.fileimport.security;

import com.elster.jupiter.nls.TranslationKey;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    ADMINISTRATE_IMPORT_SERVICES(Constants.ADMINISTRATE_IMPORT_SERVICES, "Administrate"),
    VIEW_IMPORT_SERVICES(Constants.VIEW_IMPORT_SERVICES, "View");

    private final String key;
    private final String description;

    Privileges(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return getDescription();
    }

    public String getDescription() {
        return description;
    }

    public static String[] keys() {
        return Arrays.stream(Privileges.values())
                .map(Privileges::getKey)
                .collect(Collectors.toList())
                .toArray(new String[Privileges.values().length]);
    }

    public interface Constants {
        String ADMINISTRATE_IMPORT_SERVICES = "privilege.administrate.importServices";
        String VIEW_IMPORT_SERVICES = "privilege.view.importServices";
    }
}
