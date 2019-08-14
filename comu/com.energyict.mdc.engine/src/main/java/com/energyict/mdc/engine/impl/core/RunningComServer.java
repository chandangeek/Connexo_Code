/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.HighPriorityComJob;
import com.energyict.mdc.common.comserver.OutboundComPort;

import java.util.Map;

/**
 * Models the aspects of a {@link ComServer} that is actually running.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (09:55)
 */
public interface RunningComServer extends ServerProcess {

    ComServer getComServer();

    ComServerDAO getComServerDAO();

    boolean isRemoteQueryApiStarted();

    int getCollectedDataStorageCapacity();

    int getCurrentCollectedDataStorageSize();

    int getCurrentCollectedDataStorageLoadPercentage();

    int getNumberOfCollectedDataStorageThreads();

    int getCollectedDataStorageThreadPriority();

    String getAcquiredTokenThreadNames();

    void eventClientRegistered();

    void eventClientUnregistered();

    void eventWasPublished();

    void refresh(ComPort comPort);

    /**
     * Tests if this RunningComServer is capable of executing
     * high priority tasks. It can only do this if at least
     * one of its {@link OutboundComPort}s is active.
     *
     * @return A flag that indicates if this RunningComServer will accept high priority tasks for execution
     */
    boolean canExecuteTasksWithHighPriority();

    /**
     * Retrieves the actual load of high priority tasks per {@link ComPortPool}.<br/>
     * Or in other words: the number of high priority tasks which are currently executed per ComPortPool
     *
     * @return A map containing the number of the high priority tasks which are currently executed per ComPortPool
     */
    Map<Long, Integer> getHighPriorityLoadPerComPortPool();

    /**
     * Executes the specified {@link HighPriorityComJob} with high priority.
     * This will pick an appropriate {@link ScheduledComPort},
     * interrupt the ComJob that is currently executing
     * and passing it the high priority ComJob.
     * Picking the appropriate port will take the
     * following into account:
     * <ol>
     * <li>The {@link com.energyict.mdc.common.tasks.ComTaskExecution} that is currently being executed by the port</li>
     * <li>The {@link com.energyict.mdc.common.device.data.ScheduledConnectionTask} that is currently being executed by the port</li>
     * <li>The {@link com.energyict.mdc.common.device.data.Device} that relates to the task that is currently being executed by the port</li>
     * <li>The port's number of active connections</li>
     * </ol>
     *
     * @param job The HighPriorityComJob
     */
    void executeWithHighPriority(HighPriorityComJob job);

}