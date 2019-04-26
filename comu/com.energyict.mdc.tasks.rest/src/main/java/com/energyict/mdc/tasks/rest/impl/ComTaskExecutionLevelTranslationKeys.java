/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

import com.energyict.mdc.tasks.ComTaskUserAction;

import java.util.stream.Stream;

public enum ComTaskExecutionLevelTranslationKeys implements TranslationKey {

    LEVEL_1(ComTaskUserAction.EXECUTE_SCHEDULE_PLAN_COM_TASK_1, "Level 1"),
    LEVEL_2(ComTaskUserAction.EXECUTE_SCHEDULE_PLAN_COM_TASK_2, "Level 2"),
    LEVEL_3(ComTaskUserAction.EXECUTE_SCHEDULE_PLAN_COM_TASK_3, "Level 3"),
    LEVEL_4(ComTaskUserAction.EXECUTE_SCHEDULE_PLAN_COM_TASK_4, "Level 4");

    private final ComTaskUserAction level;
    private final String defaultFormat;

    ComTaskExecutionLevelTranslationKeys(ComTaskUserAction level, String defaultFormat) {
        this.level = level;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return level.getPrivilege();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public static ComTaskExecutionLevelTranslationKeys from(String privilege) {
        return Stream.of(values()).filter(k -> k.level.getPrivilege().equals(privilege)).findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown or unsupported com task execution privilege level:" + privilege));
    }

}