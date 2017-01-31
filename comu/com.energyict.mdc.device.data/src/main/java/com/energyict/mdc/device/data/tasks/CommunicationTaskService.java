/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Provides services that relate to {@link ComTaskExecution}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (13:30)
 */
@ProviderType
public interface CommunicationTaskService {
    String FILTER_ITEMIZER_QUEUE_DESTINATION = "ItemizeCommFilterQD";
    String FILTER_ITEMIZER_QUEUE_SUBSCRIBER = "ItemizeCommFilterQS";
    String FILTER_ITEMIZER_QUEUE_DISPLAYNAME = "Itemize communications from filter";
    String COMMUNICATION_RESCHEDULER_QUEUE_DESTINATION = "ReschCommQD";
    String COMMUNICATION_RESCHEDULER_QUEUE_SUBSCRIBER = "ReschCommQS";
    String COMMUNICATION_RESCHEDULER_QUEUE_DISPLAYNAME = "Handle communication task rescheduling";

    /**
     * Gets all {@link ComTaskExecution}s of the specified {@link Device}
     * that are using the default {@link ConnectionTask}.
     *
     * @param device The Device
     * @return The List of ComTaskExecution
     */
    List<ComTaskExecution> findComTaskExecutionsWithDefaultConnectionTask(Device device);

    TimeDuration releaseTimedOutComTasks(ComServer comServer);

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
    void releaseInterruptedComTasks(ComServer comServer);

    /**
     * Finds the ComTaskExecution with the given ID
     *
     * @param id the unique ID of the ComTaskExecution
     * @return the requested ComTaskExecution
     */
    Optional<ComTaskExecution> findComTaskExecution(long id);

    public Optional<ComTaskExecution> findAndLockComTaskExecutionByIdAndVersion(long id, long version);

    /**
     * Finds all {@link ConnectionTask}s that match the specified filter.
     *
     * @param filter    The ComTaskExecutionFilterSpecification
     * @param pageStart The first ComTaskExecution
     * @param pageSize  The maximum number of ComTaskExecutions
     * @return The List of ComTaskExecution
     */
    List<ComTaskExecution> findComTaskExecutionsByFilter(ComTaskExecutionFilterSpecification filter, int pageStart, int pageSize);

    /**
     * Finds all ComTaskExecutions for the given Device which aren't made obsolete yet
     *
     * @param device the device
     * @return the currently active ComTaskExecutions for this device
     */
    List<ComTaskExecution> findComTaskExecutionsByDevice(Device device);

    /**
     * Finds all the ComTaskExecutions for the given Device, including the ones that have been made obsolete
     *
     * @param device the device
     * @return all ComTaskExecutions which have ever been created for this Device
     */
    List<ComTaskExecution> findAllComTaskExecutionsIncludingObsoleteForDevice(Device device);

    /**
     * Attempts to lock the ComTaskExecution that is about to be executed
     * by the specified ComPort and returns the locked ComTaskExecution
     * when the lock succeeds and <code>null</code> when the lock fails.
     * Note that this MUST run in an existing transactional context.
     *
     * @param comTaskExecution The ComTaskExecution
     * @param comPort          The ComPort that is about to execute the ComTaskExecution
     * @return <code>true</code> iff the lock succeeds
     */
    ComTaskExecution attemptLockComTaskExecution(ComTaskExecution comTaskExecution, ComPort comPort);

    /**
     * Removes the business lock on the specified ComTaskExecution,
     * making it available for other ComPorts to execute the ComTaskExecution.
     *
     * @param comTaskExecution The ComTaskExecution
     */
    void unlockComTaskExecution(ComTaskExecution comTaskExecution);

    /**
     * Finds all the ComTaskExecutions which are linked to the given ConnectionTask
     * (and are not obsolete)
     *
     * @param connectionTask the given ConnectionTask
     * @return all the ComTaskExecutions (which are not obsolete) for the given ConnectionTask
     */
    Finder<ComTaskExecution> findComTaskExecutionsByConnectionTask(ConnectionTask<?, ?> connectionTask);

    /**
     * Finds all the ComTaskExecutions which are linked to the given ComSchedule (MasterSchedule)
     * (and are not obsolete)
     *
     * @param comSchedule the given comSchedule
     * @return all the ComTaskExecutions (which are not obsolete) for the given ConnectionTask
     */
    List<ComTaskExecution> findComTaskExecutionsByComSchedule(ComSchedule comSchedule);

    List<ComTaskExecution> findComTaskExecutionsByComScheduleWithinRange(ComSchedule comSchedule, long minId, long maxId);

    List<ComTaskExecution> findComTasksByDefaultConnectionTask(Device device);

    Fetcher<ComTaskExecution> getPlannedComTaskExecutionsFor(OutboundComPort comPort);

    List<ComTaskExecution> getPlannedComTaskExecutionsFor(InboundComPort comPort, Device device);

    boolean isComTaskStillPending(long comTaskExecutionId);

    boolean areComTasksStillPending(Collection<Long> comTaskExecutionIds);

    Optional<ComTaskExecutionSession> findLastSessionFor(ComTaskExecution comTaskExecution);

    Finder<ComTaskExecutionSession> findSessionsByComTaskExecution(ComTaskExecution comTaskExecution);

    Optional<ComTaskExecutionSession> findSession(long sessionId);

    Finder<ComTaskExecutionSession> findSessionsByComTaskExecutionAndComTask(ComTaskExecution comTaskExecution, ComTask comTask);

    /**
     * Counts the {@link Device}s from the specified List that have had
     * communication errors of the specified type
     * that have occurred in the specified {@link Interval}.
     *
     * @param devices The List of Devices that are used for counting
     * @param interval The Interval during which the communication errors have occurred
     * @param successIndicatorCondition The condition that specifies the type of communication error
     * @return The number of communication errors
     */
    int countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(List<Device> devices, Range<Instant> interval, Condition successIndicatorCondition);

    void executionCompletedFor(ComTaskExecution comTaskExecution);

    void executionFailedFor(ComTaskExecution comTaskExecution);

    void executionStartedFor(ComTaskExecution comTaskExecution, ComPort comPort);

    void executionRescheduled(ComTaskExecution comTaskExecution, Instant rescheduleDate);

    List<ComTaskExecution> findComTaskExecutionsWhichAreExecuting(ComPort comPort);
}