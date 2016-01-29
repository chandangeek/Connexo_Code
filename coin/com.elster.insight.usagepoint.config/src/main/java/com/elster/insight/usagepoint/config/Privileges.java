package com.elster.insight.usagepoint.config;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    RESOURCE_METROLOGY_CONFIG("usagePoint.metrologyConfiguration", "Metrology configuration"),
    RESOURCE_METROLOGY_CONFIG_DESCR("usagePoint.metrologyConfiguration.description", "Manage metrology configuration"),
    RESOURCE_METROLOGY_CONFIG_CPS("usagePoint.metrologyConfiguration.cps", "CAS for metrology configuration"),
    RESOURCE_METROLOGY_CONFIG_CPS_DESCR("usagePoint.metrologyConfiguration.cps.description", "Manage CAS for metrology configuration"),

    //Privileges
    ADMIN_ANY_METROLOGY_CONFIG(Constants.ADMIN_ANY_METROLOGY_CONFIG, "Administrate any metrology configuration"),
    BROWSE_ANY_METROLOGY_CONFIG(Constants.BROWSE_ANY_METROLOGY_CONFIG, "Browse any metrology configuration"),
    METROLOGY_CPS_VIEW(Constants.METROLOGY_CPS_VIEW, "Browse CAS for metrology configuration"),
    METROLOGY_CPS_ADMIN(Constants.METROLOGY_CPS_ADMIN, "Administrate CAS for metrology configuration"),
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
        String ADMIN_ANY_METROLOGY_CONFIG = "ADMIN_ANY_METROLOGY_CONFIG";
        String BROWSE_ANY_METROLOGY_CONFIG = "BROWSE_ANY_METROLOGY_CONFIG";
        String METROLOGY_CPS_VIEW = "metrology.cps.view";
        String METROLOGY_CPS_ADMIN = "metrology.cps.admin";
    }
}

