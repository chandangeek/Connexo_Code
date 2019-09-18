/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    // Resources
    RESOURCE_COMMUNICATION_TASK_EXECUTION(
            "communication.task.execution.scheduling",
            "Communication task execution/scheduling"),
    RESOURCE_COMMUNICATION_TASK_EXECUTION_DESCRIPTION(
            "comminication.task.execution.scheduling.description",
            "Execute/plan/schedule communication tasks"),

    // Privileges
    EXECUTE_COM_TASK_1(Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_1, "Level 1"),
    EXECUTE_COM_TASK_2(Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_2, "Level 2"),
    EXECUTE_COM_TASK_3(Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_3, "Level 3"),
    EXECUTE_COM_TASK_4(Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_4, "Level 4");

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
        String EXECUTE_SCHEDULE_PLAN_COM_TASK_1 = "execute.schedule.plan.com.task.level1";
        String EXECUTE_SCHEDULE_PLAN_COM_TASK_2 = "execute.schedule.plan.com.task.level2";
        String EXECUTE_SCHEDULE_PLAN_COM_TASK_3 = "execute.schedule.plan.com.task.level3";
        String EXECUTE_SCHEDULE_PLAN_COM_TASK_4 = "execute.schedule.plan.com.task.level4";
    }

}
