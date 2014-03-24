package com.energyict.mdc.tasks.task.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.tasks.task.TaskService;
import java.util.logging.Level;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (11:00)
 */
public enum MessageSeeds implements MessageSeed {
    SET_CLOCK(1, "clockTaskType.setClock", "Set the clock", Level.SEVERE),
    FORCE_CLOCK(2, "clockTaskType.forceClock", "Force the clock", Level.SEVERE),
    SYNC_CLOCK(3, "clockTaskType.synchronizeClock", "Synchronize the clock", Level.SEVERE),
    SHOULD_BE_AT_LEAST(4, Constants.TSK_VALUE_TOO_SMALL, "Minimal acceptable value is {min}, was {value}", Level.SEVERE),
    CAN_NOT_BE_EMPTY(5, Constants.TSK_CAN_NOT_BE_EMPTY, "This field can not be empty", Level.SEVERE),
    MIN_ABOVE_MAX(6, Constants.TSK_MIN_MUST_BE_BELOW_MAX, "Invalid range: minimum value exceeds maximum value", Level.SEVERE),
    MIN_EQUALS_MAX(7, Constants.TSK_MIN_EQUALS_MAX, "Invalid range: minimum value equals maximum value ", Level.SEVERE),
    TIMEDURATION_IS_NULL(8, Constants.TSK_TIMEDURATION_IS_ZERO, "Field must denote non-0 duration", Level.SEVERE),
    SIZE_TOO_LONG(9, Constants.TSK_SIZE_TOO_LONG, "Field exceeds max size of {max} characters", Level.SEVERE),
    DUPLICATE_COMTASK_NAME(10, Constants.TSK_DUPLICATE_COMTASK_NAME, "A ComTask by this name already exists", Level.SEVERE),
    PROTOCOL_TASK_REQUIRED(11, Constants.TSK_DUPLICATE_COMTASK_NAME, "No protocol task defined for communication task. At least one is required.", Level.SEVERE),
    DUPLICATE_PROTOCOL_TASK_TYPE_IN_COMTASK(12, Constants.TSK_DUPLICATE_PROTOCOL_TASK_TYPE_IN_COM_TASK, "ComTask contains multiple ProtocolTasks of the same type", Level.SEVERE),
    VALUE_NOT_IN_RANGE(999, Constants.TSK_VALUE_NOT_IN_RANGE, "{value} not in range {min} to {max}", Level.SEVERE);

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

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

final class Constants {
    public static final String TSK_VALUE_TOO_SMALL = TaskService.COMPONENT_NAME+".ValueTooSmall";
    public static final String TSK_CAN_NOT_BE_EMPTY = TaskService.COMPONENT_NAME+".CanNotBeEmpty";
    public static final String TSK_VALUE_NOT_IN_RANGE = TaskService.COMPONENT_NAME+".ValueNotInRange";
    public static final String TSK_MIN_MUST_BE_BELOW_MAX = TaskService.COMPONENT_NAME+".MinMustBeBelowMax";
    public static final String TSK_MIN_EQUALS_MAX = TaskService.COMPONENT_NAME+".MinEqualsMax";
    public static final String TSK_TIMEDURATION_IS_ZERO = TaskService.COMPONENT_NAME+".TimeDurationMustBePositive";
    public static final String TSK_SIZE_TOO_LONG = TaskService.COMPONENT_NAME+".MaxSizeExceeded";
    public static final String TSK_DUPLICATE_COMTASK_NAME = TaskService.COMPONENT_NAME + ".comTask.name.duplicated";
    public static final String TSK_COMTASK_WITHOUT_PROTOCOLTASK = TaskService.COMPONENT_NAME + ".comTask.requiresProtocolTask";
    public static final String TSK_DUPLICATE_PROTOCOL_TASK_TYPE_IN_COM_TASK = TaskService.COMPONENT_NAME + ".duplicateProtocolTaskInComTask";
}

