/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Provides services that relate to {@link ConnectionTask}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (08:56)
 */
@ProviderType
public interface ConnectionTaskService {

    String FILTER_ITEMIZER_QUEUE_DESTINATION = "ItemizeConnFilterQD";
    String FILTER_ITEMIZER_QUEUE_SUBSCRIBER = "ItemizeConnFilterQS";
    String FILTER_ITEMIZER_QUEUE_DISPLAYNAME = "Itemize connection rescheduling from filter";
    String CONNECTION_RESCHEDULER_QUEUE_DESTINATION = "ReschConnQD";
    String CONNECTION_RESCHEDULER_QUEUE_SUBSCRIBER = "ReschConnQS";
    String CONNECTION_RESCHEDULER_QUEUE_DISPLAY_NAME = "Handle connection rescheduling";
    String FILTER_ITEMIZER_PROPERTIES_QUEUE_DESTINATION = "ItemizeConnPropFilterQD";
    String FILTER_ITEMIZER_PROPERTIES_QUEUE_SUBSCRIBER = "ItemizeConnPropFilterQS";
    String FILTER_ITEMIZER_PROPERTIES_QUEUE_DISPLAY_NAME = "Itemize connection attribute updates from filter";
    String CONNECTION_PROP_UPDATER_QUEUE_DESTINATION = "PropUpConnQD";
    String CONNECTION_PROP_UPDATER_QUEUE_SUBSCRIBER = "PropUpConnQS";
    String CONNECTION_PROP_UPDATER_QUEUE_DISPLAY_NAME = "Handle connection attribute updates";

    Optional<ConnectionTask> findConnectionTask(long id);

    Optional<ConnectionTask> findAndLockConnectionTaskByIdAndVersion(long id, long version);

    Optional<OutboundConnectionTask> findOutboundConnectionTask(long id);

    Optional<InboundConnectionTask> findInboundConnectionTask(long id);

    Optional<ScheduledConnectionTask> findScheduledConnectionTask(long id);

    Optional<ConnectionInitiationTask> findConnectionInitiationTask(long id);

    /**
     * Finds the {@link ConnectionTask} on the specified Device
     * that uses the specified {@link PartialConnectionTask}.
     * Note that there can be only one such ConnectionTask.
     *
     * @param partialConnectionTask The PartialConnectionTask
     * @param device                The Device
     * @return The ConnectionTask or <code>Optional.empty()</code> if there is no such ConnectionTask yet
     */
    Optional<ConnectionTask> findConnectionTaskForPartialOnDevice(PartialConnectionTask partialConnectionTask, Device device);

    /**
     * Finds the {@link ConnectionTask}s that are configured
     * against the specified Device.
     *
     * @param device the Device
     * @return the List of ConnectionTask
     */
    List<ConnectionTask> findConnectionTasksByDevice(Device device);

    /**
     * Finds the all {@link ConnectionTask}s, i.e. including the obsolete ones,
     * that are configured against the specified Device.
     *
     * @param device the Device
     * @return the List of ConnectionTask
     * @see ConnectionTask#isObsolete()
     */
    List<ConnectionTask> findAllConnectionTasksByDevice(Device device);

    /**
     * Finds the {@link InboundConnectionTask}s that are configured
     * against the specified Device.
     *
     * @param device the Device
     * @return the List of InboundConnectionTask
     */
    List<InboundConnectionTask> findInboundConnectionTasksByDevice(Device device);

    /**
     * Finds the {@link ScheduledConnectionTask}s that are configured
     * against the specified Device.
     *
     * @param device the Device
     * @return the List of ScheduledConnectionTask
     */
    List<ScheduledConnectionTask> findScheduledConnectionTasksByDevice(Device device);

    /**
     * Finds the default {@link ConnectionTask} for the specified Device.
     *
     * @param device The Device for which we need to search the default ConnectionTask
     * @return The default ConnectionTask for the given Device if one exists
     */
    Optional<ConnectionTask> findDefaultConnectionTaskForDevice(Device device);

    /**
     * Finds all the {@link ConnectionTask}s with the specified {@link TaskStatus}.
     *
     * @param status The TaskStatus
     * @return The ConnectionTasks with the specified TaskStatus
     */
    List<ConnectionTask> findConnectionTasksByStatus(TaskStatus status);

    /**
     * Finds all {@link ConnectionTask}s that match the specified filter.
     *
     * @param filter    The ConnectionTaskFilter
     * @param pageStart The first ConnectionTask
     * @param pageSize  The maximum number of ConnectionTasks
     * @return The List of ConnectionTask
     */
    List<ConnectionTask> findConnectionTasksByFilter(ConnectionTaskFilterSpecification filter, int pageStart, int pageSize);


    /**
     * Finds all {@link ConnectionType}s that match the specified filter.
     *
     * @param filter    The ConnectionTaskFilter
     * @return The List of ConnectionTypes
     */
    List<ConnectionTypePluggableClass> findConnectionTypeByFilter(ConnectionTaskFilterSpecification filter);

    /**
     * Sets the specified {@link ConnectionTask} as the default for the Device
     * against which the ConnectionTask was created.
     * Note that there can be only 1 default per Device so when there is already
     * another default ConnectionTask marked as the default, the existing
     * ConnectionTask will no longer be the default after this call.
     * This will impact existing {@link ComTaskExecution}s
     * that are linked to the old default as these will now relate to this ConnectionTask.
     *
     * @param connectionTask The ConnectionTask that will become the default
     */
    void setDefaultConnectionTask(ConnectionTask connectionTask);

    /**
     * Clears the marker flag on the default {@link ConnectionTask} for the specified Device.
     *
     * @param device The Device
     */
    void clearDefaultConnectionTask(Device device);

    /**
     * Attempts to lock the {@link ConnectionTask} that is about to be executed
     * by the specified {@link ComServer} and returns the locked ConnectionTask
     * when the lock succeeds and <code>null</code> when the lock fails.
     * Note that this MUST run in an existing transactional context.
     *
     * @param connectionTask The ConnectionTask
     * @param comServer      The ComServer that is about to execute the ConnectionTask
     * @return <code>true</code> iff the lock succeeds
     */
    <T extends ConnectionTask> T attemptLockConnectionTask(T connectionTask, ComServer comServer);

    <T extends ConnectionTask> T attemptLockConnectionTask(long id);

    /**
     * Removes the business lock on the specified {@link ConnectionTask},
     * making it available for other {@link ComServer}s to execute the ConnectionTask.
     *
     * @param connectionTask The ConnectionTask
     */
    void unlockConnectionTask(ConnectionTask connectionTask);

    /**
     * Cleans up any marker flags on {@link ConnectionTask}s that were not properly
     * cleaned because the {@link ComServer} they were running
     * on was actually forcefully shutdown, i.e. not allowing it to
     * shut down running processing and cleanup when done.
     * Leaving the marker flags, prohibits the ComServer from
     * picking up the tasks again.
     * This is intended to be called at startup time.
     *
     * @param comServer The ComServer that is currently starting up.
     */
    void releaseInterruptedConnectionTasks(ComServer comServer);

    /**
     * Cleans up any marker flags on {@link ConnectionTask}s that are running
     * on {@link com.energyict.mdc.engine.config.OutboundComPort}s of the {@link ComServer}
     * for a period of time that is longer than the task execution timeout specified
     * on the {@link com.energyict.mdc.engine.config.OutboundComPortPool} they are contained in.
     *
     * @param outboundCapableComServer The ComServer
     */
    void releaseTimedOutConnectionTasks(ComServer outboundCapableComServer);

    List<ComSession> findAllSessionsFor(ConnectionTask<?, ?> connectionTask);

    ComSessionBuilder buildComSession(ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Instant startTime);

    Optional<ComSession> findComSession(long id);

    List<ComSession> findComSessions(ComPort comPort);

    List<ComSession> findComSessions(ComPortPool comPortPool);

    /**
     * Finds all ConnectionTasks locked by a specific ComServer
     */
    public List<ConnectionTask> findLockedByComServer(ComServer comServer);

}