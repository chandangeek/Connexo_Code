package com.elster.jupiter.appserver.security;
/*
public interface Privileges {



}

*/

import com.elster.jupiter.nls.TranslationKey;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    VIEW_APPSEVER(Constants.VIEW_APPSEVER, "View"),
    ADMINISTRATE_APPSEVER(Constants.ADMINISTRATE_APPSEVER, "Administrate");

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
        String VIEW_APPSEVER = "privilege.view.appServer";
        String ADMINISTRATE_APPSEVER = "privilege.administrate.appServer";
    }
}