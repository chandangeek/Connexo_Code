package com.elster.jupiter.systemproperties.impl;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_SYS_PROPS("systemProperties", "System properties"),
    RESOURCE_SYS_PROPS_DESCRIPTION("systemProperties.description", "Manage system properties"),

    //Privileges
    VIEW_SYS_PROPS(Constants.VIEW_SYS_PROPS, "View"),
    ADMINISTRATE_SYS_PROPS(Constants.ADMINISTRATE_SYS_PROPS, "Administer");


    private final String key;
    private final String description;

    Privileges(String key, String description) {
        this.key = key;
        this.description = description;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return description;
    }



    public interface Constants {
        String VIEW_SYS_PROPS = "privilege.view.sysProps";
        String ADMINISTRATE_SYS_PROPS = "privilege.administrate.sysProps";
    }
}
