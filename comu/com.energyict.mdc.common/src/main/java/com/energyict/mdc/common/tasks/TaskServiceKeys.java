/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks;

public interface TaskServiceKeys {
    String TASK_SERVICE_COMPONENT_NAME ="CTS";
    String VALUE_TOO_SMALL = TASK_SERVICE_COMPONENT_NAME +".ValueTooSmall";
    String CAN_NOT_BE_EMPTY = TASK_SERVICE_COMPONENT_NAME +".CanNotBeEmpty";
    String VALUE_NOT_IN_RANGE = TASK_SERVICE_COMPONENT_NAME +".ValueNotInRange";
    String MIN_MUST_BE_BELOW_MAX = TASK_SERVICE_COMPONENT_NAME +".MinMustBeBelowMax";
    String MIN_EQUALS_MAX = TASK_SERVICE_COMPONENT_NAME +".MinEqualsMax";
    String TIMEDURATION_MUST_BE_POSITIVE = TASK_SERVICE_COMPONENT_NAME +".TimeDurationMustBePositive";
    String SIZE_TOO_LONG = TASK_SERVICE_COMPONENT_NAME +".MaxSizeExceeded";
    String DUPLICATE_COMTASK_NAME = TASK_SERVICE_COMPONENT_NAME + ".comTask.name.duplicated";
    String DUPLICATE_PROTOCOL_TASK_TYPE_IN_COM_TASK = TASK_SERVICE_COMPONENT_NAME + ".duplicateProtocolTaskInComTask";
    String CLOCK_TASK_TYPE_SET_CLOCK = TASK_SERVICE_COMPONENT_NAME + ".clockTaskType.setClock";
    String CLOCK_TASK_TYPE_FORCE_CLOCK = TASK_SERVICE_COMPONENT_NAME + ".clockTaskType.forceClock";
    String CLOCK_TASK_TYPE_SYNCHRONIZE_CLOCK = TASK_SERVICE_COMPONENT_NAME + ".clockTaskType.synchronizeClock";
    String FIELD_TOO_LONG = TASK_SERVICE_COMPONENT_NAME + ".fieldSizeIncorrect";
    String VETO_LOG_BOOK_TYPE_DELETION = TASK_SERVICE_COMPONENT_NAME + ".logBookType.inuse";
    String VETO_LOAD_PROFILE_TYPE_DELETION = TASK_SERVICE_COMPONENT_NAME + ".loadProfileType.inuse";
    String VETO_REGISTER_GROUP_DELETION = TASK_SERVICE_COMPONENT_NAME + ".registerGroup.inuse";
    String ONLY_ONE_COMTASK_WITH_FIRMWARE_ALLOWED = TASK_SERVICE_COMPONENT_NAME + ".firmware.only.one.comtask.allowed";
    String ONLY_ONE_PROTOCOLTASK_WHEN_FIRMWARE_UPGRADE = TASK_SERVICE_COMPONENT_NAME + ".firmware.only.one.protocoltask";
}
