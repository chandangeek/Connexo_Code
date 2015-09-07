package com.energyict.mdc.engine.config.security;

import com.elster.jupiter.nls.TranslationKey;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    ADMINISTRATE_COMMUNICATION_ADMINISTRATION(Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, "Administrate"),
    VIEW_COMMUNICATION_ADMINISTRATION(Constants.VIEW_COMMUNICATION_ADMINISTRATION, "View"),
    VIEW_COMMUNICATION_ADMINISTRATION_INTERNAL(Constants.VIEW_COMMUNICATION_ADMINISTRATION_INTERNAL,"Internal view");

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
        String ADMINISTRATE_COMMUNICATION_ADMINISTRATION = "privilege.administrate.communicationAdministration";
        String VIEW_COMMUNICATION_ADMINISTRATION = "privilege.view.communicationAdministration";
        String VIEW_COMMUNICATION_ADMINISTRATION_INTERNAL = "privilege.view.communicationAdministration.internal";
    }
}

