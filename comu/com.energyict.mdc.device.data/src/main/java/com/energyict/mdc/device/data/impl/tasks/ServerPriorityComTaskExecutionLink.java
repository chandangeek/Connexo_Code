/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;

import java.time.Instant;

public interface ServerPriorityComTaskExecutionLink extends PriorityComTaskExecutionLink {

    /**
     * Attempts to lock this HighPriorityComTaskExecution that is about
     * to be executed on the specified {@link ComPort}
     * and returns <code>true</code> when the lock succeeds
     * and <code>false</code> when the lock fails.
     * Note that this MUST run in an existing transactional context.
     *
     * @return <code>true</code> iff the lock succeeds
     */
    boolean attemptLock(ComPort comPort);

    /**
     * Unlocks this HighPriorityComTaskExecution, basically undoing the effect
     * of the attemptLock method providing that that was successful.
     *
     * @see #attemptLock(ComPort)
     */
    void unlock();

    /**
     * Injects the {@link OutboundConnectionTask} right after
     * the construction to optimize the lazy loading.
     * Note that this will throw an IllegalArgumentException
     * if the id of the injected ServerOutboundConnectionTask
     * is not the same of the ServerOutboundConnectionTask
     * that is configured on this ServerComTaskExecution.
     *
     * @param connectionTask The ServerOutboundConnectionTask
     * @throws IllegalArgumentException
     */
    void injectConnectionTask(OutboundConnectionTask connectionTask);

    void setLockedComPort(ComPort comPort);

    void executionRescheduled(Instant nextExecutionTimestamp);
}
