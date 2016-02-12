package com.elster.insight.usagepoint.config.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    RESOURCE_METROLOGY_CONFIG("usagePoint.metrologyConfiguration", "Metrology configuration"),
    RESOURCE_METROLOGY_CONFIGURATION_DESCRIPTION("usagePoint.metrologyConfiguration.description", "Manage metrology configuration"),

    //Privileges
    ADMINISTER_ANY_METROLOGY_CONFIG(Constants.ADMINISTER_ANY_METROLOGY_CONFIGURATION, "Administer any metrology configuration"),
    BROWSE_ANY_METROLOGY_CONFIG(Constants.BROWSE_ANY_METROLOGY_CONFIGURATION, "Browse any metrology configuration"),
    ;

    private final String key;
    private final String description;

    Privileges(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public static String[] keys() {
        return Arrays.stream(Privileges.values())
                .map(Privileges::getKey)
                .collect(Collectors.toList())
                .toArray(new String[Privileges.values().length]);
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

    public interface Constants {
        String ADMINISTER_ANY_METROLOGY_CONFIGURATION = "UCR_ADMINISTER_ANY_METROLOGY_CONFIG";
        String BROWSE_ANY_METROLOGY_CONFIGURATION = "UCR_BROWSE_ANY_METROLOGY_CONFIG";
    }
}
