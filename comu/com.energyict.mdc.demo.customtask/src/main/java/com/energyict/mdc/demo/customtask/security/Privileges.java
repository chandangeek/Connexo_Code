/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.demo.customtask.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_DEMO_CUSTOM_TASK("MDCDemoCustomTask", "Demo custom task for MDC"),
    RESOURCE_DEMO_CUSTOM_TASK_DESCRIPTION("MDCDemoCustomTask.description", "Manage demo custom task for MDC"),

    //Privileges
    ADMINISTRATE_DEMO_CUSTOM_TASK(Constants.ADMINISTRATE_MDC_DEMO_CUSTOM_TASK, "Administrate"),
    VIEW_DEMO_CUSTOM_TASK(Constants.VIEW_MDC_DEMO_CUSTOM_TASK, "View"),
    RUN_DEMO_CUSTOM_TASK(Constants.RUN_MDC_DEMO_CUSTOM_TASK, "Run");

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
        String ADMINISTRATE_MDC_DEMO_CUSTOM_TASK = "privilege.administrate.MDCDemoCustomTask";
        String VIEW_MDC_DEMO_CUSTOM_TASK = "privilege.view.MDCDemoCustomTask";
        String RUN_MDC_DEMO_CUSTOM_TASK = "privilege.run.MDCDemoCustomTask";
    }
}
