/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config;

/**
 * Models the privileges of the device life cycle bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (11:02)
 */

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.common.device.lifecycle.config.Constants;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_DEVICE_LIFECYCLE("deviceLifeCycleAdministration.deviceLifeCycleAdministrations", "Device life cycle"),
    RESOURCE_DEVICE_LIFECYCLE_DESCRIPTION("deviceLifeCycleAdministration.deviceLifeCycleAdministrations.description", "Manage device life cycle"),
    RESOURCE_DEVICE_LIFECYCLE_LEVELS("deviceLifeCycle.deviceLifeCycle", "Device life cycle access levels"),
    RESOURCE_DEVICE_LIFECYCLE_LEVELS_DESCRIPTION("deviceLifeCycle.deviceLifeCycle.description", "Manage device life cycle access levels"),

    //Privileges
    VIEW_DEVICE_LIFE_CYCLE(Constants.VIEW_DEVICE_LIFE_CYCLE, "View"),
    CONFIGURE_DEVICE_LIFE_CYCLE(Constants.CONFIGURE_DEVICE_LIFE_CYCLE, "Administrate"),
    INITIATE_ACTION_1(Constants.INITIATE_ACTION_1, "Initiate level 1"),
    INITIATE_ACTION_2(Constants.INITIATE_ACTION_2, "Initiate level 2"),
    INITIATE_ACTION_3(Constants.INITIATE_ACTION_3, "Initiate level 3"),
    INITIATE_ACTION_4(Constants.INITIATE_ACTION_4, "Initiate level 4");

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
}