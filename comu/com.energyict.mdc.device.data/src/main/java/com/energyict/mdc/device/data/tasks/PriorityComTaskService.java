/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.HighPriorityComJob;
import com.energyict.mdc.common.comserver.OutboundCapableComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides services that relate to {@link PriorityComTaskExecutionLink}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (13:30)
 */
@ProviderType
public interface PriorityComTaskService {

    PriorityComTaskExecutionLink from(ComTaskExecution comTaskExecution);

    /**
     * Finds the {@link PriorityComTaskExecutionLink} that is uniquely identified
     * by the specified integer value.
     *
     * @param id The unique identifier
     * @return The HighPriorityComTaskExecution or <code>null</code> if no such HighPriorityComTaskExecution exists
     */
    Optional<PriorityComTaskExecutionLink> find(int id);

    /**
     * Finds the {@link PriorityComTaskExecutionLink} for the specified {@link ComTaskExecution}.
     *
     * @param comTaskExecution The ComTaskE
     * @return The HighPriorityComTaskExecution or <code>null</code> if no such HighPriorityComTaskExecution exists
     */
    Optional<PriorityComTaskExecutionLink> findByComTaskExecution(ComTaskExecution comTaskExecution);

    /**
     * Finds and returns all {@link HighPriorityComJob}s that are
     * ready to be executed by one of the {@link OutboundComPort}s
     * of the specified {@link OutboundCapableComServer}.
     * <b>Note:</b> The actual load of high priority tasks (~ the number of high priority tasks which are currently executed)
     * mapped per {@link ComPortPool} is also provided. This information can be used to determine
     * the maximum number of additional high priority tasks which can be picked up per {@link ComPortPool}.
     *
     * @param comServer The OutboundCapableComServer
     * @param currentHighPriorityLoadPerComPortPool A map containing the number of the high priority tasks which are currently executed per ComPortPool
     * @return The List of ComJob
     */
    List<HighPriorityComJob> findExecutable(OutboundCapableComServer comServer, Map<Long, Integer> currentHighPriorityLoadPerComPortPool);

    /**
     * Finds and returns all {@link HighPriorityComJob}s that will
     * be ready to be executed by one of the {@link OutboundComPort}s
     * of the specified {@link OutboundCapableComServer} on the specified Date.
     * <b>Note:</b> The actual load of high priority tasks (~ the number of high priority tasks which are currently executed)
     * mapped per {@link ComPortPool} is also provided. This information can be used to determine
     * the maximum number of additional high priority tasks which can be picked up per {@link ComPortPool}.
     *
     * @param comServer The OutboundCapableComServer
     * @param currentHighPriorityLoadPerComPortPool A map containing the number of the high priority tasks which are currently executed per ComPortPool
     * @param date The date on which the high priority tasks should be ready to be executed
     * @return The List of ComJob
     */
    List<HighPriorityComJob> findExecutable(OutboundCapableComServer comServer, Map<Long, Integer> currentHighPriorityLoadPerComPortPool, Instant date);

    boolean arePriorityComTasksStillPending(Collection<Long> priorityComTaskExecutionIds);

    PriorityComTaskExecutionLink attemptLockComTaskExecution(PriorityComTaskExecutionLink comTaskExecution, ComPort comPort);
}
