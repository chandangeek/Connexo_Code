package com.elster.jupiter.systemproperties;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_SYS_PROPS("systemProperties", "System properties"),
    RESOURCE_SYS_PROPS_DESCRIPTION("systemProperties.description", "Manage system properties"),

    //Privileges
    VIEW_SYS_PROPS(Constants.VIEW_SYS_PROPS, "View"),
    ADMINISTRATE_SYS_PROPS(Constants.ADMINISTRATE_SYS_PROPS, "Administrate");


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
        //String VIEW_DATA_PURGE = "privilege.view.sysProps";
        //String ADMINISTRATE_DATA_PURGE = "privilege.administrate.sysProps";
        String VIEW_SYS_PROPS = "privilege.view.sysProps";
        String ADMINISTRATE_SYS_PROPS = "privilege.administrate.sysProps";
    }
}
