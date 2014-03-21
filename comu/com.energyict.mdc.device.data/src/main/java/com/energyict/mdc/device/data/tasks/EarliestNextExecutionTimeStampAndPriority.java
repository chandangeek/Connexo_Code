package com.energyict.mdc.device.data.tasks;

import java.util.Date;

/**
 * Simple value holder for the earliest next execution timestamp and related priority of a task.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-14 (14:12)
 */
public class EarliestNextExecutionTimeStampAndPriority {

    public Date earliestNextExecutionTimestamp;
    public int priority;

    public EarliestNextExecutionTimeStampAndPriority(Date earliestNextExecutionTimestamp, int priority) {
        super();
        this.earliestNextExecutionTimestamp = earliestNextExecutionTimestamp;
        this.priority = priority;
    }

}