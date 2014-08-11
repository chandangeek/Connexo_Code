package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.ServerComTaskStatus;
import com.energyict.mdc.device.data.exceptions.CannotUpdateObsoleteComTaskExecutionException;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsAlreadyObsoleteException;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.CreateEventType;
import com.energyict.mdc.device.data.impl.DeleteEventType;
import com.energyict.mdc.device.data.impl.PersistentIdObject;
import com.energyict.mdc.device.data.impl.UpdateEventType;
import com.energyict.mdc.device.data.impl.constraintvalidators.ComTasksMustBeEnabledByDeviceConfiguration;
import com.energyict.mdc.device.data.impl.constraintvalidators.ConnectionTaskIsRequiredWhenNotUsingDefault;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Clock;
import com.google.common.collect.ImmutableMap;
import org.hibernate.validator.constraints.Range;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Provides code and structural reuse opportunities for
 * components that intend to provide an implementation
 * for the {@link ComTaskExecution} interface.
 *
 * Copyrights EnergyICT
 * Date: 11/04/14
 * Time: 15:09
 */
@ConnectionTaskIsRequiredWhenNotUsingDefault(groups = {Save.Create.class, Save.Update.class})
@ComTasksMustBeEnabledByDeviceConfiguration(groups = {Save.Create.class})
public abstract class ComTaskExecutionImpl extends PersistentIdObject<ComTaskExecution> implements ServerComTaskExecution {
    protected static final String SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR = "0";
    protected static final String AD_HOC_COM_TASK_EXECUTION_DISCRIMINATOR = "1";
    protected static final String MANUALLY_SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR = "2";

    public static final Map<String, Class<? extends ComTaskExecution>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ComTaskExecution>>of(
                    SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR, ScheduledComTaskExecutionImpl.class,
                    MANUALLY_SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR, ManuallyScheduledComTaskExecutionImpl.class,
                    AD_HOC_COM_TASK_EXECUTION_DISCRIMINATOR, AdHocComTaskExecutionImpl.class);

    private final Clock clock;
    private final DeviceDataService deviceDataService;
    private final SchedulingService schedulingService;

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_IS_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();

    private long connectionTaskId;  // Required for performance of ComTaskExecution query to avoid loading the ConnectionTask to only get the id
    private Reference<ConnectionTask<?, ?>> connectionTask = ValueReference.absent();

    private Reference<ComPort> comPort = ValueReference.absent();

    private Date nextExecutionTimestamp;
    private Date lastExecutionTimestamp;
    private Date executionStart;
    private Date lastSuccessfulCompletionTimestamp;
    private Date plannedNextExecutionTimestamp;
    private Date obsoleteDate;
    private Date modificationDate;

    /**
     * ExecutionPriority can be overruled by the Minimize ConnectionTask
     */
    @Range(min = TaskPriorityConstants.HIGHEST_PRIORITY, max = TaskPriorityConstants.LOWEST_PRIORITY, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.PRIORITY_NOT_IN_RANGE + "}")
    private int executionPriority;
    @Range(min = TaskPriorityConstants.HIGHEST_PRIORITY, max = TaskPriorityConstants.LOWEST_PRIORITY, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.PRIORITY_NOT_IN_RANGE + "}")
    private int plannedPriority;
    private int currentRetryCount;
    private boolean lastExecutionFailed;
    private boolean useDefaultConnectionTask;
    private boolean ignoreNextExecutionSpecsForInbound;

    @Inject
    public ComTaskExecutionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, DeviceDataService deviceDataService, SchedulingService schedulingService) {
        super(ComTaskExecution.class, dataModel, eventService, thesaurus);
        this.clock = clock;
        this.deviceDataService = deviceDataService;
        this.schedulingService = schedulingService;
    }

    protected SchedulingService getSchedulingService() {
        return schedulingService;
    }

    protected void initializeDevice(Device device) {
        this.device.set(device);
    }

    protected void initializeFrom(Device device, ComTaskEnablement comTaskEnablement) {
        this.initializeDevice(device);
        this.ignoreNextExecutionSpecsForInbound = comTaskEnablement.isIgnoreNextExecutionSpecsForInbound();
        this.executionPriority = comTaskEnablement.getPriority();
        this.plannedPriority = comTaskEnablement.getPriority();
        this.setUseDefaultConnectionTask(comTaskEnablement.usesDefaultConnectionTask());
    }

    @Override
    public Device getDevice() {
        return this.device.get();   // we do an explicit get because Device is required and should not be null
    }

    @Override
    public ComPort getExecutingComPort() {
        return comPort.orNull();
    }

    @Override
    public boolean isExecuting() {
        return this.comPort.isPresent() || (this.connectionTask.isPresent() && (this.connectionTask.get().getExecutingComServer() != null) && this.getNextExecutionTimestamp().before(this.clock.now()));
    }

    @Override
    public int getExecutionPriority() {
        return this.executionPriority;
    }

    void setExecutingPriority(int executingPriority) {
        this.executionPriority = executingPriority;
    }

    @Override
    public TaskStatus getStatus() {
        return ServerComTaskStatus.getApplicableStatusFor(this, this.now());
    }

    @Override
    public boolean isOnHold() {
        return this.nextExecutionTimestamp == null;
    }

    @Override
    public Date getNextExecutionTimestamp() {
        return this.nextExecutionTimestamp;
    }

    @Override
    public int getCurrentTryCount() {
        return this.getCurrentRetryCount() + 1;
    }

    private int getCurrentRetryCount() {
        return this.currentRetryCount;
    }

    @Override
    public boolean useDefaultConnectionTask() {
        return this.useDefaultConnectionTask;
    }

    void setUseDefaultConnectionTask(boolean useDefaultConnectionTask) {
        this.useDefaultConnectionTask = useDefaultConnectionTask;
        if (this.useDefaultConnectionTask) {
            this.setConnectionTask(this.deviceDataService.findDefaultConnectionTaskForDevice(getDevice()));
        }
    }

    void setDefaultConnectionTask(ConnectionTask<?, ?> defaultConnectionTask) {
        this.useDefaultConnectionTask = true;
        setConnectionTask(defaultConnectionTask);
    }

    @Override
    public Date getExecutionStartedTimestamp() {
        return this.executionStart;
    }

    @Override
    public void makeObsolete() {
        reloadMyselfForObsoleting();
        validateMakeObsolete();
        this.obsoleteDate = this.now();
        this.post();
    }

    /**
     * We need to check if this task is currently running or someone else made it obsolete.
     * We are already in a Transaction so we don't wrap it again.
     */
    private void reloadMyselfForObsoleting() {
        ComTaskExecution updatedVersionOfMyself = this.deviceDataService.findComTaskExecution(this.getId());
        if (updatedVersionOfMyself != null) {
            this.comPort.set(updatedVersionOfMyself.getExecutingComPort());
            this.obsoleteDate = updatedVersionOfMyself.getObsoleteDate();
            this.setConnectionTask(updatedVersionOfMyself.getConnectionTask());
        }
    }

    private void validateMakeObsolete() {
        if (this.isObsolete()) {
            throw new ComTaskExecutionIsAlreadyObsoleteException(this.getThesaurus(), this);
        } else if (this.comPort.isPresent()) {
            throw new ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException(this.getThesaurus(), this, this.getExecutingComPort().getComServer());
        }

        if (this.useDefaultConnectionTask) {
            ConnectionTask<?, ?> defaultConnectionTaskForDevice = this.deviceDataService.findDefaultConnectionTaskForDevice(getDevice());
            if (defaultConnectionTaskForDevice != null && defaultConnectionTaskForDevice.getExecutingComServer() != null) {
                throw new ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException(this.getThesaurus(), this, this.deviceDataService.findDefaultConnectionTaskForDevice(getDevice()).getExecutingComServer());
            }
        } else if (this.connectionTask.isPresent() && this.connectionTask.get().getExecutingComServer() != null) {
            throw new ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException(this.getThesaurus(), this, this.connectionTask.get().getExecutingComServer());
        }
    }

    @Override
    public boolean isObsolete() {
        return this.obsoleteDate != null;
    }

    @Override
    public Date getObsoleteDate() {
        return this.obsoleteDate;
    }

    @Override
    public ConnectionTask<?, ?> getConnectionTask() {
        return this.connectionTask.orNull();
    }

    void setConnectionTask(ConnectionTask<?, ?> connectionTask) {
        this.connectionTask.set(connectionTask);
        if (connectionTask != null) {
            this.connectionTaskId = connectionTask.getId();
        }
        else {
            this.connectionTaskId = 0;
        }
    }

    @Override
    public boolean usesSameConnectionTaskAs (ComTaskExecution anotherTask) {
        if (anotherTask instanceof ComTaskExecutionImpl) {
            ComTaskExecutionImpl comTaskExecution = (ComTaskExecutionImpl) anotherTask;
            return this.connectionTaskId == comTaskExecution.connectionTaskId;
        }
        else {
            return this.connectionTaskId == anotherTask.getConnectionTask().getId();
        }
    }

    @Override
    public Date getLastExecutionStartTimestamp() {
        return this.lastExecutionTimestamp;
    }

    @Override
    public Date getLastSuccessfulCompletionTimestamp() {
        return this.lastSuccessfulCompletionTimestamp;
    }

    @Override
    public boolean isIgnoreNextExecutionSpecsForInbound() {
        return this.ignoreNextExecutionSpecsForInbound;
    }

    void setIgnoreNextExecutionSpecsForInbound(boolean ignoreNextExecutionSpecsForInbound) {
        this.ignoreNextExecutionSpecsForInbound = ignoreNextExecutionSpecsForInbound;
    }

    @Override
    public Date getPlannedNextExecutionTimestamp() {
        return this.plannedNextExecutionTimestamp;
    }

    @Override
    public int getPlannedPriority() {
        return this.plannedPriority;
    }

    protected void setPlannedPriority (int plannedPriority) {
        this.plannedPriority = plannedPriority;
    }

    @Override
    public void updateNextExecutionTimestamp() {
        recalculateNextAndPlannedExecutionTimestamp();
        post();
    }

    void recalculateNextAndPlannedExecutionTimestamp() {
        Date plannedNextExecutionTimestamp = this.calculateNextExecutionTimestamp(this.clock.now());
        this.schedule(plannedNextExecutionTimestamp, plannedNextExecutionTimestamp);
    }

    protected Date calculateNextExecutionTimestamp(Date now) {
        return this.calculateNextExecutionTimestampFromBaseline(now);
    }

    private Date calculateNextExecutionTimestampFromBaseline(Date baseLine) {
        NextExecutionSpecs nextExecutionSpecs = this.getNextExecutionSpecs().get();
        Calendar calendar = Calendar.getInstance(this.clock.getTimeZone());
        calendar.setTime(baseLine);
        return nextExecutionSpecs.getNextTimestamp(calendar);
    }

    /**
     * Provide my two dates and I'll update this object according to it's settings.
     *
     * @param nextExecutionTimestamp        the time you think this object should schedule
     * @param plannedNextExecutionTimestamp the time this object is planned to schedule
     */
    private void schedule(Date nextExecutionTimestamp, Date plannedNextExecutionTimestamp) {
        resetCurrentRetryCount();
        doReschedule(nextExecutionTimestamp, plannedNextExecutionTimestamp);
    }

    private void resetCurrentRetryCount() {
        this.currentRetryCount = 0;
    }

    /**
     * Every xxSchedule attempt should go through this object.
     * I'll won't touch the plannedNextExecutionTimeStamp, but I might update
     * the nextExecutionTimeStamp according to the specs of this object
     * or the specs of my ConnectionTask.
     *
     * @param nextExecutionTimestamp        the time you think this object should schedule
     * @param plannedNextExecutionTimestamp the time this object is planned to schedule
     */
    private void doReschedule(Date nextExecutionTimestamp, Date plannedNextExecutionTimestamp) {
        this.setExecutingComPort(null);
        this.setExecutionStartedTimestamp(null);
        if (nextExecutionTimestamp != null) {// nextExecutionTimestamp is null when putting on hold
            nextExecutionTimestamp = defineNextExecutionTimeStamp(nextExecutionTimestamp);
        }
        this.setPlannedNextExecutionTimestamp(plannedNextExecutionTimestamp);
        this.nextExecutionTimestamp = nextExecutionTimestamp;

        /* ConnectionTask can be null when the default is used but
         * no default has been set or created yet. */
        ConnectionTask<?, ?> connectionTask = this.getConnectionTask();
        if (connectionTask != null) {
            connectionTask.scheduledComTaskRescheduled(this);
        }
    }

    private void setExecutingComPort(ComPort comPort) {
        this.comPort.set(comPort);
    }

    private void setExecutionStartedTimestamp(Date executionStartedTimestamp) {
        this.executionStart = executionStartedTimestamp;
    }

    private void setPlannedNextExecutionTimestamp(Date plannedNextExecutionTimestamp) {
        this.plannedNextExecutionTimestamp = plannedNextExecutionTimestamp;
    }

    void setNextExecutionTimestamp(Date nextExecutionTimestamp) {
        this.doReschedule(nextExecutionTimestamp);
    }

    /**
     * Just for clarity's sake.<br/>
     * If the current ConnectionTask is either INBOUND or OUTBOUND with an ASAP strategy,
     * then we adjust the given nextExecutionTimestamp according to the defined ComWindow.
     * If the current ConnectionTask is OUTBOUND with a Minimize strategy,
     * then we set the nextExecutionTimestamp to the next nextExecutionTimestamp of the
     * connectionTask, starting for the given nextExecutionTimestamp. This means future dates will be updated to
     * future nextExecutionTimeStamps of the current ConnectionTask.
     *
     * @param nextExecutionTimestamp the nextExecutionTimestamp to adjust if necessary
     * @return the adjusted nextExecutionTimestamp
     */
    private Date defineNextExecutionTimeStamp(Date nextExecutionTimestamp) {
        if (!this.isScheduledConnectionTask(getConnectionTask()) || ConnectionStrategy.AS_SOON_AS_POSSIBLE.equals(getScheduledConnectionTask().getConnectionStrategy())) {
            return this.applyComWindowIfOutboundAndAny(nextExecutionTimestamp);
        } else { // in case of outbound MINIMIZE
            Date nextActualConnectionTime = getScheduledConnectionTask().getNextExecutionTimestamp();
            // nextActualConnectionTime can be off regular schedule due to retries. If a retry time would fit our needs, we'll hitch along.
            if (nextActualConnectionTime != null && !nextExecutionTimestamp.after(nextActualConnectionTime)) {
                return nextActualConnectionTime;
            } else {
                Calendar calendar = Calendar.getInstance(this.clock.getTimeZone());
                calendar.setTime(nextExecutionTimestamp);
                calendar.add(Calendar.MILLISECOND, -1); // hack getNextTimeStamp to be inclusive
                return getScheduledConnectionTask().getNextExecutionSpecs().getNextTimestamp(calendar);
            }
        }
    }

    private Date applyComWindowIfOutboundAndAny(Date preliminaryNextExecutionTimestamp) {
        if (isScheduledConnectionTask(getConnectionTask())) {
            return getScheduledConnectionTask().applyComWindowIfAny(preliminaryNextExecutionTimestamp);
        } else {
            return preliminaryNextExecutionTimestamp;
        }
    }

    private boolean isScheduledConnectionTask(ConnectionTask<?, ?> connectionTask) {
        return connectionTask instanceof ScheduledConnectionTask;
    }

    private ScheduledConnectionTaskImpl getScheduledConnectionTask() {
        return (ScheduledConnectionTaskImpl) connectionTask.get();
    }

    @Override
    public void putOnHold() {
        this.schedule(null);
        this.save();
    }

    @Override
    public void scheduleNow() {
        this.schedule(this.clock.now());
    }

    @Override
    public void schedule(Date when) {
        this.schedule(when, this.getPlannedNextExecutionTimestamp());
        post();
    }

    @Override
    public void setLockedComPort(ComPort comPort) {
        setExecutingComPort(comPort);
        post();
    }

    @Override
    public void executionCompleted() {
        this.markSuccessfullyCompleted();
        this.doReschedule(calculateNextExecutionTimestamp(this.clock.now()));
        post();
    }

    /**
     * Marks this ComTaskExecution as successfully completed.
     */
    private void markSuccessfullyCompleted() {
        this.lastSuccessfulCompletionTimestamp = this.clock.now();
        this.resetCurrentRetryCount();
    }

    private void doReschedule(Date nextExecutionTimestamp) {
        this.doReschedule(nextExecutionTimestamp, nextExecutionTimestamp);
    }

    @Override
    public void executionFailed() {
        this.currentRetryCount++;    // increment the current number of retries
        if (this.currentRetryCount < getMaxNumberOfTries()) {
            this.doExecutionAttemptFailed();
        } else {
            this.doExecutionFailed();
        }
        post();
    }

    protected void doExecutionAttemptFailed() {
        this.lastExecutionFailed = true;
        this.doReschedule(calculateNextExecutionTimestampAfterFailure());
    }

    private Date calculateNextExecutionTimestampAfterFailure() {
        Date failureDate = this.clock.now();
        Calendar calendar = Calendar.getInstance(this.clock.getTimeZone());
        calendar.setTime(failureDate);
        TimeDuration baseRetryDelay = this.getRescheduleRetryDelay();
        TimeDuration failureRetryDelay = new TimeDuration(baseRetryDelay.getCount() * getCurrentRetryCount(), baseRetryDelay.getTimeUnitCode());
        failureRetryDelay.addTo(calendar);
        final Date calculatedNextExecutionTimeStamp = this.calculateNextExecutionTimestamp(failureDate);
        if (calculatedNextExecutionTimeStamp != null) {
            return this.minimum(calendar.getTime(), calculatedNextExecutionTimeStamp);
        } else {
            return calendar.getTime();
        }
    }

    private Date minimum(Date date1, Date date2) {
        return date1.before(date2) ? date1 : date2;
    }

    /**
     * The rescheduleDelay is determined as follow:
     * <ul>
     * <li>First we check if the configured {@link ComTask} has a proper {@link #getConnectionTask().getRescheduleDelay()}</li>
     * <li>Then we return the default {@link ComTaskExecution#DEFAULT_COMTASK_FAILURE_RESCHEDULE_DELAY_SECONDS} in seconds</li>
     * </ul>
     *
     * @return the configured rescheduleRetryDelay
     */
    private TimeDuration getRescheduleRetryDelay() {
        TimeDuration comTaskDefinedRescheduleDelay = this.comTaskRescheduleDelay();
        if (comTaskDefinedRescheduleDelay == null || comTaskDefinedRescheduleDelay.getSeconds() <= 0) {
            return this.defaultRescheduleDelay();
        } else {
            return comTaskDefinedRescheduleDelay;
        }
    }

    private TimeDuration defaultRescheduleDelay() {
        return new TimeDuration(ComTaskExecution.DEFAULT_COMTASK_FAILURE_RESCHEDULE_DELAY_SECONDS, TimeDuration.SECONDS);
    }

    private TimeDuration comTaskRescheduleDelay() {
        TimeDuration comTaskDefinedRescheduleDelay;
        if (this.isScheduledConnectionTask(getConnectionTask())) {
            ScheduledConnectionTask outboundConnectionTask = getScheduledConnectionTask();
            comTaskDefinedRescheduleDelay = outboundConnectionTask.getRescheduleDelay();
        } else {
            comTaskDefinedRescheduleDelay = null;
        }
        return comTaskDefinedRescheduleDelay;
    }

    protected void doExecutionFailed() {
        this.lastExecutionFailed = true;
        this.resetCurrentRetryCount();
        if (isAdHoc()) {
            this.doReschedule(null, null);
        } else {
            this.doReschedule(calculateNextExecutionTimestamp(this.clock.now()));
        }
    }

    @Override
    public boolean lastExecutionFailed() {
        return this.lastExecutionFailed;
    }

    @Override
    public void executionStarted(ComPort comPort) {
        this.doExecutionStarted(comPort);
        this.post();    // update myself!
    }

    protected void doExecutionStarted(ComPort comPort) {
        Date now = this.clock.now();
        this.setExecutionStartedTimestamp(now);
        this.lastExecutionTimestamp = this.clock.now();
        this.lastExecutionFailed = false;
        this.setNextExecutionTimestamp(this.calculateNextExecutionTimestamp(this.getExecutionStartedTimestamp()));
        this.setExecutingComPort(comPort);
    }

    /**
     * This will update the object with the given ConnectionTask,
     * <i>OR</i> the connectionTask will be cleared and configured to use the default ConnectionTask
     *
     * @param connectionTask the connectionTask to set or <code>null</code> to clear the connectionTask
     */
    protected void assignConnectionTask(ConnectionTask<?, ?> connectionTask) {
        this.setConnectionTask(connectionTask);
        if (!this.connectionTask.isPresent()) {
            this.setUseDefaultConnectionTask(true);
        }
    }

    @Override
    public void connectionTaskRemoved() {
        this.setConnectionTask(null);
        this.setUseDefaultConnectionTask(true);
        this.post();
    }

    @Override
    public void updateConnectionTask(ConnectionTask<?, ?> connectionTask) {
        this.assignConnectionTask(connectionTask);
        this.setUseDefaultConnectionTask(false);
        this.post();
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.COMTASKEXECUTION;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.COMTASKEXECUTION;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.COMTASKEXECUTION;
    }

    @Override
    protected void doDelete() {
        this.getDataModel().remove(this);
    }

    @Override
    protected void validateDelete() {
        // nothing to validate
    }

    /**
     * Return true if this comTaskExecution and the parameter denote the same job, that is:
     * - ScheduledComTaskExecs with the same schedule
     * - AdHocComTasks with the same comTask
     */
    public abstract boolean performsIdenticalTask(ComTaskExecutionImpl comTaskExecution);

    /**
     * We don't do our own persistence, our device will take care of that
     */
    public void prepareForSaving() {
        this.modificationDate = this.now();
        validateNotObsolete();
    }

    protected Date now() {
        return this.clock.now();
    }

    protected void validateNotObsolete() {
        if (this.obsoleteDate != null) {
            throw new CannotUpdateObsoleteComTaskExecutionException(this.getThesaurus(), this);
        }
    }

    public abstract static class AbstractComTaskExecutionBuilder<B extends ComTaskExecutionBuilder<B, C>, C extends ComTaskExecution, CI extends ComTaskExecutionImpl> implements ComTaskExecutionBuilder<B, C> {

        private final CI comTaskExecution;
        private final B self;

        protected AbstractComTaskExecutionBuilder(CI instance, Class<B> clazz) {
            this.comTaskExecution = instance;
            this.self = clazz.cast(this);
        }

        protected CI getComTaskExecution () {
            return this.comTaskExecution;
        }

        protected B self () {
            return this.self;
        }

        @Override
        public B useDefaultConnectionTask(boolean useDefaultConnectionTask) {
            this.comTaskExecution.setUseDefaultConnectionTask(useDefaultConnectionTask);
            return self;
        }

        @Override
        public B connectionTask(ConnectionTask<?, ?> connectionTask) {
            this.comTaskExecution.setConnectionTask(connectionTask);
            this.comTaskExecution.setUseDefaultConnectionTask(false);
            this.comTaskExecution.recalculateNextAndPlannedExecutionTimestamp();
            return self;
        }

        @Override
        public B priority(int priority) {
            this.comTaskExecution.setPlannedPriority(priority);
            return self;
        }

        @Override
        public B ignoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound) {
            this.comTaskExecution.setIgnoreNextExecutionSpecsForInbound(ignoreNextExecutionSpecsForInbound);
            return self;
        }

        @Override
        public C add() {
            this.comTaskExecution.prepareForSaving();
            return (C) this.comTaskExecution;
        }

    }

    public abstract static class AbstractComTaskExecutionUpdater<U extends ComTaskExecutionUpdater<U, C>, C extends ComTaskExecution, CI extends ComTaskExecutionImpl> implements ComTaskExecutionUpdater<U, C> {

        private final CI comTaskExecution;
        private final U self;

        protected AbstractComTaskExecutionUpdater(CI comTaskExecution, Class<U> clazz) {
            this.comTaskExecution = comTaskExecution;
            this.self = clazz.cast(this);
        }

        protected CI getComTaskExecution () {
            return this.comTaskExecution;
        }

        protected U self() {
            return this.self;
        }

        @Override
        public U useDefaultConnectionTask(boolean useDefaultConnectionTask) {
            this.comTaskExecution.setUseDefaultConnectionTask(useDefaultConnectionTask);
            return self;
        }

        @Override
        public U connectionTask(ConnectionTask<?, ?> connectionTask) {
            this.comTaskExecution.setConnectionTask(connectionTask);
            this.comTaskExecution.setUseDefaultConnectionTask(false);
            this.comTaskExecution.recalculateNextAndPlannedExecutionTimestamp();
            return self;
        }

        @Override
        public U priority(int executionPriority) {
            this.comTaskExecution.setPlannedPriority(executionPriority);
            return self;
        }

        @Override
        public U ignoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound) {
            this.comTaskExecution.setIgnoreNextExecutionSpecsForInbound(ignoreNextExecutionSpecsForInbound);
            return self;
        }

        @Override
        public U forceNextExecutionTimeStampAndPriority(Date nextExecutionTimestamp, int priority) {
            this.comTaskExecution.setNextExecutionTimestamp(nextExecutionTimestamp);
            this.comTaskExecution.setExecutingPriority(priority);
            return self;
        }

        @Override
        public U useDefaultConnectionTask(ConnectionTask<?, ?> defaultConnectionTask) {
            this.comTaskExecution.setDefaultConnectionTask(defaultConnectionTask);
            return self;
        }

        @Override
        public C update() {
            this.comTaskExecution.prepareForSaving();
            this.comTaskExecution.save();
            return (C) this.comTaskExecution;
        }

    }

}