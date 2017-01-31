/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_TOU_CALENDARS("calendars.timeOfUse", "Calendars"),
    RESOURCE_TOU_CALENDARS_DESCRIPTION("calendars.timeOfUse.description", "Manage calendars"),

    //Privileges
    MANAGE_TOU_CALENDARS(Constants.MANAGE_TOU_CALENDARS, "Administrate");

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
        String MANAGE_TOU_CALENDARS = "privilege.administrate.touCalendars";
    }
}
