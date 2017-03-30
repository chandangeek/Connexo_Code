/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import java.time.Instant;

/**
 * Simple value holder for the earliest next execution timestamp and related priority of a task.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-14 (14:12)
 */
public class EarliestNextExecutionTimeStampAndPriority {

    public Instant earliestNextExecutionTimestamp;
    public int priority;

    public EarliestNextExecutionTimeStampAndPriority(Instant earliestNextExecutionTimestamp, int priority) {
        super();
        this.earliestNextExecutionTimestamp = earliestNextExecutionTimestamp;
        this.priority = priority;
    }

}