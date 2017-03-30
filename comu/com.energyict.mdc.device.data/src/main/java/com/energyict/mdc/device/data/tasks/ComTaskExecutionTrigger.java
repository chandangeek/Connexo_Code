/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import java.time.Instant;

/**
 * A ComTaskExecutionTrigger keeps track of combination of ComTaskExecution and a trigger timestamp,
 * which indicate the timestamp when the ComTaskExecution should be scheduled for execution. The scheduling
 * logic inside ComTaskExecution will during reschedule choose the most optimal reschedule timestamp,
 * being the earliest timestamp of
 * <ul>
 * <li>the timestamp according to regular rescheduling (~thus according to the NextExecutionSpecs)</li>
 * <li>the earliest timestamp of all ComTaskExecutionTriggers for the given ComTaskExecution</li>
 * </ul>
 *
 * @author sva
 * @since 24/06/2016 - 16:55
 */
public interface ComTaskExecutionTrigger {

    ComTaskExecution getComTaskExecution();

    Instant getTriggerTimeStamp();

}