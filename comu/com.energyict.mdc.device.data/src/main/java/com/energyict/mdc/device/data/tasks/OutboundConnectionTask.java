/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.impl.ServerComTaskExecution;
import com.energyict.mdc.engine.config.OutboundComPortPool;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface OutboundConnectionTask<PCTT extends PartialConnectionTask> extends ConnectionTask<OutboundComPortPool, PCTT>, OutboundConnectionTaskExecutionAspects {

    /**
     * Keeps track of the maximum number of consecutive failures a ConnectionTask can have before marking it as failed.
     *
     * @return the maximum number of consecutive failures this ConnectionTask can have
     */
    int getMaxNumberOfTries();

    /**
     * Gets the counter that keeps track of the number of times
     * the execution of this OutboundConnectionTask has been tried.
     *
     * @return The current try count
     */
    int getCurrentTryCount();

    /**
     * Gets the counter that keeps track of the number of times
     * the execution of this OutboundConnectionTask has been retried.
     *
     * @return The current retry count
     * 0 = no retries yet
     * 1 = first retry
     * ...
     */
    int getCurrentRetryCount();

    /**
     * Tests if the last execution of this ConnectionTask failed.
     * Note that each time the ConnectionTask executes,
     * this flag will be reset.
     *
     * @return <code>true</code> iff the last execution of this ComTaskExecution failed.
     */
    boolean lastExecutionFailed();

    /**
     * Defines the delay before rescheduling this ConnectionTask after a fail
     *
     * @return the time to wait before we may retry after a failing sessions
     */
    TimeDuration getRescheduleDelay();

    /**
     * Applies the {@link ComWindow} to the calculated next execution timestamp
     * of a {@link ServerComTaskExecution} before it is actually applied.
     *
     * @param calculatedNextExecutionTimestamp The calculated next execution timestamp
     * @return The next execution timestamp
     */
    public Instant applyComWindowIfAny(Instant calculatedNextExecutionTimestamp);

}
