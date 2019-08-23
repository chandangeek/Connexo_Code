/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks;

import com.energyict.mdc.common.ApplicationException;

/**
 * Models the situation that occurs when the ComServer is already
 * executing as max as allowed number of high priority tasks. In this
 * case no new high priority tasks can be picked up.
 *
 * @author sva
 * @since 11/04/2016 - 11:40
 */
public class NoMoreHighPriorityTasksCanBePickedUpRuntimeException extends ApplicationException {
    public static final String NO_MORE_HIGH_PRIO_TASKS_CAN_BE_PICKED_UP = "No more high priority tasks can be picked up";

    public NoMoreHighPriorityTasksCanBePickedUpRuntimeException() {
        super(NO_MORE_HIGH_PRIO_TASKS_CAN_BE_PICKED_UP);
    }
}