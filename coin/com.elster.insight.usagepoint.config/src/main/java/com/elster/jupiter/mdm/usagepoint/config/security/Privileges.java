/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.security;

import com.elster.jupiter.nls.TranslationKey;

import aQute.bnd.annotation.ProviderType;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    VIEW_VALIDATION_ON_METROLOGY_CONFIGURATION(Constants.VIEW_VALIDATION_ON_METROLOGY_CONFIGURATION, "View validation on metrology configuration"),
    ADMINISTER_VALIDATION_ON_METROLOGY_CONFIGURATION(Constants.ADMINISTER_VALIDATION_ON_METROLOGY_CONFIGURATION, "Administer validation on metrology configuration"),
    VIEW_ESTIMATION_ON_METROLOGY_CONFIGURATION(Constants.VIEW_ESTIMATION_ON_METROLOGY_CONFIGURATION, "View estimation on metrology configuration"),
    ADMINISTER_ESTIMATION_ON_METROLOGY_CONFIGURATION(Constants.ADMINISTER_ESTIMATION_ON_METROLOGY_CONFIGURATION, "Administer estimation on metrology configuration");

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

    @ProviderType
    public interface Constants {
        String VIEW_VALIDATION_ON_METROLOGY_CONFIGURATION = "privilege.view.metrologyConfiguration.validation";
        String ADMINISTER_VALIDATION_ON_METROLOGY_CONFIGURATION = "privilege.administrate.metrologyConfiguration.validation";
        String VIEW_ESTIMATION_ON_METROLOGY_CONFIGURATION = "privilege.view.metrologyConfiguration.estimation";
        String ADMINISTER_ESTIMATION_ON_METROLOGY_CONFIGURATION = "privilege.administrate.metrologyConfiguration.estimation";
    }
}
