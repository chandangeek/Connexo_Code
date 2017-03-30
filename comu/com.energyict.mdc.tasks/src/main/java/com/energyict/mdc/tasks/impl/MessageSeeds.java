/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.tasks.TaskService;

import java.util.logging.Level;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (11:00)
 */
public enum MessageSeeds implements MessageSeed {
    SET_CLOCK(1, Keys.CLOCK_TASK_TYPE_SET_CLOCK, "Set the clock"),
    FORCE_CLOCK(2, Keys.CLOCK_TASK_TYPE_FORCE_CLOCK, "Force the clock"),
    SYNC_CLOCK(3, Keys.CLOCK_TASK_TYPE_SYNCHRONIZE_CLOCK, "Synchronize the clock"),
    CAN_NOT_BE_EMPTY(5, Keys.CAN_NOT_BE_EMPTY, "This field is required"),
    MIN_ABOVE_MAX(6, Keys.MIN_MUST_BE_BELOW_MAX, "Invalid range: minimum value exceeds maximum value"),
    MIN_EQUALS_MAX(7, Keys.MIN_EQUALS_MAX, "Invalid range: minimum value equals maximum value "),
    TIMEDURATION_IS_NULL(8, Keys.TIMEDURATION_MUST_BE_POSITIVE, "Field must denote non-0 duration"),
    DUPLICATE_COMTASK_NAME(10, Keys.DUPLICATE_COMTASK_NAME, "Name must be unique"),
    DUPLICATE_PROTOCOL_TASK_TYPE_IN_COMTASK(13, Keys.DUPLICATE_PROTOCOL_TASK_TYPE_IN_COM_TASK, "ComTask contains multiple ProtocolTasks of the same type"),
    VETO_LOG_BOOK_TYPE_DELETION(14, Keys.VETO_LOG_BOOK_TYPE_DELETION, "Log book type ''{0}'' is still in use by the following communication task(s): ''{1}''"),
    VETO_LOAD_PROFILE_TYPE_DELETION(15, Keys.VETO_LOAD_PROFILE_TYPE_DELETION, "Load profile type ''{0}'' is still in use by the following communication task(s): ''{1}''"),
    ONLY_ONE_COMTASK_WITH_FIRMWARE_ALLOWED(16, Keys.ONLY_ONE_COMTASK_WITH_FIRMWARE_ALLOWED, "Only one comtask with the firmware protocol task allowed"),
    ONLY_ONE_PROTOCOLTASK_WHEN_FIRMWARE_UPGRADE(17, Keys.ONLY_ONE_PROTOCOLTASK_WHEN_FIRMWARE_UPGRADE, "Only one protocol task is allowed when defining a firmware upgrade comtask"),
    VETO_REGISTER_GROUP_DELETION(18, Keys.VETO_REGISTER_GROUP_DELETION, "Register group ''{0}'' is still in use by the following communication task(s): ''{1}''");

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

    public static final class Keys {
        public static final String VALUE_TOO_SMALL = TaskService.COMPONENT_NAME+".ValueTooSmall";
        public static final String CAN_NOT_BE_EMPTY = TaskService.COMPONENT_NAME+".CanNotBeEmpty";
        public static final String VALUE_NOT_IN_RANGE = TaskService.COMPONENT_NAME+".ValueNotInRange";
        public static final String MIN_MUST_BE_BELOW_MAX = TaskService.COMPONENT_NAME+".MinMustBeBelowMax";
        public static final String MIN_EQUALS_MAX = TaskService.COMPONENT_NAME+".MinEqualsMax";
        public static final String TIMEDURATION_MUST_BE_POSITIVE = TaskService.COMPONENT_NAME+".TimeDurationMustBePositive";
        public static final String SIZE_TOO_LONG = TaskService.COMPONENT_NAME+".MaxSizeExceeded";
        public static final String DUPLICATE_COMTASK_NAME = TaskService.COMPONENT_NAME + ".comTask.name.duplicated";
        public static final String DUPLICATE_PROTOCOL_TASK_TYPE_IN_COM_TASK = TaskService.COMPONENT_NAME + ".duplicateProtocolTaskInComTask";
        public static final String CLOCK_TASK_TYPE_SET_CLOCK = TaskService.COMPONENT_NAME + ".clockTaskType.setClock";
        public static final String CLOCK_TASK_TYPE_FORCE_CLOCK = TaskService.COMPONENT_NAME + ".clockTaskType.forceClock";
        public static final String CLOCK_TASK_TYPE_SYNCHRONIZE_CLOCK = TaskService.COMPONENT_NAME + ".clockTaskType.synchronizeClock";
        public static final String FIELD_TOO_LONG = TaskService.COMPONENT_NAME + ".fieldSizeIncorrect";
        public static final String VETO_LOG_BOOK_TYPE_DELETION = TaskService.COMPONENT_NAME + ".logBookType.inuse";
        public static final String VETO_LOAD_PROFILE_TYPE_DELETION = TaskService.COMPONENT_NAME + ".loadProfileType.inuse";
        public static final String VETO_REGISTER_GROUP_DELETION = TaskService.COMPONENT_NAME + ".registerGroup.inuse";
        public static final String ONLY_ONE_COMTASK_WITH_FIRMWARE_ALLOWED = TaskService.COMPONENT_NAME + ".firmware.only.one.comtask.allowed";
        public static final String ONLY_ONE_PROTOCOLTASK_WHEN_FIRMWARE_UPGRADE = TaskService.COMPONENT_NAME + ".firmware.only.one.protocoltask";
    }

}

