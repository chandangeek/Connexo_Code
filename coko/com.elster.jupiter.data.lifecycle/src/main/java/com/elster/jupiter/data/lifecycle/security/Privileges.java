package com.elster.jupiter.data.lifecycle.security;

import com.elster.jupiter.nls.TranslationKey;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    ADMINISTRATE_DATA_PURGE(Constants.ADMINISTRATE_DATA_PURGE, "Administrate"),
    VIEW_DATA_PURGE(Constants.VIEW_DATA_PURGE, "View");

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
        String VIEW_DATA_PURGE = "privilege.view.dataPurge";
        String ADMINISTRATE_DATA_PURGE = "privilege.administrate.dataPurge";
    }
}