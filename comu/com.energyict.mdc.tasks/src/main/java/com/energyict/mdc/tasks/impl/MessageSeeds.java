/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.tasks.TaskServiceKeys;
import com.energyict.mdc.tasks.TaskService;

import java.util.logging.Level;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (11:00)
 */
public enum MessageSeeds implements MessageSeed {
    SET_CLOCK(1, TaskServiceKeys.CLOCK_TASK_TYPE_SET_CLOCK, "Set the clock"),
    FORCE_CLOCK(2, TaskServiceKeys.CLOCK_TASK_TYPE_FORCE_CLOCK, "Force the clock"),
    SYNC_CLOCK(3, TaskServiceKeys.CLOCK_TASK_TYPE_SYNCHRONIZE_CLOCK, "Synchronize the clock"),
    CAN_NOT_BE_EMPTY(5, TaskServiceKeys.CAN_NOT_BE_EMPTY, "This field is required"),
    MIN_ABOVE_MAX(6, TaskServiceKeys.MIN_MUST_BE_BELOW_MAX, "Invalid range: minimum value exceeds maximum value"),
    MIN_EQUALS_MAX(7, TaskServiceKeys.MIN_EQUALS_MAX, "Invalid range: minimum value equals maximum value "),
    TIMEDURATION_IS_NULL(8, TaskServiceKeys.TIMEDURATION_MUST_BE_POSITIVE, "Field must denote non-0 duration"),
    DUPLICATE_COMTASK_NAME(10, TaskServiceKeys.DUPLICATE_COMTASK_NAME, "Name must be unique"),
    DUPLICATE_PROTOCOL_TASK_TYPE_IN_COMTASK(13, TaskServiceKeys.DUPLICATE_PROTOCOL_TASK_TYPE_IN_COM_TASK, "ComTask contains multiple ProtocolTasks of the same type"),
    VETO_LOG_BOOK_TYPE_DELETION(14, TaskServiceKeys.VETO_LOG_BOOK_TYPE_DELETION, "Log book type ''{0}'' is still in use by the following communication task(s): ''{1}''"),
    VETO_LOAD_PROFILE_TYPE_DELETION(15, TaskServiceKeys.VETO_LOAD_PROFILE_TYPE_DELETION, "Load profile type ''{0}'' is still in use by the following communication task(s): ''{1}''"),
    ONLY_ONE_COMTASK_WITH_FIRMWARE_ALLOWED(16, TaskServiceKeys.ONLY_ONE_COMTASK_WITH_FIRMWARE_ALLOWED, "Only one comtask with the firmware protocol task allowed"),
    ONLY_ONE_PROTOCOLTASK_WHEN_FIRMWARE_UPGRADE(17, TaskServiceKeys.ONLY_ONE_PROTOCOLTASK_WHEN_FIRMWARE_UPGRADE, "Only one protocol task is allowed when defining a firmware upgrade comtask"),
    VETO_REGISTER_GROUP_DELETION(18, TaskServiceKeys.VETO_REGISTER_GROUP_DELETION, "Register group ''{0}'' is still in use by the following communication task(s): ''{1}''");

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public String getModule() {
        return TaskService.COMPONENT_NAME;
    }

}

