package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.CannotUpdateObsoleteComTaskExecutionException;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsAlreadyObsoleteException;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.CreateEventType;
import com.energyict.mdc.device.data.impl.DeleteEventType;
import com.energyict.mdc.device.data.impl.EventType;
import com.energyict.mdc.device.data.impl.PersistentIdObject;
import com.energyict.mdc.device.data.impl.ServerComTaskExecution;
import com.energyict.mdc.device.data.impl.UpdateEventType;
import com.energyict.mdc.device.data.impl.constraintvalidators.ComTasksMustBeEnabledByDeviceConfiguration;
import com.energyict.mdc.device.data.impl.constraintvalidators.ConnectionTaskIsRequiredWhenNotUsingDefault;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComPort;
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
import com.elster.jupiter.time.TimeDuration;
import com.google.common.collect.ImmutableMap;
import org.hibernate.validator.constraints.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Calendar;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Provides code and structural reuse opportunities for
 * components that intend to provide an implementation
 * for the {@link ComTaskExecution} interface.
 * <p>
 * Copyrights EnergyICT
 * Date: 11/04/14
 * Time: 15:09
 */
@ConnectionTaskIsRequiredWhenNotUsingDefault(groups = {Save.Create.class, Save.Update.class})
@ComTasksMustBeEnabledByDeviceConfiguration(groups = {Save.Create.class})
public abstract class ComTaskExecutionImpl extends PersistentIdObject<ComTaskExecution> implements ServerComTaskExecution {
    protected static final String SHARED_SCHEDULE_COM_TASK_EXECUTION_DISCRIMINATOR = "0";
    protected static final String MANUALLY_SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR = "1";
    protected static final String FIRMWARE_COM_TASK_EXECUTION_DISCRIMINATOR = "2";

    public static final Map<String, Class<? extends ComTaskExecution>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ComTaskExecution>>of(
                    SHARED_SCHEDULE_COM_TASK_EXECUTION_DISCRIMINATOR, ScheduledComTaskExecutionImpl.class,
                    MANUALLY_SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR, ManuallyScheduledComTaskExecutionImpl.class,
                    FIRMWARE_COM_TASK_EXECUTION_DISCRIMINATOR, FirmwareComTaskExecutionImpl.class);

    private final Clock clock;
    private final CommunicationTaskService communicationTaskService;
    private final SchedulingService schedulingService;

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_IS_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();

    private long connectionTaskId;  // Required for performance of ComTaskExecution query to avoid loading the ConnectionTask to only get the id
    private Reference<ConnectionTask<?, ?>> connectionTask = ValueReference.absent();

    private Reference<ComPort> comPort = ValueReference.absent();

    private Instant nextExecutionTimestamp;
    private Instant lastExecutionTimestamp;
    private Instant executionStart;
    private Instant lastSuccessfulCompletionTimestamp;
    private Instant plannedNextExecutionTimestamp;
    private Instant obsoleteDate;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    /**
     * ExecutionPriority can be overruled by the Minimize ConnectionTask.
     */
    @Range(min = TaskPriorityConstants.HIGHEST_PRIORITY, max = TaskPriorityConstants.LOWEST_PRIORITY, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.PRIORITY_NOT_IN_RANGE + "}")
    private int executionPriority;
    @Range(min = TaskPriorityConstants.HIGHEST_PRIORITY, max = TaskPriorityConstants.LOWEST_PRIORITY, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.PRIORITY_NOT_IN_RANGE + "}")
    private int plannedPriority;
    private int currentRetryCount;
    private boolean lastExecutionFailed;
    private Reference<ComTaskExecutionSession> lastSession = ValueReference.absent();
    @SuppressWarnings("unused")
    private CompletionCode lastSessionHighestPriorityCompletionCode;
    @SuppressWarnings("unused")
    private ComTaskExecutionSession.SuccessIndicator lastSessionSuccessIndicator;
    private boolean useDefaultConnectionTask;
    private boolean ignoreNextExecutionSpecsForInbound;

    @Inject
    public ComTaskExecutionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, CommunicationTaskService communicationTaskService, SchedulingService schedulingService) {
        super(ComTaskExecution.class, dataModel, eventService, thesaurus);
        this.clock = clock;
        this.communicationTaskService = communicationTaskService;
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
        if (comTaskEnablement.usesDefaultConnectionTask() || !comTaskEnablement.hasPartialConnectionTask()) {
            this.setUseDefaultConnectionTask(true);
        } else if (comTaskEnablement.hasPartialConnectionTask()) {
            this.setMatchingConnectionTaskOrUseDefaultIfNotFound(device, comTaskEnablement);
        }
    }

    private void setMatchingConnectionTaskOrUseDefaultIfNotFound(Device device, ComTaskEnablement comTaskEnablement) {
        boolean notFound = true;
        PartialConnectionTask partialConnectionTask = comTaskEnablement.getPartialConnectionTask().get();
        for (ConnectionTask<?, ?> connectionTask : device.getConnectionTasks()) {
            if (connectionTask.getPartialConnectionTask().getId() == partialConnectionTask.getId()) {
                this.setConnectionTask(connectionTask);
                notFound = false;
            }
        }
        if (notFound) {
            this.setUseDefaultConnectionTask(true);
        }
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
        return this.comPort.isPresent()
            || (   this.connectionTask.isPresent()
                && (this.connectionTask.get().getExecutingComServer() != null)
                && (this.getNextExecutionTimestamp() != null && this.getNextExecutionTimestamp().isBefore(this.clock.instant())));
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
    public Instant getNextExecutionTimestamp() {
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
    public boolean usesDefaultConnectionTask() {
        return this.useDefaultConnectionTask;
    }

    void setUseDefaultConnectionTask(boolean useDefaultConnectionTask) {
        this.useDefaultConnectionTask = useDefaultConnectionTask;
        if (useDefaultConnectionTask) {
            this.setConnectionTask(null);
        }
    }

    void setDefaultConnectionTask(ConnectionTask<?, ?> defaultConnectionTask) {
        this.useDefaultConnectionTask = true;
        setConnectionTask(defaultConnectionTask);
    }

    @Override
    public Instant getExecutionStartedTimestamp() {
        return this.executionStart;
    }

    @Override
    public void makeObsolete() {
        reloadMyselfForObsoleting();
        validateMakeObsolete();
        this.obsoleteDate = this.clock.instant();
        this.update(ComTaskExecutionFields.OBSOLETEDATE.fieldName());
    }

    /**
     * We need to check if this task is currently running or someone else made it obsolete.
     * We are already in a Transaction so we don't wrap it again.
     */
    private void reloadMyselfForObsoleting() {
        Optional<ComTaskExecution> updatedVersionOfMyself = this.communicationTaskService.findComTaskExecution(this.getId());
        updatedVersionOfMyself.ifPresent(cte -> {
            this.comPort.set(cte.getExecutingComPort());
            this.obsoleteDate = cte.getObsoleteDate();
            this.setConnectionTask(cte.getConnectionTask().orElse(null));
        });
    }

    private void validateMakeObsolete() {
        if (this.isObsolete()) {
            throw new ComTaskExecutionIsAlreadyObsoleteException(this.getThesaurus(), this);
        } else if (this.comPort.isPresent()) {
            throw new ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException(this.getThesaurus(), this, this.getExecutingComPort().getComServer());
        }
        if (this.useDefaultConnectionTask) {
            this.postEvent(EventType.COMTASKEXECUTION_VALIDATE_OBSOLETE);
        } else if (this.connectionTask.isPresent() && this.connectionTask.get().getExecutingComServer() != null) {
            throw new ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException(this.getThesaurus(), this, this.connectionTask.get().getExecutingComServer());
        }
    }

    @Override
    public boolean isObsolete() {
        return this.obsoleteDate != null;
    }

    @Override
    public Instant getObsoleteDate() {
        return this.obsoleteDate;
    }

    @Override
    public Optional<ConnectionTask<?, ?>> getConnectionTask() {
        return this.connectionTask.getOptional();
    }

    void setConnectionTask(ConnectionTask<?, ?> connectionTask) {
        this.connectionTask.set(connectionTask);
        if (connectionTask != null) {
            this.connectionTaskId = connectionTask.getId();
        } else {
            this.connectionTaskId = 0;
        }
    }

    @Override
    public boolean usesSameConnectionTaskAs(ComTaskExecution anotherTask) {
        if (anotherTask instanceof ComTaskExecutionImpl) {
            ComTaskExecutionImpl comTaskExecution = (ComTaskExecutionImpl) anotherTask;
            return this.connectionTaskId == comTaskExecution.connectionTaskId;
        } else {
            if (anotherTask.getConnectionTask().isPresent()) {
                return this.connectionTaskId == anotherTask.getConnectionTask().get().getId();
            }
            else {
                return false;
            }
        }
    }

    @Override
    public Instant getLastExecutionStartTimestamp() {
        return this.lastExecutionTimestamp;
    }

    @Override
    public void sessionCreated(ComTaskExecutionSession session) {
        if (this.lastSession.isPresent()) {
            if (session.endsAfter(this.lastSession.get())) {
                this.setLastSessionAndUpdate(session);
            }
        } else {
            this.setLastSessionAndUpdate(session);
        }
    }

    private void setLastSessionAndUpdate(ComTaskExecutionSession session) {
        this.setLastSession(session);
/*      Bug in the DataModel that does not support foreign key columns in the update method
        this.getDataModel()
                .update(this,
                        ComTaskExecutionFields.LAST_SESSION.fieldName(),
                        ComTaskExecutionFields.LAST_SESSION_HIGHEST_PRIORITY_COMPLETION_CODE.fieldName(),
                        ComTaskExecutionFields.LAST_SESSION_SUCCESSINDICATOR.fieldName());
*/
        this.update();
    }

    private void setLastSession(ComTaskExecutionSession session) {
        this.lastSession.set(session);
        this.lastSessionHighestPriorityCompletionCode = session.getHighestPriorityCompletionCode();
        this.lastSessionSuccessIndicator = session.getSuccessIndicator();
    }

    @Override
    public Optional<ComTaskExecutionSession> getLastSession() {
        Optional<ComTaskExecutionSession> optional = this.lastSession.getOptional();
        if (optional.isPresent()) {
            return java.util.Optional.of(optional.get());
        }
        else {
            return java.util.Optional.empty();
        }
    }

    @Override
    public Instant getLastSuccessfulCompletionTimestamp() {
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
    public Instant getPlannedNextExecutionTimestamp() {
        return this.plannedNextExecutionTimestamp;
    }

    @Override
    public int getPlannedPriority() {
        return this.plannedPriority;
    }

    protected void setPlannedPriority(int plannedPriority) {
        this.plannedPriority = plannedPriority;
    }

    @Override
    public void updateNextExecutionTimestamp() {
        recalculateNextAndPlannedExecutionTimestamp();
        this.update();
    }

    void recalculateNextAndPlannedExecutionTimestamp() {
        Instant plannedNextExecutionTimestamp = this.calculateNextExecutionTimestamp(clock.instant());
        this.schedule(plannedNextExecutionTimestamp, plannedNextExecutionTimestamp);
    }

    protected Instant calculateNextExecutionTimestamp(Instant now) {
        return this.calculateNextExecutionTimestampFromBaseline(now);
    }

    private Instant calculateNextExecutionTimestampFromBaseline(Instant baseLine) {
        NextExecutionSpecs nextExecutionSpecs = this.getNextExecutionSpecs().get();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(this.clock.getZone()));
        calendar.setTimeInMillis(baseLine.toEpochMilli());
        return nextExecutionSpecs.getNextTimestamp(calendar).toInstant();
    }

    /**
     * Provide my two dates and I'll update this object according to it's settings.
     *
     * @param nextExecutionTimestamp        the time you think this object should schedule
     * @param plannedNextExecutionTimestamp the time this object is planned to schedule
     */
    private void schedule(Instant nextExecutionTimestamp, Instant plannedNextExecutionTimestamp) {
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
    private void doReschedule(Instant nextExecutionTimestamp, Instant plannedNextExecutionTimestamp) {
        this.setExecutingComPort(null);
        this.setExecutionStartedTimestamp(null);
        if (nextExecutionTimestamp != null) {// nextExecutionTimestamp is null when putting on hold
            nextExecutionTimestamp = defineNextExecutionTimeStamp(nextExecutionTimestamp);
        }
        this.setPlannedNextExecutionTimestamp(plannedNextExecutionTimestamp);
        this.nextExecutionTimestamp = nextExecutionTimestamp;

        /* ConnectionTask can be null when the default is used but
         * no default has been set or created yet. */
        this.getConnectionTask().ifPresent(ct -> ct.scheduledComTaskRescheduled(this));
    }

    private void setExecutingComPort(ComPort comPort) {
        this.comPort.set(comPort);
    }

    private void setExecutionStartedTimestamp(Instant executionStartedTimestamp) {
        this.executionStart = executionStartedTimestamp;
    }

    private void setPlannedNextExecutionTimestamp(Instant plannedNextExecutionTimestamp) {
        this.plannedNextExecutionTimestamp = plannedNextExecutionTimestamp;
    }

    void setNextExecutionTimestamp(Instant nextExecutionTimestamp) {
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
    private Instant defineNextExecutionTimeStamp(Instant nextExecutionTimestamp) {
        if (!this.connectionTaskIsScheduled() || ConnectionStrategy.AS_SOON_AS_POSSIBLE.equals(getScheduledConnectionTask().getConnectionStrategy())) {
            return this.applyComWindowIfOutboundAndAny(nextExecutionTimestamp);
        } else { // in case of outbound MINIMIZE
            Instant nextActualConnectionTime = getScheduledConnectionTask().getNextExecutionTimestamp();
            // nextActualConnectionTime can be off regular schedule due to retries. If a retry time would fit our needs, we'll hitch along.
            if (nextActualConnectionTime != null && !nextExecutionTimestamp.isAfter(nextActualConnectionTime)) {
                return nextActualConnectionTime;
            } else {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(this.clock.getZone()));
                calendar.setTimeInMillis(nextExecutionTimestamp.toEpochMilli());
                calendar.add(Calendar.MILLISECOND, -1); // hack getNextTimeStamp to be inclusive
                return getScheduledConnectionTask().getNextExecutionSpecs().getNextTimestamp(calendar).toInstant();
            }
        }
    }

    private Instant applyComWindowIfOutboundAndAny(Instant preliminaryNextExecutionTimestamp) {
        if (this.connectionTaskIsScheduled()) {
            return getScheduledConnectionTask().applyComWindowIfAny(preliminaryNextExecutionTimestamp);
        } else {
            return preliminaryNextExecutionTimestamp;
        }
    }

    private boolean connectionTaskIsScheduled() {
        return this.getConnectionTask().isPresent()
            && this.getConnectionTask().get() instanceof ScheduledConnectionTask;
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
        this.schedule(clock.instant());
    }

    @Override
    public void runNow() {
        this.resetCurrentRetryCount();
        this.setExecutingComPort(null);
        this.setExecutionStartedTimestamp(null);
        Instant currentDate = clock.instant();
        this.setPlannedNextExecutionTimestamp(currentDate);
        this.nextExecutionTimestamp = currentDate;

        if (this.connectionTaskIsScheduled()) {
            ((ScheduledConnectionTaskImpl) this.getConnectionTask().get()).scheduleConnectionNow();
        }
        this.update();
    }

    @Override
    public void schedule(Instant when) {
        this.schedule(when, this.getPlannedNextExecutionTimestamp());
        this.update();
    }

    @Override
    public void setLockedComPort(ComPort comPort) {
        setExecutingComPort(comPort);
        this.update(ComTaskExecutionFields.COMPORT.fieldName());
    }

    @Override
    public void executionCompleted() {
        this.markSuccessfullyCompleted();
        this.doReschedule(calculateNextExecutionTimestamp(clock.instant()));
        this.update();
    }

    /**
     * Marks this ComTaskExecution as successfully completed.
     */
    private void markSuccessfullyCompleted() {
        this.lastSuccessfulCompletionTimestamp = clock.instant();
        this.resetCurrentRetryCount();
    }

    private void doReschedule(Instant nextExecutionTimestamp) {
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
        this.update();
    }

    protected void doExecutionAttemptFailed() {
        this.lastExecutionFailed = true;
        this.doReschedule(calculateNextExecutionTimestampAfterFailure());
    }

    private Instant calculateNextExecutionTimestampAfterFailure() {
        Instant failureDate = clock.instant();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(clock.getZone()));
        calendar.setTimeInMillis(failureDate.toEpochMilli());
        TimeDuration baseRetryDelay = this.getRescheduleRetryDelay();
        TimeDuration failureRetryDelay = new TimeDuration(baseRetryDelay.getCount() * getCurrentRetryCount(), baseRetryDelay.getTimeUnitCode());
        failureRetryDelay.addTo(calendar);
        Instant calculatedNextExecutionTimeStamp = this.calculateNextExecutionTimestamp(failureDate);
        if (calculatedNextExecutionTimeStamp != null) {
            return this.minimum(calendar.getTime().toInstant(), calculatedNextExecutionTimeStamp);
        } else {
            return calendar.getTime().toInstant();
        }
    }

    private Instant minimum(Instant date1, Instant date2) {
        return date1.isBefore(date2) ? date1 : date2;
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
        return new TimeDuration(ComTaskExecution.DEFAULT_COMTASK_FAILURE_RESCHEDULE_DELAY_SECONDS, TimeDuration.TimeUnit.SECONDS);
    }

    private TimeDuration comTaskRescheduleDelay() {
        TimeDuration comTaskDefinedRescheduleDelay;
        if (this.connectionTaskIsScheduled()) {
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
            this.doReschedule(calculateNextExecutionTimestamp(clock.instant()));
        }
    }

    @Override
    public boolean isLastExecutionFailed() {
        return this.lastExecutionFailed;
    }

    @Override
    public void executionStarted(ComPort comPort) {
        this.doExecutionStarted(comPort);
        this.update();
    }

    private void doExecutionStarted(ComPort comPort) {
        Instant now = this.clock.instant();
        this.setExecutionStartedTimestamp(now);
        this.lastExecutionTimestamp = now;
        this.lastExecutionFailed = false;
        this.setNextExecutionTimestamp(this.calculateNextExecutionTimestamp(this.getExecutionStartedTimestamp()));
        this.setExecutingComPort(comPort);
    }

    @Override
    public void connectionTaskRemoved() {
        this.setConnectionTask(null);
        this.setUseDefaultConnectionTask(true);
        this.update();
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
     * We don't do our own persistence, our device will take care of that.
     */
    public void prepareForSaving() {
        validateNotObsolete();
    }

    protected Instant now() {
        return clock.instant();
    }

    protected void validateNotObsolete() {
        if (this.obsoleteDate != null) {
            throw new CannotUpdateObsoleteComTaskExecutionException(this.getThesaurus(), this);
        }
    }

    public abstract static class AbstractComTaskExecutionBuilder<C extends ComTaskExecution, CI extends ComTaskExecutionImpl> implements ComTaskExecutionBuilder<C> {

        private final CI comTaskExecution;

        protected AbstractComTaskExecutionBuilder(CI instance) {
            this.comTaskExecution = instance;
        }

        protected CI getComTaskExecution() {
            return this.comTaskExecution;
        }

        @Override
        public ComTaskExecutionBuilder<C> useDefaultConnectionTask(boolean useDefaultConnectionTask) {
            this.comTaskExecution.setUseDefaultConnectionTask(useDefaultConnectionTask);
            return this;
        }

        @Override
        public ComTaskExecutionBuilder<C> connectionTask(ConnectionTask<?, ?> connectionTask) {
            this.comTaskExecution.setConnectionTask(connectionTask);
            this.comTaskExecution.setUseDefaultConnectionTask(false);
            this.comTaskExecution.recalculateNextAndPlannedExecutionTimestamp();
            return this;
        }

        @Override
        public ComTaskExecutionBuilder<C> priority(int priority) {
            this.comTaskExecution.setPlannedPriority(priority);
            return this;
        }

        @Override
        public ComTaskExecutionBuilder<C> ignoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound) {
            this.comTaskExecution.setIgnoreNextExecutionSpecsForInbound(ignoreNextExecutionSpecsForInbound);
            return this;
        }

        @Override
        public C add() {
            this.comTaskExecution.prepareForSaving();
            return (C) this.comTaskExecution;
        }

    }

    public abstract static class AbstractComTaskExecutionUpdater<U extends ComTaskExecutionUpdater<U, C>, C extends ComTaskExecution, CI extends ComTaskExecutionImpl> implements ComTaskExecutionUpdater<U, C> {

        private final CI comTaskExecution;
        private boolean connectionTaskSchedulingMayHaveChanged = false;
        private final U self;

        protected AbstractComTaskExecutionUpdater(CI comTaskExecution, Class<U> clazz) {
            this.comTaskExecution = comTaskExecution;
            this.self = clazz.cast(this);
        }

        protected CI getComTaskExecution() {
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
        public U forceNextExecutionTimeStampAndPriority(Instant nextExecutionTimestamp, int priority) {
            this.comTaskExecution.setNextExecutionTimestamp(nextExecutionTimestamp);
            this.comTaskExecution.setExecutingPriority(priority);
            return self;
        }

        @Override
        public U useDefaultConnectionTask(ConnectionTask<?, ?> defaultConnectionTask) {
            this.comTaskExecution.setDefaultConnectionTask(defaultConnectionTask);
            this.connectionTaskSchedulingMayHaveChanged = true;
            return self;
        }

        @Override
        public C update() {
            this.comTaskExecution.prepareForSaving();
            this.comTaskExecution.save();
            if (this.connectionTaskSchedulingMayHaveChanged) {
                this.comTaskExecution.getConnectionTask().ifPresent(ct -> ct.scheduledComTaskRescheduled(this.comTaskExecution));
            }
            return (C) this.comTaskExecution;
        }

    }

}