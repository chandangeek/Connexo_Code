package com.energyict.mdc.device.data;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CommunicationErrorType;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.scheduling.model.ComSchedule;

import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides services that relate to {@link ComTaskExecution}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (13:30)
 */
public interface CommunicationTaskService {

    public void setOrUpdateDefaultConnectionTaskOnComTaskInDeviceTopology(Device device, ConnectionTask connectionTask);

    public TimeDuration releaseTimedOutComTasks(ComServer comServer);

    /**
     * Cleans up any marker flags on {@link ComTaskExecution}s that were not properly
     * cleaned because the {@link ComServer} they were running
     * on was actually forcefully shutdown, i.e. not allowing it to
     * shut down running processing and cleanup when done.
     * Leaving the marker flags, prohibits the ComServer from
     * picking up the tasks again.
     * This is intended to be called at startup time.
     *
     * @param comServer The ComServer that is currently starting up.
     */
    public void releaseInterruptedComTasks(ComServer comServer);

    /**
     * Finds the ComTaskExecution with the given ID
     *
     * @param id the unique ID of the ComTaskExecution
     * @return the requested ComTaskExecution
     */
    public ComTaskExecution findComTaskExecution(long id);

    /**
     * Counts all {@link ComTaskExecution}s in the system,
     * grouping them by their respective {@link TaskStatus}.
     *
     * @return The numbers, broken down by TaskStatus
     */
    public Map<TaskStatus, Long> getComTaskExecutionStatusCount();

    /**
     * Counts all {@link ComTaskExecution}s that match the specified filter,
     * grouping them by their respective {@link TaskStatus}.
     *
     * @param filter The ComTaskExecutionFilterSpecification
     * @return The numbers, broken down by TaskStatus
     */
    public Map<TaskStatus, Long> getComTaskExecutionStatusCount(ComTaskExecutionFilterSpecification filter);

    /**
     * Counts all {@link ComTaskExecution}s for {@link ComSchedule}s,
     * grouping them by their respective {@link TaskStatus}.
     *
     * @param taskStatuses The Set of TaskStatus
     * @return The numbers, broken down by ComSchedule and TaskStatus
     */
    public Map<ComSchedule, Map<TaskStatus, Long>> getCommunicationTasksComScheduleBreakdown(Set<TaskStatus> taskStatuses);

    /**
     * Counts all {@link ComTaskExecution}s for {@link ComSchedule}s,
     * grouping them by the {@link com.energyict.mdc.device.config.DeviceType}
     * of the related {@link com.energyict.mdc.device.data.Device}.
     *
     * @param taskStatuses The Set of TaskStatus
     * @return The numbers, broken down by DeviceType and TaskStatus
     */
    public Map<DeviceType, Map<TaskStatus, Long>> getCommunicationTasksDeviceTypeBreakdown(Set<TaskStatus> taskStatuses);

    /**
     * Finds all {@link ConnectionTask}s that match the specified filter.
     *
     * @param filter The ComTaskExecutionFilterSpecification
     * @param pageStart The first ComTaskExecution
     * @param pageSize The maximum number of ComTaskExecutions
     * @return The List of ComTaskExecution
     */
    public List<ComTaskExecution> findComTaskExecutionsByFilter(ComTaskExecutionFilterSpecification filter, int pageStart, int pageSize);

    /**
     * Finds all ComTaskExecutions for the given Device which aren't made obsolete yet
     *
     * @param device the device
     * @return the currently active ComTaskExecutions for this device
     */
    public List<ComTaskExecution> findComTaskExecutionsByDevice(Device device);

    /**
     * Finds all the ComTaskExecutions for the given Device, including the ones that have been made obsolete
     *
     * @param device the device
     * @return all ComTaskExecutions which have ever been created for this Device
     */
    public List<ComTaskExecution> findAllComTaskExecutionsIncludingObsoleteForDevice(Device device);

    /**
     * Attempts to lock the ComTaskExecution that is about to be executed
     * by the specified ComPort and returns the locked ComTaskExecution
     * when the lock succeeds and <code>null</code> when the lock fails.
     * Note that this MUST run in an existing transactional context.
     *
     * @param comTaskExecution The ComTaskExecution
     * @param comPort The ComPort that is about to execute the ComTaskExecution
     * @return <code>true</code> iff the lock succeeds
     */
    public ComTaskExecution attemptLockComTaskExecution(ComTaskExecution comTaskExecution, ComPort comPort);

    /**
     * Removes the business lock on the specified ComTaskExecution,
     * making it available for other ComPorts to execute the ComTaskExecution.
     *
     * @param comTaskExecution The ComTaskExecution
     */
    public void unlockComTaskExecution(ComTaskExecution comTaskExecution);

    /**
     * Finds all the ComTaskExecutions which are linked to the given ConnectionTask
     * (and are not obsolete)
     *
     * @param connectionTask the given ConnectionTask
     * @return all the ComTaskExecutions (which are not obsolete) for the given ConnectionTask
     */
    public List<ComTaskExecution> findComTaskExecutionsByConnectionTask(ConnectionTask<?, ?> connectionTask);

    /**
     * Finds all the ComTaskExecutions which are linked to the given ComSchedule (MasterSchedule)
     * (and are not obsolete)
     *
     * @param comSchedule the given comSchedule
     * @return all the ComTaskExecutions (which are not obsolete) for the given ConnectionTask
     */
    public List<ComTaskExecution> findComTaskExecutionsByComSchedule(ComSchedule comSchedule);
    public List<ComTaskExecution> findComTaskExecutionsByComScheduleWithinRange(ComSchedule comSchedule, long minId, long maxId);

    public List<ComTaskExecution> findComTasksByDefaultConnectionTask(Device device);

    public Fetcher<ComTaskExecution> getPlannedComTaskExecutionsFor(ComPort comPort);

    public List<ComTaskExecution> getPlannedComTaskExecutionsFor(InboundComPort comPort, Device device);

    public boolean areComTasksStillPending(Collection<Long> comTaskExecutionIds);

    public Optional<ComTaskExecutionSession> findLastSessionFor(ComTaskExecution comTaskExecution);

    public List<ComTaskExecutionSession> findByComTaskExecution(ComTaskExecution comTaskExecution);

    /**
     * Counts the number of communication errors that have occurred in the specified
     * {@link Interval} within the topology that starts from the speified Device.
     *
     * @param interval The Interval during which the communication errors have occurred
     * @return The number of communication errors
     */
    public int countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(CommunicationErrorType errorType, Device device, Interval interval);

    /**
     * Counts the last {@link ComSession} of all {@link ConnectionTask}s,
     * grouping them by their respective highest priority {@link CompletionCode}.
     *
     * @return The numbers, broken down by SuccessIndicator
     */
    public Map<CompletionCode, Long> getComTaskLastComSessionHighestPriorityCompletionCodeCount();

    /**
     * Counts all {@link ComTaskExecution}s grouping them by their
     * respective {@link DeviceType} and the {@link CompletionCode}
     * of the task's last {@link ComTaskExecutionSession session}.
     * The counters are returned in the order of CompletionCode.
     *
     * @return The counters
     */
    public Map<DeviceType, List<Long>> getComTasksDeviceTypeHeatMap();

}