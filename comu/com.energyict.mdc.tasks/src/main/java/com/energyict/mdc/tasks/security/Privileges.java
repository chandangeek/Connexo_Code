/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    // Resources
    RESOURCE_COMMUNICATION_TASK_EXECUTION("comminication.task.execution", "Communication task execution"),
    RESOURCE_COMMUNICATION_TASK_EXECUTION_DESCRIPTION(
            "comminication.task.execution.description",
            "Execute communication tasks"),

    // Privileges
    EXECUTE_COM_TASK_1(Constants.EXECUTE_COM_TASK_1, "Execute level 1"),
    EXECUTE_COM_TASK_2(Constants.EXECUTE_COM_TASK_2, "Execute level 2"),
    EXECUTE_COM_TASK_3(Constants.EXECUTE_COM_TASK_3, "Execute level 3"),
    EXECUTE_COM_TASK_4(Constants.EXECUTE_COM_TASK_4, "Execute level 4");

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
        return getDescription();
    }

    public String getDescription() {
        return description;
    }

    public static String[] keys() {
        return Arrays.stream(Privileges.values()).map(Privileges::getKey).collect(Collectors.toList())
                .toArray(new String[Privileges.values().length]);
    }

    public interface Constants {
        String EXECUTE_COM_TASK_1 = "execute.com.task.level1";
        String EXECUTE_COM_TASK_2 = "execute.com.task.level2";
        String EXECUTE_COM_TASK_3 = "execute.com.task.level3";
        String EXECUTE_COM_TASK_4 = "execute.com.task.level4";
    }

}
