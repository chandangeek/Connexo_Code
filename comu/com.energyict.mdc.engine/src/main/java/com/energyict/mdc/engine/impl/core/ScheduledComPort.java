/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.HighPriorityComJob;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;

import java.util.List;
import java.util.Map;

/**
 * Models the scheduling aspects of a {@link OutboundComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (10:05)
 */
public interface ScheduledComPort extends ComPortServerProcess {

    /**
     * Gets the {@link OutboundComPort} that is scheduled via this ScheduledComPort.
     *
     * @return The OutboundComPort
     */
    OutboundComPort getComPort();


    /**
     * @return the number of active thread
     */
    int getActiveThreadCount();

    /**
     * Receives notification from the ComServer that the
     * changes interpoll delay changed.
     *
     * @param changesInterpollDelay The new scheduling interpoll delay
     * @see ComServer#getChangesInterPollDelay()
     */
    void changesInterpollDelayChanged(TimeDuration changesInterpollDelay);

    /**
     * Receives notification from the ComServer that the
     * scheduling interpoll delay changed.
     *
     * @param schedulingInterpollDelay The new scheduling interpoll delay
     * @see ComServer#getSchedulingInterPollDelay()
     */
    void schedulingInterpollDelayChanged(TimeDuration schedulingInterpollDelay);

    /**
     * If the log level of the ComServer was changed, used this method to propagate the changes
     */
    void updateLogLevel(OutboundComPort comPort);

    /**
     * Tests if this ScheduledComPort is currently executing one of specified {@link ComTaskExecution}s.
     *
     * @param comTaskExecutions The list of ComTaskExecution
     * @return A flag that indicates if this ScheduledComPort is executing one of the ComTaskExecutions
     */
    boolean isExecutingOneOf(List<ComTaskExecution> comTaskExecutions);

    /**
     * Tests if this ScheduledComPort is currently connected to the specified {@link OutboundConnectionTask}.
     *
     * @param connectionTask The OutboundConnectionTask
     * @return A flag that indicates if this ScheduledComPort is currently connected to the OutboundConnectionTask
     */
    boolean isConnectedTo(OutboundConnectionTask connectionTask);

    /**
     * Retrieves the actual load of high priority tasks per {@link ComPortPool}.<br/>
     * Or in other words: the number of high priority tasks which are currently executed (or are ready for execution) per ComPortPool
     *
     * @return A map containing the number of the high priority tasks which are currently executed per ComPortPool
     */
    Map<Long, Integer> getHighPriorityLoadPerComPortPool();

    /**
     * Interrupts any ongoing work currently being executed by this ScheduledComPort
     * with proper cleanup and immediately starts the execution of the specified {@link HighPriorityComJob}.
     *
     * @param job The ComJob that needs to be executed with high priority
     */
    void executeWithHighPriority(HighPriorityComJob job);

    void reload(OutboundComPort comPort);

}