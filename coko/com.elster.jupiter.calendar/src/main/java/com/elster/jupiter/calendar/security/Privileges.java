package com.elster.jupiter.calendar.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_TOU_CALENDARS("calendars.timeOfUse", "Time of use calendars"),
    RESOURCE_TOU_CALENDARS_DESCRIPTION("calendars.timeOfUse.description", "Manage time of use calendars"),

    //Privileges
    VIEW_TOU_CALENDARS(Constants.VIEW_TOU_CALENDARS, "View time of use calendars"),
    VIEW_TO_PREVIEW(Constants.VIEW_TOU_PREVIEW, "Preview time of use calendar");

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
        String VIEW_TOU_CALENDARS = "privilege.view.touCalendars";
        String VIEW_TOU_PREVIEW = "privilege.view.touPreview";
    }
}
