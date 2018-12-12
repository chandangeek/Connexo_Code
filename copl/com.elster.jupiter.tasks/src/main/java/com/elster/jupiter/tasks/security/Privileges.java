/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    //Resources
    RESOURCE_TASKS("task.tasks", "Tasks"),
    RESOURCE_TASKS_DESCRIPTION("task.tasks.description", "Manage tasks"),

    //Privileges
    VIEW_TASK_OVERVIEW(Constants.VIEW_TASK_OVERVIEW, "View task overview");

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
        String VIEW_TASK_OVERVIEW = "privilege.view.ViewTaskOverview";
    }


}