package com.energyict.mdc.device.data.tasks;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Models the execution of a ComTask in the context of a connection with a device
 * for which an appropriate {@link ConnectionTask} exists.
 * <p>
 * A ComTaskExecution is either scheduled or ad hoc.
 * The difference is that a scheduled ComTaskExecution has a scheduling frequency
 * and an ad hoc ComTaskExecution has not frequency.
 * <p>
 * A ComTaskExecution can be assigned to a specific {@link ConnectionTask}
 * but it can also be assigned to the default {@link ConnectionTask}.
 * In the first case, the ComTaskExecution is only executed when that
 * specific ConnectionTask executes. In the latter case,
 * the ComTaskExecution will be executed each time the default
 * ConnectionTask executes, regardless of which one is the default.
 * In other words, when the default changes, the execution of the
 * ComTaskExecution will also change.
 * <p>
 * The execution can be prioritized with a simple numerical mechanism.
 * The priority is a positive number and smaller numbers indicate higher priority.
 * In other words, zero is the highest priority.
 * This way the priority mechanism has a fixed and absolute highest priority
 * and it is not possible for an administrator to add a task to a running system
 * and give it the absolute highest priority by searching
 * the highest existing priority and adding one to it.
 * A practical example of task priorities is:
 * <ul>
 * <li>100: highest priority</li>
 * <li>200</li>
 * <li>300</li>
 * <li>400</li>
 * <li>500: lowest priority</li>
 * </ul>
 * The above still leaves some room for urgent high priority tasks to be scheduled
 * and executed before all others.
 * <p>
 * Each time a ComTaskExecution is executed,
 * a ComTaskExecutionSession is created
 * that captures all the details of the communication with the device.
 * That communication overview is very imported and should not be deleted easily.
 * Therefore, ComTaskExecutions are never deleted but made obsolete.
 * Obsolete ComTaskExecutions will not return from {@link DeviceService}
 * finder methods.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-09-21 (15:14)
 */
@ProviderType
public interface ComTaskExecution extends HasId, DataCollectionConfiguration {
    /**
     * The Default amount of seconds a ComTask should wait before retrying.
     */
    public static final int DEFAULT_COMTASK_FAILURE_RESCHEDULE_DELAY_SECONDS = 300;

    public static final int DEFAULT_PRIORITY = TaskPriorityConstants.DEFAULT_PRIORITY;

    /**
     * Tests if this ComTaskExecution is for a {@link ComSchedule}
     * that defines both the scheduling frequency and the
     * actual {@link ComTask}s that will be executed.
     *
     * @return A flag that indicates if this ComTaskExecution is for a ComSchedule
     */
    public boolean usesSharedSchedule();

    /**
     * Tests if this ComTaskExecution is scheduled manually,
     * i.e. it has a scheduling frequency causing it to be
     * executed frequently but not at a frequency defined by a
     * {@link com.energyict.mdc.scheduling.model.ComSchedule}
     * but by a one shot setting provided by the user.
     *
     * @return A flag that indicates if this ComTaskExecution is scheduled manually
     */
    public boolean isScheduledManually();

    /**
     * Tests if this ComTaskExecution is ad hoc,
     * i.e. it was meant to be executed once and only once.
     *
     * @return A flag that indicates if this ComTaskExecution is adhoc
     */
    public boolean isAdHoc();

    /**
     * Gets the {@link Device} for which this ComTaskExecution
     * is going to execute tasks.
     *
     * @return The Device for which tasks are executed
     */
    public Device getDevice();

    /**
     * Gets the {@link ComPort} that is currently
     * executing this ComTaskExecution or <code>null</code>
     * if this ComTaskExecution is not executing at this moment.
     *
     * @return The ComPort or <code>null</code>
     */
    public ComPort getExecutingComPort();

    /**
     * Tests if this ComTaskExecution is currently executing.
     * Convenience (and possibly faster) for <code>getExecutingComPort() != null</code>.
     *
     * @return <code>true</code> iff this ComTaskExecution is executing, i.e. if the executing ComPort is not null
     */
    public boolean isExecuting();

    /**
     * Gets this OutboundComTaskExecution's execution priority.
     * Note that the execution priority might be different from
     * the planned priority if the related Connection is using
     * the minimize strategy. In which case, a number of tasks
     * are combined and an execution priority is calculated
     * from that set of combined tasks.
     * Remember that this is a positive number
     * and smaller numbers indicate higher priority.
     * Zero is therefore the absolute highest priority.
     *
     * @return The execution priority
     */
    public int getExecutionPriority();

    /**
     * Gets this ComTaskExecution's status.
     *
     * @return The TaskStatus
     */
    public TaskStatus getStatus();

    /**
     * Tests if this ComTaskExecution is on hold.
     * Remember that the execution of a ComTaskExecution that is
     * on hold is temporarily disabled.
     *
     * @return <code>true</code> iff this ComTaskExecution is on hold.
     */
    public boolean isOnHold();

    /**
     * Gets the earliest possible timestamp on which this ComTaskExecution
     * will effectively be executed by the ComServer.
     *
     * @return The earliest possible next execution timestamp
     */
    public Instant getNextExecutionTimestamp();

    /**
     * Gets the maximum number of consecutive failures a ComTaskExecution can have before marking it as failed.
     *
     * @return the maximum number of consecutive failures
     */
    public int getMaxNumberOfTries();

    /**
     * Gets the counter that keeps track of the number of times
     * the execution of this OutboundComTaskExecution has been tried.
     *
     * @return The current try count
     */
    public int getCurrentTryCount();

    /**
     * Indication whether this OutboundComTaskExecution uses the default
     * {@link ScheduledConnectionTask} configured on the Device.
     * This will return false if a user sets the ConnectionTask to a
     * specific OutboundConnectionTask from the Device,
     * even when he chooses the (at that moment) default ConnectionTask.
     * <p>
     * This value will return true if this ComTask is always scheduled for
     * the default ConnectionTask which is at that moment configured on the Device.
     *
     * @return true if this ComTask is scheduled for the default ConnectionTask, false otherwise.
     */
    public boolean usesDefaultConnectionTask();

    /**
     * Gets the timestamp on which this ComTaskExecution started
     * or <code>null</code> if this ComTaskExecution is not executing at this moment.
     *
     * @return The timestamp on which this ComTaskExecution started or <code>null</code>
     */
    public Instant getExecutionStartedTimestamp();

    /**
     * Makes this ComTaskExecution obsolete, i.e. it will no longer execute
     * nor will it be returned by {@link DeviceService} finder methods.
     * <p>
     * Note: the call needs to run in a Transaction, no additional save() is required.
     */
    public void makeObsolete();

    /**
     * Tests if this ComTaskExecution is obsolete.
     *
     * @return A flag that indicates if this ComTaskExecution is obsolete
     */
    public boolean isObsolete();

    /**
     * Gets the date on which this ComTaskExecution was made obsolete.
     *
     * @return The date when this ComTaskExecution is made obsolete
     * or <code>null</code> when this ComTaskExecution is not obsolete at all.
     */
    public Instant getObsoleteDate();

    /**
     * Gets the {@link ConnectionTask} which will be used to perform this ComTaskExecution.
     *
     * @return the ConnectionTask
     */
    public Optional<ConnectionTask<?, ?>> getConnectionTask();

    public boolean usesSameConnectionTaskAs(ComTaskExecution anotherTask);

    public Optional<ComTaskExecutionSession> getLastSession();

    /**
     * Gets the timestamp of the last execution of this ComTaskExecution.
     *
     * @return The timestamp on which the last execution of this ComTaskExecution started
     * or <code>null</code> if this ComTaskExecution has not started executing yet
     */
    public Instant getLastExecutionStartTimestamp();

    /**
     * Gets the timestamp of the last time this ComTaskExecution completed successfully.
     *
     * @return The timestamp of last successful completion
     * or <code>null</code> if this ComTaskExecution
     * has never completed successfully
     */
    public Instant getLastSuccessfulCompletionTimestamp();

    /**
     * Gets the specifications for the calculation of the next
     * execution timestamp of this ComTaskExecution.
     * Note that ad-hoc ComTaskExecution do not have such a specification.
     *
     * @return The NextExecutionSpecs
     */
    public Optional<NextExecutionSpecs> getNextExecutionSpecs();

    /**
     * Gets the flag that indicates if this ComTaskExecution
     * will ignore the {@link NextExecutionSpecs} and therefore
     * always execute in an inbound context.
     *
     * @return The flag that indicates if the NextExecutionSpecs will be ignored in an inbound context
     */
    public boolean isIgnoreNextExecutionSpecsForInbound();

    /**
     * Gets the earliest possible timestamp of
     * the next execution of this ConnectionTask
     * according to the {@link NextExecutionSpecs}.
     *
     * @return The earliest possible next execution timestamp
     */
    public Instant getPlannedNextExecutionTimestamp();

    /**
     * Gets this ComTaskExecution's planned execution priority.
     * Remember that this is a positive number
     * and smaller numbers indicate higher priority.
     * Zero is therefore the absolute highest priority.
     *
     * @return The execution priority
     */
    public int getPlannedPriority();

    /**
     * Calculates and updates the next execution of this ComTaskExecution
     * according to the recurring properties.
     *
     * @see #getNextExecutionTimestamp()
     */
    public void updateNextExecutionTimestamp();

    /**
     * Puts this ComTaskExecution "on hold", i.e. temporarily
     * disables its execution. The reverse operation is to schedule
     * its execution again.
     *
     * @see #scheduleNow()
     * @see #schedule(Instant)
     * @see #updateNextExecutionTimestamp()
     */
    public void putOnHold();

    /**
     * Updates the next execution of this ComTaskExecution
     * so that it will get picked up as soon as possible.
     */
    public void scheduleNow();

    /**
     * RunNow will trigger the ComTaskExecution for an immediate  readout, regardless of the current strategy.
     * <ul>
     * In case of:
     * <li>ASAP, the nextExecutionTimeStamp of the comTask will be set to now. This will automatically update the
     * nextExecutionTimeStamp of his connectionTask</li>
     * <li>Minimize, the nextExecutionTimeStamp fo the comTask will be set to new. Additionally, the nextExecutionTimeStamp
     * of his connectionTask will be set to now. This means that all ComTasks that were already pending for this minimize
     * connection, will also be read out.</li>
     * </ul>
     */
    public void runNow();

    /**
     * Updates the next execution of this ComTaskExecution
     * so that it will get picked as soon as possible after the specified Date.
     */
    public void schedule(Instant when);

    public ComTaskExecutionUpdater<? extends ComTaskExecutionUpdater<?, ?>, ? extends ComTaskExecution> getUpdater();

    public List<ProtocolTask> getProtocolTasks();

    public List<ComTask> getComTasks();

    /**
     * Tests if this ComTaskExecution is configured to execute the ComSchedule.
     * Note that only {@link ScheduledComTaskExecution} can be configured to do this.
     *
     * @see #usesSharedSchedule()
     */
    public boolean executesComSchedule(ComSchedule comSchedule);

    /**
     * Tests if this ComTaskExecution is configured to execute the {@link ComTask}.
     * Note that this can be the case when:
     * <ul>
     * <li>The ComTaskExecution is configured to execute a {@link ComSchedule} that contains the ComTask</li>
     * <li>The ComTaskExecution is an adhoc execution for the ComTask</li>
     * <li>The ComTaskExecution is a manually scheduled execution for the ComTask</li>
     * </ul>
     *
     * @see #isAdHoc()
     * @see #isScheduledManually()
     */
    public boolean executesComTask(ComTask comTask);

    /**
     * Tests if the last execution of this ComTaskExecution failed.
     * Note that each time the ComTaskExecution executes,
     * this flag will be reset.
     *
     * @return <code>true</code> iff the last execution of this ComTaskExecution failed.
     */
    boolean isLastExecutionFailed();

}