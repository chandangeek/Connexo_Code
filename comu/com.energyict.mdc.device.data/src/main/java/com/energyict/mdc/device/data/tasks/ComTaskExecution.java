package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;
import java.util.Date;
import java.util.List;

/**
 * Models the execution of a ComTask in the context of a connection with a device
 * for which an appropriate {@link ConnectionTask} exists.
 * <p/>
 * A ComTaskExecution is either scheduled or ad hoc.
 * The difference is that a scheduled ComTaskExecution has a scheduling frequency
 * and an ad hoc ComTaskExecution has not frequency.
 * <p/>
 * A ComTaskExecution can be assigned to a specific {@link ConnectionTask}
 * but it can also be assigned to the default {@link ConnectionTask}.
 * In the first case, the ComTaskExecution is only executed when that
 * specific ConnectionTask executes. In the latter case,
 * the ComTaskExecution will be executed each time the default
 * ConnectionTask executes, regardless of which one is the default.
 * In other words, when the default changes, the execution of the
 * ComTaskExecution will also change.
 * <p/>
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
 * <p/>
 * Each time a ComTaskExecution is executed,
 * a ComTaskExecutionSession is created
 * that captures all the details of the communication with the device.
 * That communication overview is very imported and should not be deleted easily.
 * Therefore, ComTaskExecutions are never deleted but made obsolete.
 * Obsolete ComTaskExecutions will not return from {@link DeviceDataService}
 * finder methods.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-09-21 (15:14)
 */
public interface ComTaskExecution extends HasId, DataCollectionConfiguration {
    /**
     * The Default amount of seconds a ComTask should wait before retrying
     */
    public static final int DEFAULT_COMTASK_FAILURE_RESCHEDULE_DELAY_SECONDS = 300;

    /**
     * Tests if this ComTaskExecution is scheduled,
     * i.e. if it has a scheduling frequency causing it
     * to be executed frequently at that specified frequency.
     *
     * @return A flag that indicates if this ComTaskExecution is scheduled
     */
    public boolean isScheduled ();

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
    public Device getDevice ();

    /**
     * Gets the {@link ProtocolDialectConfigurationProperties}.
     */
    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties();

    /**
     * Gets the {@link ComPort} that is currently
     * executing this ComTaskExecution or <code>null</code>
     * if this ComTaskExecution is not executing at this moment.
     *
     * @return The ComPort or <code>null</code>
     */
    public ComPort getExecutingComPort ();

    /**
     * Tests if this ComTaskExecution is currently executing.
     * Convenience (and possibly faster) for <code>getExecutingComPort() != null</code>.
     *
     * @return <code>true</code> iff this ComTaskExecution is executing, i.e. if the executing ComPort is not null
     */
    public boolean isExecuting ();

    /**
     * Gets this OutboundComTaskExecution's execution priority.
     * Remember that this is a positive number
     * and smaller numbers indicate higher priority.
     * Zero is therefore the absolute highest priority.
     *
     * @return The execution priority
     */
    public int getPriority ();

    /**
     * Gets this ComTaskExecution's status.
     *
     * @return The TaskStatus
     */
    public TaskStatus getStatus ();

    /**
     * Tests if this ComTaskExecution is on hold.
     * Remember that the execution of a ComTaskExecution that is
     * on hold is temporarily disabled.
     *
     * @return <code>true</code> iff this ComTaskExecution is on hold.
     */
    public boolean isOnHold ();

    /**
     * Gets the earliest possible timestamp on which this ComTaskExecution
     * will effectively be executed by the ComServer.
     *
     * @return The earliest possible next execution timestamp
     */
    public Date getNextExecutionTimestamp ();

    /**
     * Gets the maximum number of consecutive failures a ComTaskExecution can have before marking it as failed.

     * @return the maximum number of consecutive failures
     */
    public int getMaxNumberOfTries ();

    /**
     * Gets the counter that keeps track of the number of times
     * the execution of this OutboundComTaskExecution has been tried.
     *
     * @return The current try count
     */
    public int getCurrentTryCount ();

    /**
     * Indication whether this OutboundComTaskExecution uses the default
     * {@link ScheduledConnectionTask} configured on the Device.
     * This will return false if a user sets the ConnectionTask to a
     * specific OutboundConnectionTask from the Device,
     * even when he chooses the (at that moment) default ConnectionTask.
     * <p/>
     * This value will return true if this ComTask is always scheduled for
     * the default ConnectionTask which is at that moment configured on the Device.
     *
     * @return true if this ComTask is scheduled for the default ConnectionTask, false otherwise.
     */
    public boolean useDefaultConnectionTask ();
    /**
     * Gets the timestamp on which this ComTaskExecution started
     * or <code>null</code> if this ComTaskExecution is not executing at this moment.
     *
     * @return The timestamp on which this ComTaskExecution started or <code>null</code>
     */
    public Date getExecutionStartedTimestamp ();

    /**
     * Makes this ComTaskExecution obsolete, i.e. it will no longer execute
     * nor will it be returned by {@link DeviceDataService} finder methods.
     *
     * Note: the call needs to run in a Transaction, no additional save() is required.
     */
    public void makeObsolete ();

    /**
     * Tests if this ComTaskExecution is obsolete.
     *
     * @return A flag that indicates if this ComTaskExecution is obsolete
     */
    public boolean isObsolete ();

    /**
     * Gets the date on which this ComTaskExecution was made obsolete.
     *
     * @return The date when this ComTaskExecution is made obsolete
     *         or <code>null</code> when this ComTaskExecution is not obsolete at all.
     */
    public Date getObsoleteDate ();

    /**
     * Gets the {@link ConnectionTask} which will be used to perform this ComTaskExecution
     *
     * @return the used ConnectionTask
     */
    public ConnectionTask<?,?> getConnectionTask ();

    public boolean usesSameConnectionTaskAs(ComTaskExecution anotherTask);

    /**
     * Gets the timestamp of the last execution of this ComTaskExecution.
     *
     * @return The timestamp on which the last execution of this ComTaskExecution started
     *         or <code>null</code> if this ComTaskExecution has not started executing yet
     */
    public Date getLastExecutionStartTimestamp ();

    /**
     * Gets the timestamp of the last time this ComTaskExecution completed successfully.
     *
     * @return The timestamp of last successful completion
     *         or <code>null</code> if this ComTaskExecution
     *         has never completed successfully
     */
    public Date getLastSuccessfulCompletionTimestamp ();

    /**
     * Gets the specifications for the calculation of the next
     * execution timestamp of this ComTaskExecution.
     *
     * @return The NextExecutionSpecs
     */
    public NextExecutionSpecs getNextExecutionSpecs();

    /**
     * Gets the flag that indicates if this ComTaskExecution
     * will ignore the {@link NextExecutionSpecs} and therefore
     * always execute in an inbound context.
     *
     * @return The flag that indicates if the NextExecutionSpecs will be ignored in an inbound context
     */
    public boolean isIgnoreNextExecutionSpecsForInbound ();

    /**
     * Gets the earliest possible timestamp of
     * the next execution of this ConnectionTask
     * according to the {@link NextExecutionSpecs}.
     *
     * @return The earliest possible next execution timestamp
     */
    public Date getPlannedNextExecutionTimestamp();

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

     * @see #getNextExecutionTimestamp()
     */
    public void updateNextExecutionTimestamp();

    /**
     * Puts this ComTaskExecution "on hold", i.e. temporarily
     * disables its execution. The reverse operation is to schedule
     * its execution again.
     *
     * @see #scheduleNow()
     * @see #schedule(java.util.Date)
     * @see #updateNextExecutionTimestamp()
     */
    public void putOnHold();

    /**
     * Updates the next execution of this ComTaskExecution
     * so that it will get picked up as soon as possible.
     */
    public void scheduleNow();

    /**
     * Updates the next execution of this ComTaskExecution
     * so that it will get picked as soon as possible after the specified Date.
     */
    public void schedule(Date when);

    ComTaskExecutionUpdater<? extends ComTaskExecutionUpdater<?,?>, ? extends ComTaskExecution> getUpdater();

    public List<ProtocolTask> getProtocolTasks();

    public List<ComTask> getComTasks();
}