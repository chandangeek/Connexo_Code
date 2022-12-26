/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.device.data.ConnectionInitiationTask;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.InboundConnectionTask;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Arrays;
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
     * @param device The Device
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
     * Finds the {@link ConnectionTask} with the specified {@link ConnectionFunction} for the specified Device.
     *
     * @param device The Device for which we need to search the ConnectionTask
     * @return The ConnectionTask with the specified ConnectionFunction for the given Device if one exists
     */
    Optional<ConnectionTask> findConnectionTaskByDeviceAndConnectionFunction(Device device, ConnectionFunction connectionFunction);

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
     * @param filter The ConnectionTaskFilter
     * @param pageStart The first ConnectionTask
     * @param pageSize The maximum number of ConnectionTasks
     * @return The List of ConnectionTask
     */
    List<ConnectionTask> findConnectionTasksByFilter(ConnectionTaskFilterSpecification filter, int pageStart, int pageSize);


    /**
     * Finds all {@link ConnectionType}s that match the specified filter.
     *
     * @param filter The ConnectionTaskFilter
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
     * Sets the specified {@link ConnectionTask} as the one having a certain {@link ConnectionFunction}
     * for the Device against which the ConnectionTask was created. If present, the old ConnectionFunction
     * is first cleared.
     *
     * @param connectionTask        The ConnectionTask that will become the one having a certain {@link ConnectionFunction}
     * @param oldConnectionFunction An optional containing the old ConnectionFunction (which should be cleared first), or else an empty optional.
     */
    void setConnectionTaskHavingConnectionFunction(ConnectionTask<?, ?> connectionTask, Optional<ConnectionFunction> oldConnectionFunction);

    /**
     * Clears the {@link ConnectionFunction} for the specified Device.
     *
     * @param connectionTask        the ConnectionTask
     * @param oldConnectionFunction An optional containing the old ConnectionFunction (which should be cleared), or else an empty optional
     */
    void clearConnectionTaskConnectionFunction(ConnectionTask<?, ?> connectionTask, Optional<ConnectionFunction> oldConnectionFunction);

    /**
     * Attempts to lock the {@link ConnectionTask} that is about to be executed
     * by the specified {@link ComPort} and returns the locked ConnectionTask
     * when the lock succeeds and <code>null</code> when the lock fails.
     * Note that this MUST run in an existing transactional context.
     *
     * @param connectionTask The ConnectionTask
     * @param ComPort The ComPort that is about to execute the ConnectionTask
     * @return <code>true</code> iff the lock succeeds
     */
    <T extends ConnectionTask> T attemptLockConnectionTask(T connectionTask, ComPort comPort);

    /**
     * @deprecated Pls use {@link #findAndLockConnectionTaskById(long)} instead.
     */
    @Deprecated
    <T extends ConnectionTask> T attemptLockConnectionTask(long id);

    /**
     * Removes the business lock on the specified {@link ConnectionTask},
     * making it available for other {@link ComPort}s to execute the ConnectionTask.
     *
     * @param connectionTask The ConnectionTask
     */
    void unlockConnectionTask(ConnectionTask connectionTask);

    /**
     * Attempts to remove the business lock (comPort) from the specified {@link ConnectionTask},
     * making it available for other {@link ComPort}s to execute the ConnectionTask.
     * First, it tries to set a database lock on the corresponding row (using SELECT FOR UPDATE NOWAIT). Only if this succeeds,
     * the business lock is removed (otherwise, it would hang the calling thread indefinitely, until the locking session releases the row).
     *
     * @param connectionTask the ConnectionTask to be unlocked
     * @return true if the business lock was removed
     */
    boolean attemptUnlockConnectionTask(ConnectionTask connectionTask);

    /**
     * Update the given connectionTask with given ProtocolDialectConfigurationProperties
     *
     * @param connectionTask ConnectionTask to update
     * @param properties     to update the ConnectionTask with
     * @return the updated ConnectionTask
     */
    ConnectionTask updateProtocolDialectConfigurationProperties(ConnectionTask connectionTask, ProtocolDialectConfigurationProperties properties);

    /**
     * Cleans up any marker flags on {@link ConnectionTask}s that were not properly
     * cleaned because the {@link ComPort} they were running
     * on was actually forcefully shutdown, i.e. not allowing it to
     * shut down running processing and cleanup when done.
     * Leaving the marker flags, prohibits the ComPort from
     * picking up the tasks again.
     * This is intended to be called at startup time.
     *
     * @param comPort The ComPort that is currently starting up.
     */
    void releaseInterruptedConnectionTasks(ComPort comPort);

    List<ComSession> findAllSessionsFor(ConnectionTask<?, ?> connectionTask);

    ComSessionBuilder buildComSession(ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Instant startTime);

    Optional<ComSession> findComSession(long id);

    List<ComSession> findComSessions(ComPort comPort);

    List<ComSession> findComSessions(ComPortPool comPortPool);

    /**
     * Finds all ConnectionTasks locked by a specific ComPort
     */
    List<ConnectionTask> findLockedByComPort(ComPort comPort);

    Optional<ConnectionTask> findAndLockConnectionTaskById(long id);

    /**
     * Finds all ConnectionTasks that are running on {@link OutboundComPort}s of the {@link ComPort}
     * for a period of time that is longer than the task execution timeout specified
     * on the {@link OutboundComPortPool} they are contained in.
     */
    List<ConnectionTask> findTimedOutConnectionTasksByComPort(ComPort comPort);

    long getConnectionTasksCount(ConnectionTaskFilterSpecification filter);

    default List<PartialConnectionTask> findPartialConnectionTasks() {
        return Arrays.asList();
    }
}
