package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.scheduling.NextExecutionSpecs;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.time.TemporalExpression;

import java.time.Instant;
import java.util.List;

/**
 * Models a {@link ConnectionTask} that is used in the context
 * of outbound communication, i.e. when the ComServer is setting
 * up the communication.
 * A ScheduledConnectionTask can be recurring in time,
 * i.e. it will connect to its related device on a frequent basis.
 * <p/>
 * It uses a {@link ConnectionStrategy} to calculate the next time
 * if will connect based on the timestamp it has last executed.
 * <p/>
 * The used strategy can decide to optimize the number of connections
 * in which case all communication with the linked devices will be executed
 * during the same communication session. A single connection is established
 * according to a frequency and an offset to midnight
 * (e.g. every day at midnight, every week at 2 a.m., ...).
 * <p/>
 * When the strategy is to execute communication as soon as possible,
 * then the frequency of establishing the connection is not used
 * but the frequency settings will be taken from the {@link ComTaskExecution}s
 * that are found on the related devices.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-11 (16:40)
 */
@ProviderType
public interface ScheduledConnectionTask extends OutboundConnectionTask<PartialScheduledConnectionTask> {

    public void setMaxNumberOfTries(int maxNumberOfTries);

    /**
     * Gets the time window during which communication with the device
     * is allowed or <code>null</code> if the {@link ConnectionType}
     * specifies that it does not support ComWindows.
     *
     * @return The ComWindow
     */
    public ComWindow getCommunicationWindow();

    public void setCommunicationWindow(ComWindow comWindow);

    /**
     * Gets the {@link ConnectionStrategy} that calculates
     * the next time a connection will be established.
     *
     * @return The ConnectionStrategy
     */
    public ConnectionStrategy getConnectionStrategy();

    public void setConnectionStrategy(ConnectionStrategy connectionStrategy);

    /**
     * Gets the specifications for the calculation of the next
     * execution timestamp of this ScheduledConnectionTask.
     *
     * @return The NextExecutionSpecs
     */
    public NextExecutionSpecs getNextExecutionSpecs();

    /**
     * Sets the specifications for the calculation of the next
     * execution timestamp from the {@link TemporalExpression}
     * for this ScheduledConnectionTask.
     *
     * @param temporalExpression The TemporalExpression
     */
    public void setNextExecutionSpecsFrom (TemporalExpression temporalExpression);

    /**
     * Gets the earliest possible timestamp of
     * the next execution of this ConnectionTask.
     *
     * @return The earliest possible next execution timestamp
     */
    public Instant getNextExecutionTimestamp();

    /**
     * Gets the earliest possible timestamp of
     * the next execution of this ConnectionTask
     * according to the {@link NextExecutionSpecs}.
     *
     * @return The earliest possible next execution timestamp
     */
    public Instant getPlannedNextExecutionTimestamp ();

    /**
     * Calculates and updates the next execution of this ConnectionTask
     * according to the recurring properties.
     *
     * @return The timestamp on which this ScheduledConnectionTask is rescheduled
     *         or <code>null</code> if no recurring properties have been set.
     * @see #getNextExecutionTimestamp()
     */
    public Instant updateNextExecutionTimestamp();

    void setDynamicMaxNumberOfTries(int maxNumberOfTries);

    /**
     * Updates the next execution of this ConnectionTask so that it will get picked up as soon as possible.
     * All ComTaskExecutions linked to this ConnectionTask in states Pending, Failed, Retrying or Never completed
     *
     * @return The timestamp on which this ScheduledConnectionTask is scheduled.
     */
    public Instant scheduleNow();

    /**
     * Updates the next execution of this ConnectionTask
     * so that it will get picked as soon as possible after the specified Date.
     * Note that the specified Date may be overruled by a {@link ComTaskExecution}
     * that is related to this ScheduledConnectionTask and that is scheduled
     * to be executed earlier.
     *
     * @param when The earliest possible Date on which this ConnectionTask should execute
     * @return The actual Date on which this ScheduledConnectionTask is scheduled.
     */
    public Instant schedule(Instant when);

    /**
     * Returns the {@link ConnectionInitiationTask} that will execute first
     * to initiate the connection to the device before actually connecting to it.
     *
     * @return The ConnectionInitiationTask that will initiate the connection to the device
     */
    public ConnectionInitiationTask getInitiatorTask();

    public void setInitiatorTask(ConnectionInitiationTask initiatorTask);

    /**
     * Returns whether this ConnectionTask is allowed to perform simultaneous connections to the same endPoint
     *
     * @return true if simultaneous connections are allowed, false otherwise
     */
    public boolean isSimultaneousConnectionsAllowed();

    public void setSimultaneousConnectionsAllowed (boolean allowSimultaneousConnections);

    /**
     * Gets this ComTaskExecution's status.
     *
     * @return The TaskStatus
     */
    public TaskStatus getTaskStatus();

    /**
     * @return a List containing all {@link ComTaskExecution}s that use this ScheduledConnectionTask.
     */
    public List<ComTaskExecution> getScheduledComTasks();

    /**
     * Schedules this ConnectionTask and the related {@link ComTaskExecution}s
     * to be executed on the specified Date.
     *
     * @param when The earliest possible Date on which this ConnectionTask should execute
     * @return The actual Date on which this OutboundConnectionTask is scheduled.
     * @see #schedule(Instant)
     */
    public Instant trigger (Instant when);

}