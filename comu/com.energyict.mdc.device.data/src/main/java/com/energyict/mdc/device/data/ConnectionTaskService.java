package com.energyict.mdc.device.data;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import com.google.common.base.Optional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Provides services that relate to {@link ConnectionTask}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (08:56)
 */
public interface ConnectionTaskService {

    public Optional<ConnectionTask> findConnectionTask(long id);

    public Optional<OutboundConnectionTask> findOutboundConnectionTask(long id);

    public Optional<InboundConnectionTask> findInboundConnectionTask(long id);

    public Optional<ScheduledConnectionTask> findScheduledConnectionTask(long id);

    public Optional<ConnectionInitiationTask> findConnectionInitiationTask(long id);

    /**
     * Finds the {@link ConnectionTask} on the specified Device
     * that uses the specified {@link PartialConnectionTask}.
     * Note that there can be only one such ConnectionTask.
     *
     * @param partialConnectionTask The PartialConnectionTask
     * @param device The Device
     * @return The ConnectionTask or <code>null</code> if there is no such ConnectionTask yet
     */
    public Optional<ConnectionTask> findConnectionTaskForPartialOnDevice(PartialConnectionTask partialConnectionTask, Device device);

    /**
     * Finds the {@link ConnectionTask}s that are configured
     * against the specified Device.
     *
     * @param device the Device
     * @return the List of ConnectionTask
     */
    public List<ConnectionTask> findConnectionTasksByDevice(Device device);

    /**
     * Finds the all {@link ConnectionTask}s, i.e. including the obsolete ones,
     * that are configured against the specified Device.
     *
     * @param device the Device
     * @return the List of ConnectionTask
     * @see ConnectionTask#isObsolete()
     */
    public List<ConnectionTask> findAllConnectionTasksByDevice(Device device);

    /**
     * Finds the {@link InboundConnectionTask}s that are configured
     * against the specified Device.
     *
     * @param device the Device
     * @return the List of InboundConnectionTask
     */
    public List<InboundConnectionTask> findInboundConnectionTasksByDevice(Device device);

    /**
     * Finds the {@link ScheduledConnectionTask}s that are configured
     * against the specified Device.
     *
     * @param device the Device
     * @return the List of ScheduledConnectionTask
     */
    public List<ScheduledConnectionTask> findScheduledConnectionTasksByDevice(Device device);

    /**
     * Finds the default {@link ConnectionTask} for the specified Device.
     * The search will start at the Device but if none is found there,
     * it will continue to the gateway level (if the Device has a gateway of course).
     *
     * @param device The Device for which we need to search the default ConnectionTask
     * @return The default ConnectionTask for the given Device or <code>null</code> if none was found
     */
    public ConnectionTask findDefaultConnectionTaskForDevice(Device device);

    /**
     * Finds all the {@link ConnectionTask}s with the specified {@link TaskStatus}.
     *
     * @param status The TaskStatus
     * @return The ConnectionTasks with the specified TaskStatus
     */
    public List<ConnectionTask> findConnectionTasksByStatus(TaskStatus status);

    /**
     * Counts all {@link ConnectionTask}s in the system,
     * grouping them by their respective {@link TaskStatus}.
     *
     * @return The numbers, broken down by TaskStatus
     */
    public Map<TaskStatus, Long> getConnectionTaskStatusCount();

    /**
     * Counts all {@link ConnectionTask}s that match the specified filter,
     * grouping them by their respective {@link TaskStatus}.
     *
     * @param filter The ConnectionTaskFilter
     * @return The numbers, broken down by TaskStatus
     */
    public Map<TaskStatus, Long> getConnectionTaskStatusCount(ConnectionTaskFilterSpecification filter);

    /**
     * Finds all {@link ConnectionTask}s that match the specified filter.
     *
     * @param filter The ConnectionTaskFilter
     * @param pageStart The first ConnectionTask
     * @param pageSize The maximum number of ConnectionTasks
     * @return The List of ConnectionTask
     */
    public List<ConnectionTask> findConnectionTasksByFilter(ConnectionTaskFilterSpecification filter, int pageStart, int pageSize);

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
    public void setDefaultConnectionTask(ConnectionTask connectionTask);

    /**
     * Clears the marker flag on the default {@link ConnectionTask} for the specified Device.
     *
     * @param device The Device
     */
    public void clearDefaultConnectionTask(Device device);

    /**
     * Attempts to lock the {@link ConnectionTask} that is about to be executed
     * by the specified {@link ComServer} and returns the locked ConnectionTask
     * when the lock succeeds and <code>null</code> when the lock fails.
     * Note that this MUST run in an existing transactional context.
     *
     * @param connectionTask The ConnectionTask
     * @param comServer The ComServer that is about to execute the ConnectionTask
     * @return <code>true</code> iff the lock succeeds
     */
    public <T extends ConnectionTask> T attemptLockConnectionTask(T connectionTask, ComServer comServer);

    /**
     * Removes the business lock on the specified {@link ConnectionTask},
     * making it available for other {@link ComServer}s to execute the ConnectionTask.
     *
     * @param connectionTask The ConnectionTask
     */
    public void unlockConnectionTask(ConnectionTask connectionTask);

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
    public void releaseInterruptedConnectionTasks(ComServer comServer);

    /**
     * Cleans up any marker flags on {@link ConnectionTask}s that are running
     * on {@link com.energyict.mdc.engine.model.OutboundComPort}s of the {@link ComServer}
     * for a period of time that is longer than the task execution timeout specified
     * on the {@link com.energyict.mdc.engine.model.OutboundComPortPool} they are contained in.
     *
     * @param outboundCapableComServer The ComServer
     */
    public void releaseTimedOutConnectionTasks(ComServer outboundCapableComServer);

    List<ComSession> findAllSessionsFor(ConnectionTask<?, ?> connectionTask);

    ComSessionBuilder buildComSession(ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Date startTime);

    Optional<ComSession> findComSession(long id);

    List<ComSession> findComSessions(ComPort comPort);

    List<ComSession> findComSessions(ComPortPool comPortPool);

    /**
     * Counts the number of {@link ConnectionTask}s whose last {@link ComSession}
     * completed successfully but has at least one failing task.
     * Note that the status of the task is not taken into account at all.
     *
     * @return The count
     */
    public long countConnectionTasksLastComSessionsWithAtLeastOneFailedTask();

    /**
     * Counts the number of {@link ConnectionTask}s that are currently waiting
     * to be executed and whose last {@link ComSession}
     * completed successfully but has at least one failing task.
     *
     * @return The count
     */
    public long countWaitingConnectionTasksLastComSessionsWithAtLeastOneFailedTask();

    /**
     * Counts the last {@link ComSession} of all {@link ConnectionTask}s,
     * grouping them by their respective {@link ConnectionTask.SuccessIndicator}.
     *
     * @return The numbers, broken down by SuccessIndicator
     */
    public Map<ComSession.SuccessIndicator, Long> getConnectionTaskLastComSessionSuccessIndicatorCount();

    /**
     * Counts all {@link ConnectionTask}s grouping them by their
     * respective {@link ConnectionTypePluggableClass} and the {@link ComSession.SuccessIndicator}
     * of the last {@link ComSession}.
     * The counters are returned in the following order:
     * <ol>
     * <li>Success but with at least one failing task</li>
     * <li>Success</li>
     * <li>SetupError</li>
     * <li>Broken</li>
     * </ol>
     *
     * @return The counters
     */
    public Map<ConnectionTypePluggableClass, List<Long>> getConnectionTypeHeatMap();

    /**
     * Counts all {@link ConnectionTask}s grouping them by their
     * respective {@link DeviceType} and the {@link ComSession.SuccessIndicator}
     * of the last {@link ComSession}.
     * The counters are returned in the following order:
     * <ol>
     * <li>Success but with at least one failing task</li>
     * <li>Success</li>
     * <li>SetupError</li>
     * <li>Broken</li>
     * </ol>
     *
     * @return The counters
     */
    public Map<DeviceType, List<Long>> getConnectionsDeviceTypeHeatMap();

    /**
     * Counts all {@link ConnectionTask}s grouping them by their
     * respective {@link ComPortPool} and the {@link ComSession.SuccessIndicator}
     * of the last {@link ComSession}.
     * The counters are returned in the following order:
     * <ol>
     * <li>Success but with at least one failing task</li>
     * <li>Success</li>
     * <li>SetupError</li>
     * <li>Broken</li>
     * </ol>
     *
     * @return The counters
     */
    public Map<ComPortPool, List<Long>> getConnectionsComPortPoolHeatMap();

}