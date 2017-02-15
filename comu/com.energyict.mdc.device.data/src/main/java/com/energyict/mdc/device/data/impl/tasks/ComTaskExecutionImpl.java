/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.domain.util.Range;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.CannotUpdateObsoleteComTaskExecutionException;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsAlreadyObsoleteException;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException;
import com.energyict.mdc.device.data.impl.CreateEventType;
import com.energyict.mdc.device.data.impl.DeleteEventType;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.EventType;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.PersistentIdObject;
import com.energyict.mdc.device.data.impl.ServerComTaskExecution;
import com.energyict.mdc.device.data.impl.TaskStatusTranslationKeys;
import com.energyict.mdc.device.data.impl.UpdateEventType;
import com.energyict.mdc.device.data.impl.constraintvalidators.ComTasksMustBeEnabledByDeviceConfiguration;
import com.energyict.mdc.device.data.impl.constraintvalidators.ManuallyScheduledNextExecSpecRequired;
import com.energyict.mdc.device.data.impl.constraintvalidators.SaveScheduled;
import com.energyict.mdc.device.data.impl.constraintvalidators.SharedScheduleComScheduleRequired;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionTrigger;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tasks.TopologyTask;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

@ComTasksMustBeEnabledByDeviceConfiguration(groups = {Save.Create.class})
@ManuallyScheduledNextExecSpecRequired(groups = {SaveScheduled.class})
@SharedScheduleComScheduleRequired(groups = {Save.Create.class, Save.Update.class})
@ComTaskMustBeFirmwareManagement(groups = {Save.Create.class, Save.Update.class})
public class ComTaskExecutionImpl extends PersistentIdObject<ComTaskExecution> implements ServerComTaskExecution {


    public enum ComTaskExecType {
        SHARED_SCHEDULE_COM_TASK_EXECUTION_DISCRIMINATOR,
        MANUALLY_SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR,
        FIRMWARE_COM_TASK_EXECUTION_DISCRIMINATOR
    }

    private final Clock clock;

    private final CommunicationTaskService communicationTaskService;
    private final SchedulingService schedulingService;
    protected Reference<ComSchedule> comSchedule = ValueReference.absent();
    protected Reference<NextExecutionSpecs> nextExecutionSpecs = ValueReference.absent();

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_IS_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.COMTASK_IS_REQUIRED + "}")
    protected Reference<ComTask> comTask = ValueReference.absent();

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
    private boolean calledByConnectionTask;

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
    private List<ComTaskExecutionTrigger> comTaskExecutionTriggers = new ArrayList<>();
    private boolean onHold;

    private Behavior behavior;
    private ComTaskExecType comTaskExecType;

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

    protected void initializeFrom(Device device, ComTaskEnablement comTaskEnablement) {
        initializeDevice(device);
        this.comTask.set(comTaskEnablement.getComTask());
        this.ignoreNextExecutionSpecsForInbound = comTaskEnablement.isIgnoreNextExecutionSpecsForInbound();
        this.executionPriority = comTaskEnablement.getPriority();
        this.plannedPriority = comTaskEnablement.getPriority();
        this.setUseDefaultConnectionTask(comTaskEnablement.usesDefaultConnectionTask());
        if (!comTaskEnablement.usesDefaultConnectionTask()) {
            setConnectionTaskIfExists(device, comTaskEnablement);
        }
    }

    public Behavior getBehavior() {
        if (this.behavior == null) {
            switch (comTaskExecType) {
                case FIRMWARE_COM_TASK_EXECUTION_DISCRIMINATOR:
                    this.behavior = new FirmwareBehavior();
                    break;
                case MANUALLY_SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR:
                    this.behavior = new ManualBehavior();
                    break;
                case SHARED_SCHEDULE_COM_TASK_EXECUTION_DISCRIMINATOR:
                    this.behavior = new ScheduledBehavior();
                    break;
                default:
                    this.behavior = new ManualBehavior();

            }
        }
        return behavior;
    }

    void initializeDevice(Device device) {
        this.device.set(device);
    }

    @Override
    public boolean usesSharedSchedule() {
        return this.getBehavior().usesSharedSchedule();
    }

    @Override
    public boolean isScheduledManually() {
        return this.getBehavior().isScheduledManually();
    }

    @Override
    public boolean isAdHoc() {
        return this.getBehavior().isAdHoc();
    }

    @Override
    public boolean isFirmware() {
        return this.getBehavior().isFirmware();
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
                || (this.connectionTask.isPresent()
                && (this.connectionTask.get().getExecutingComServer() != null)
                && ((this.getNextExecutionTimestamp() != null
                && this.getNextExecutionTimestamp().isBefore(this.clock.instant()))
                || (this.getNextExecutionTimestamp() == null
                && this.isIgnoreNextExecutionSpecsForInbound()
                && this.connectionTask.get() instanceof InboundConnectionTask))
        );
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
    public String getStatusDisplayName() {
        return TaskStatusTranslationKeys.translationFor(getStatus(), getThesaurus());
    }

    @Override
    public Instant getNextExecutionTimestamp() {
        return this.nextExecutionTimestamp;
    }

    @Override
    public int getMaxNumberOfTries() {
        return this.getBehavior().getMaxNumberOfTries();
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

    @Override
    public void save() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update() {
        Save.UPDATE.save(getDataModel(), this, Save.Create.class, Save.Update.class);
        this.notifyUpdated();
    }

    @Override
    protected void update(String... fieldNames) {
        this.getDataModel().update(this, fieldNames);
        this.notifyUpdated();
    }

    @Override
    protected void validateAndCreate() {
        this.recalculateNextAndPlannedExecutionTimestamp();
        if (!this.isAdHoc() && this.isScheduledManually()) {
            Save.CREATE.save(this.getDataModel(), this, SaveScheduled.class);
        } else {
            Save.CREATE.save(this.getDataModel(), this);
        }
    }

    @Override
    protected void validateAndUpdate() {
        if (!this.isAdHoc() && this.isScheduledManually()) {
            Save.UPDATE.save(this.getDataModel(), this, SaveScheduled.class);
        } else {
            Save.UPDATE.save(this.getDataModel(), this);
        }
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
            throw new ComTaskExecutionIsAlreadyObsoleteException(this, this.getThesaurus(), MessageSeeds.COM_TASK_EXECUTION_IS_ALREADY_OBSOLETE);
        } else if (this.comPort.isPresent()) {
            throw new ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException(this, this.getExecutingComPort()
                    .getComServer(), this.getThesaurus(), MessageSeeds.COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE);
        }
        if (this.useDefaultConnectionTask) {
            this.postEvent(EventType.COMTASKEXECUTION_VALIDATE_OBSOLETE);
        } else if (this.connectionTask.isPresent() && this.connectionTask.get().getExecutingComServer() != null) {
            throw new ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException(this, this.connectionTask.get()
                    .getExecutingComServer(), this.getThesaurus(), MessageSeeds.COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE);
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
            } else {
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
        this.getDataModel()
                .update(this,
                        ComTaskExecutionFields.LAST_SESSION.fieldName(),
                        ComTaskExecutionFields.LAST_SESSION_HIGHEST_PRIORITY_COMPLETION_CODE.fieldName(),
                        ComTaskExecutionFields.LAST_SESSION_SUCCESSINDICATOR.fieldName());
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
        } else {
            return java.util.Optional.empty();
        }
    }

    @Override
    public Instant getLastSuccessfulCompletionTimestamp() {
        return this.lastSuccessfulCompletionTimestamp;
    }

    @Override
    public Optional<NextExecutionSpecs> getNextExecutionSpecs() {
        return this.getBehavior().getNextExecutionSpecs();
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

    void setPlannedPriority(int plannedPriority) {
        this.plannedPriority = plannedPriority;
    }

    @Override
    public void updateNextExecutionTimestamp() {
        recalculateNextAndPlannedExecutionTimestamp();
        this.updateForScheduling(true);
    }

    void recalculateNextAndPlannedExecutionTimestamp() {
        Instant plannedNextExecutionTimestamp = this.calculateNextExecutionTimestamp(clock.instant());
        this.schedule(plannedNextExecutionTimestamp, plannedNextExecutionTimestamp);
    }

    protected Instant calculateNextExecutionTimestamp(Instant now) {
        if (this.isAdHoc()) {
            if (this.getLastExecutionStartTimestamp() != null
                    && this.getNextExecutionTimestamp() != null
                    && this.getLastExecutionStartTimestamp().isAfter(this.getNextExecutionTimestamp())) {
                return null;
            } else {
                return this.getNextExecutionTimestamp();
            }
        } else {
            return this.calculateNextExecutionTimestampFromBaseline(now);
        }
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
     * @param nextExecutionTimestamp the time you think this object should schedule
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
     * @param nextExecutionTimestamp the time you think this object should schedule
     * @param plannedNextExecutionTimestamp the time this object is planned to schedule
     */
    private void doReschedule(Instant nextExecutionTimestamp, Instant plannedNextExecutionTimestamp) {
        this.setExecutingComPort(null);
        this.setExecutionStartedTimestamp(null);

        nextExecutionTimestamp = applyCommunicationTriggersTo(Optional.ofNullable(nextExecutionTimestamp));
        if (nextExecutionTimestamp != null) {// nextExecutionTimestamp is null when putting on hold
            nextExecutionTimestamp = defineNextExecutionTimeStamp(nextExecutionTimestamp);
        }
        this.setPlannedNextExecutionTimestamp(plannedNextExecutionTimestamp);
        this.nextExecutionTimestamp = nextExecutionTimestamp;
        /* ConnectionTask can be null when the default is used but
         * no default has been set or created yet. */
    }

    /**
     * Apply the communication triggers (see {@link #getComTaskExecutionTriggers()}) to the given nextExecutionTimeStamp<br/>
     * This method will return the earliest date of either:
     * <ul>
     * <li>the given nextExecutionTimeStamp</li>
     * <li>the earliest communication trigger timeStamp after {@link #getLastExecutionStartTimestamp()}</li>
     * </ul>
     *
     * @param nextExecutionTimeStamp the next execution timestamp for which the ComTaskExecutionTriggers should be applied
     * @return the nextExecutionTimeStamp (which is either the given nextExecutionTimeStamp or the earliest communication trigger timeStamp)
     */
    protected Instant applyCommunicationTriggersTo(Optional<Instant> nextExecutionTimeStamp) {
        Optional<Instant> nextExecutionTimeStampAccordingToTriggers = calculateNextExecutionTimestampFromTriggers();
        if (nextExecutionTimeStamp.isPresent()) {
            return nextExecutionTimeStampAccordingToTriggers.isPresent() ? earliestOf(nextExecutionTimeStamp.get(), nextExecutionTimeStampAccordingToTriggers.get()) : nextExecutionTimeStamp.get();
        } else {
            return nextExecutionTimeStampAccordingToTriggers.isPresent() ? nextExecutionTimeStampAccordingToTriggers.get() : null;
        }
    }

    private Instant earliestOf(Instant timeStamp1, Instant timeStamp2) {
        return timeStamp1.isBefore(timeStamp2) ? timeStamp1 : timeStamp2;
    }

    /**
     * Calculates the next execution timestamp based on the list of {@link ComTaskExecutionTrigger}s
     * <b>Remark:</b> for this calculation, all ComTaskExecutionTriggers having a timeStamp after {@link #getLastExecutionStartTimestamp()}
     * are taken into account. If {@link #getLastExecutionStartTimestamp()} returns null, then all ComTaskExecutionTriggers after the current time (now) will
     * be taken into account.
     *
     * @return An optional containing the next execution timestamp, or Optional.empty() in case there were no ComTaskExecutionTriggers for this ComTaskExecution
     */
    private Optional<Instant> calculateNextExecutionTimestampFromTriggers() {
        Optional<ComTaskExecutionTrigger> earliestComTaskExecutionTrigger = getComTaskExecutionTriggers().stream()
                .filter(comTaskExecutionTrigger -> getLastExecutionStartTimestamp() == null || comTaskExecutionTrigger.getTriggerTimeStamp().isAfter(getLastExecutionStartTimestamp()))
                .sorted((e1, e2) -> e1.getTriggerTimeStamp().compareTo(e2.getTriggerTimeStamp())).findFirst();
        return earliestComTaskExecutionTrigger.isPresent() ? Optional.of(earliestComTaskExecutionTrigger.get().getTriggerTimeStamp()) : Optional.empty();
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

    void setLastExecutionStartTimestamp(Instant lastExecutionStartTimestamp) {
        this.lastExecutionTimestamp = lastExecutionStartTimestamp;
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
        } else {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(this.clock.getZone()));
            calendar.setTimeInMillis(nextExecutionTimestamp.toEpochMilli());
            calendar.add(Calendar.MILLISECOND, -1); // hack getNextTimeStamp to be inclusive
            return getScheduledConnectionTask().getNextExecutionSpecs().getNextTimestamp(calendar).toInstant();
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
    public boolean isOnHold() {
        return this.onHold;
    }

    @Override
    public void putOnHold() {
        this.onHold = true;
        this.schedule(null);
    }

    @Override
    public void resume() {
        this.onHold = false;
        this.schedule(getPlannedNextExecutionTimestamp());
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
        this.nextExecutionTimestamp = applyComWindowIfOutboundAndAny(currentDate);
        if (this.getId() > 0) {
            this.updateForScheduling(false);
        }
        if (this.connectionTaskIsScheduled()) {
            ((ScheduledConnectionTaskImpl) this.getConnectionTask().get()).scheduleConnectionNow();
        }
    }

    @Override
    public void schedule(Instant when) {
        if (!isOnHold() || when == null) {
            this.schedule(when, this.getPlannedNextExecutionTimestamp());
            if (this.getId() > 0) {
                this.updateForScheduling(true);
            }
        }
    }

    @Override
    public ComTaskExecutionUpdater getUpdater() {
        return new ComTaskExecutionUpdaterImpl(this);
    }

    @Override
    public List<ProtocolTask> getProtocolTasks() {
        return Collections.unmodifiableList(this.getComTask().getProtocolTasks());
    }

    @Override
    public boolean executesComTask(ComTask comTask) {
        return comTask != null && comTask.getId() == this.comTask.get().getId();
    }

    // 'functional' fields do not need a 'versioncount upgrade'. When rescheduling a comtaskexecution
    // you do not want a new version (no history log) -> only tell the system the comtaskexecution is rescheduled
    private void updateForScheduling(boolean informConnectionTask) {
        this.update(ComTaskExecutionFields.COMPORT.fieldName(),
                ComTaskExecutionFields.LASTSUCCESSFULCOMPLETIONTIMESTAMP.fieldName(),
                ComTaskExecutionFields.LASTEXECUTIONFAILED.fieldName(),
                ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName(),
                ComTaskExecutionFields.CURRENTRETRYCOUNT.fieldName(),
                ComTaskExecutionFields.EXECUTIONSTART.fieldName(),
                ComTaskExecutionFields.ONHOLD.fieldName(),
                ComTaskExecutionFields.PLANNEDNEXTEXECUTIONTIMESTAMP.fieldName());

        if(informConnectionTask) {
            this.getConnectionTask().ifPresent(ct -> {
                if(!calledByConnectionTask) {
                    ((ServerConnectionTask) ct).scheduledComTaskRescheduled(this);
                }
                calledByConnectionTask = false;
            });
        }
    }

    @Override
    public void setLockedComPort(ComPort comPort) {
        setExecutingComPort(comPort);
        this.update(ComTaskExecutionFields.COMPORT.fieldName());
    }

    @Override
    public void unlock() {
        this.setExecutingComPort(null);
        this.update(ComTaskExecutionFields.COMPORT.fieldName());
    }

    @Override
    public void executionCompleted() {
        this.markSuccessfullyCompleted();
        this.doReschedule(calculateNextExecutionTimestamp(clock.instant()));
        updateForScheduling(true);
    }

    @Override
    public void executionRescheduled(Instant rescheduleDate) {
        this.doReschedule(rescheduleDate);
        updateForScheduling(true);
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
        updateForScheduling(true);
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
    public long getVersion() {
        return this.version;
    }

    @Override
    public void executionStarted(ComPort comPort) {
        this.doExecutionStarted(comPort);
        this.update(ComTaskExecutionFields.EXECUTIONSTART.fieldName(),
                ComTaskExecutionFields.LASTEXECUTIONTIMESTAMP.fieldName(),
                ComTaskExecutionFields.LASTEXECUTIONFAILED.fieldName(),
                ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName(),
                ComTaskExecutionFields.PLANNEDNEXTEXECUTIONTIMESTAMP.fieldName(),
                ComTaskExecutionFields.COMPORT.fieldName()
        );
        this.updateEventType();
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
        this.update();
    }

    @Override
    public List<ComTaskExecutionTrigger> getComTaskExecutionTriggers() {
        return comTaskExecutionTriggers;
    }

    @Override
    public void addNewComTaskExecutionTrigger(Instant triggerTimeStamp) {
        if (!getComTaskExecutionTriggers().stream().anyMatch(trigger -> trigger.getTriggerTimeStamp().getEpochSecond() == triggerTimeStamp.getEpochSecond())) {
            ComTaskExecutionTriggerImpl comTaskExecutionTrigger = ComTaskExecutionTriggerImpl.from(getDataModel(), this, triggerTimeStamp);
            getComTaskExecutionTriggers().add(comTaskExecutionTrigger);
            comTaskExecutionTrigger.notifyCreated();
        }
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

    public void setNextExecutionSpecsFrom(TemporalExpression temporalExpression) {
        if (temporalExpression == null) {
            if (!this.nextExecutionSpecs.isPresent()) {
                // No change
            } else {
                this.nextExecutionSpecs.setNull();
                this.setNextExecutionTimestamp(null);   // Clear the next execution timestamp
            }
        } else {
            if (this.nextExecutionSpecs.isPresent()) {
                this.nextExecutionSpecs.get().setTemporalExpression(temporalExpression);
                this.nextExecutionSpecs.get().update();
            } else {
                NextExecutionSpecs nextExecutionSpecs1 = this.getSchedulingService().newNextExecutionSpecs(temporalExpression);
                this.nextExecutionSpecs.set(nextExecutionSpecs1);
            }
        }
        this.recalculateNextAndPlannedExecutionTimestamp();
    }

    public ComTaskExecutionImpl initializeForScheduledComTask(Device device, ComTaskEnablement comTaskEnablement, ComSchedule comSchedule) {
        this.initializeFrom(device, comTaskEnablement);
        this.setIgnoreNextExecutionSpecsForInbound(true);

        this.behavior = new ScheduledBehavior();
        this.comSchedule.set(comSchedule);

        nextExecutionSpecs.set(comSchedule.getNextExecutionSpecs());
        return this;
    }

    public ComTaskExecutionImpl initializeFirmwareTask(DeviceImpl device, ComTaskEnablement comTaskEnablement) {
        this.initializeFrom(device, comTaskEnablement);
        this.behavior = new FirmwareBehavior();
        return this;
    }

    public ComTaskExecutionImpl initializeManualScheduled(Device device, ComTaskEnablement comTaskEnablement, TemporalExpression temporalExpression) {
        this.initializeFrom(device, comTaskEnablement);
        this.behavior = new ManualBehavior();
        if (temporalExpression != null) {
            this.setNextExecutionSpecsFrom(temporalExpression);
        }
        return this;
    }

    public ComTaskExecutionImpl initializeAdhoc(Device device, ComTaskEnablement comTaskEnablement) {
        this.initializeFrom(device, comTaskEnablement);
        this.behavior = new ManualBehavior();
        return this;
    }

    private void setConnectionTaskIfExists(Device device, ComTaskEnablement comTaskEnablement) {
        Optional<PartialConnectionTask> optionalPartialConnectionTask = comTaskEnablement.getPartialConnectionTask();
        if (optionalPartialConnectionTask.isPresent()) {
            PartialConnectionTask partialConnectionTask = optionalPartialConnectionTask.get();
            device.getConnectionTasks()
                    .stream()
                    .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == partialConnectionTask.getId())
                    .forEach(this::setConnectionTask);
        }
    }

    /**
     * We don't do our own persistence, our device will take care of that.
     */
    public void prepareForSaving() {
        recalculateNextAndPlannedExecutionTimestamp();
        validateNotObsolete();
        Save.CREATE.validate(getDataModel(), this, Save.Create.class, Save.Update.class);
    }

    protected Instant now() {
        return clock.instant();
    }

    protected void validateNotObsolete() {
        if (this.obsoleteDate != null) {
            throw new CannotUpdateObsoleteComTaskExecutionException(this, this.getThesaurus(), MessageSeeds.COM_TASK_IS_OBSOLETE_AND_CAN_NOT_BE_UPDATED);
        }
    }

    @Override
    public long getConnectionTaskId() {
        return connectionTaskId;
    }

    @Override
    public List<ComTask> getComTasks() {
        return Collections.singletonList(getComTask());
    }

    @Override
    public ComTask getComTask() {
        return this.comTask.get();
    }

    @Override
    public Optional<ComSchedule> getComSchedule() {
        return this.comSchedule.getOptional();
    }

    @Override
    public boolean isConfiguredToCollectRegisterData() {
        return this.getBehavior().isConfiguredToCollectRegisterData();
    }

    @Override
    public boolean isConfiguredToCollectLoadProfileData() {
        return this.getBehavior().isConfiguredToCollectLoadProfileData();
    }

    @Override
    public boolean isConfiguredToRunBasicChecks() {
        return this.getBehavior().isConfiguredToRunBasicChecks();
    }

    @Override
    public boolean isConfiguredToCheckClock() {
        return this.getBehavior().isConfiguredToCheckClock();
    }

    @Override
    public boolean isConfiguredToCollectEvents() {
        return this.getBehavior().isConfiguredToCollectEvents();
    }

    @Override
    public boolean isConfiguredToSendMessages() {
        return this.getBehavior().isConfiguredToSendMessages();
    }

    @Override
    public boolean isConfiguredToReadStatusInformation() {
        return this.getBehavior().isConfiguredToReadStatusInformation();
    }

    @Override
    public boolean isConfiguredToUpdateTopology() {
        return this.getBehavior().isConfiguredToUpdateTopology();
    }


    interface Behavior extends DataCollectionConfiguration {

        /**
         * Tests if this ComTaskExecution is for a {@link ComSchedule}
         * that defines both the scheduling frequency and the
         * actual {@link ComTask}s that will be executed.
         *
         * @return A flag that indicates if this ComTaskExecution is for a ComSchedule
         */
        boolean usesSharedSchedule();

        /**
         * Tests if this ComTaskExecution is scheduled manually,
         * i.e. it has a scheduling frequency causing it to be
         * executed frequently but not at a frequency defined by a
         * {@link com.energyict.mdc.scheduling.model.ComSchedule}
         * but by a one shot setting provided by the user.
         *
         * @return A flag that indicates if this ComTaskExecution is scheduled manually
         */
        boolean isScheduledManually();

        /**
         * Tests if this ComTaskExecution is ad hoc,
         * i.e. it was meant to be executed once and only once.
         *
         * @return A flag that indicates if this ComTaskExecution is adhoc
         */
        boolean isAdHoc();

        /**
         * Gets the maximum number of consecutive failures a ComTaskExecution can have before marking it as failed.
         *
         * @return the maximum number of consecutive failures
         */
        int getMaxNumberOfTries();

        /**
         * Gets the specifications for the calculation of the next
         * execution timestamp of this ComTaskExecution.
         * Note that ad-hoc ComTaskExecution do not have such a specification.
         *
         * @return The NextExecutionSpecs
         */
        Optional<NextExecutionSpecs> getNextExecutionSpecs();

        /**
         * Test if this ComTaskExecution is a firmware related comtaskexecution
         *
         * @return a flag that indicates if this ComTaskExecution is firmware related
         */
        boolean isFirmware();

    }

    private class FirmwareBehavior implements Behavior {

        public FirmwareBehavior() {
            ComTaskExecutionImpl.this.comTaskExecType = ComTaskExecType.FIRMWARE_COM_TASK_EXECUTION_DISCRIMINATOR;
        }

        @Override
        public boolean usesSharedSchedule() {
            return false;
        }

        @Override
        public boolean isScheduledManually() {
            return false;
        }

        @Override
        public boolean isAdHoc() {
            return true;
        }

        @Override
        public int getMaxNumberOfTries() {
            return getComTask().getMaxNumberOfTries();
        }

        @Override
        public Optional<NextExecutionSpecs> getNextExecutionSpecs() {
            return Optional.empty();
        }

        @Override
        public boolean isFirmware() {
            return true;
        }

        @Override
        public boolean isConfiguredToCollectRegisterData() {
            return false;
        }

        @Override
        public boolean isConfiguredToCollectLoadProfileData() {
            return false;
        }

        @Override
        public boolean isConfiguredToRunBasicChecks() {
            return false;
        }

        @Override
        public boolean isConfiguredToCheckClock() {
            return false;
        }

        @Override
        public boolean isConfiguredToCollectEvents() {
            return false;
        }

        @Override
        public boolean isConfiguredToSendMessages() {
            return false;
        }

        @Override
        public boolean isConfiguredToReadStatusInformation() {
            return false;
        }

        @Override
        public boolean isConfiguredToUpdateTopology() {
            return false;
        }
    }

    private class ManualBehavior implements Behavior {

        public ManualBehavior() {
            ComTaskExecutionImpl.this.comTaskExecType = ComTaskExecType.MANUALLY_SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR;
        }

        @Override
        public boolean usesSharedSchedule() {
            return false;
        }

        @Override
        public boolean isScheduledManually() {
            return true;
        }

        @Override
        public boolean isAdHoc() {
            return !getNextExecutionSpecs().isPresent();
        }

        @Override
        public int getMaxNumberOfTries() {
            return getComTask().getMaxNumberOfTries();
        }

        @Override
        public Optional<NextExecutionSpecs> getNextExecutionSpecs() {
            return ComTaskExecutionImpl.this.nextExecutionSpecs.getOptional();

        }

        @Override
        public boolean isFirmware() {
            return false;
        }

        @Override
        public boolean isConfiguredToCollectRegisterData() {
            return isConfiguredToCollectDataOfClass(RegistersTask.class);
        }

        @Override
        public boolean isConfiguredToCollectLoadProfileData() {
            return isConfiguredToCollectDataOfClass(LoadProfilesTask.class);
        }

        @Override
        public boolean isConfiguredToRunBasicChecks() {
            return isConfiguredToCollectDataOfClass(BasicCheckTask.class);
        }

        @Override
        public boolean isConfiguredToCheckClock() {
            return isConfiguredToCollectDataOfClass(ClockTask.class);
        }

        @Override
        public boolean isConfiguredToCollectEvents() {
            return isConfiguredToCollectDataOfClass(LogBooksTask.class);
        }

        @Override
        public boolean isConfiguredToSendMessages() {
            return isConfiguredToCollectDataOfClass(MessagesTask.class);
        }

        @Override
        public boolean isConfiguredToReadStatusInformation() {
            return isConfiguredToCollectDataOfClass(StatusInformationTask.class);
        }

        @Override
        public boolean isConfiguredToUpdateTopology() {
            return isConfiguredToCollectDataOfClass(TopologyTask.class);
        }

        private <T extends ProtocolTask> boolean isConfiguredToCollectDataOfClass(Class<T> protocolTaskClass) {
            for (ProtocolTask protocolTask : ComTaskExecutionImpl.this.getComTask().getProtocolTasks()) {
                if (protocolTaskClass.isAssignableFrom(protocolTask.getClass())) {
                    return true;
                }
            }
            return false;
        }
    }

    private class ScheduledBehavior implements Behavior {

        public ScheduledBehavior() {
            ComTaskExecutionImpl.this.comTaskExecType = ComTaskExecType.SHARED_SCHEDULE_COM_TASK_EXECUTION_DISCRIMINATOR;
        }

        @Override
        public boolean usesSharedSchedule() {
            return true;
        }

        @Override
        public boolean isScheduledManually() {
            return false;
        }

        @Override
        public boolean isAdHoc() {
            return false;
        }

        @Override
        public int getMaxNumberOfTries() {
            int minimalNrOfRetries = Integer.MAX_VALUE;
            for (ComTask comTask : getComSchedule().get().getComTasks()) {
                if (comTask.getMaxNumberOfTries() < minimalNrOfRetries) {
                    minimalNrOfRetries = comTask.getMaxNumberOfTries();
                }
            }
            return minimalNrOfRetries;
        }

        @Override
        public Optional<NextExecutionSpecs> getNextExecutionSpecs() {
            return Optional.of(getComSchedule().get().getNextExecutionSpecs());
        }

        @Override
        public boolean isFirmware() {
            return false;
        }

        public Optional<ComSchedule> getComSchedule() {
            return Optional.of(ComTaskExecutionImpl.this.comSchedule.get());
        }

        @Override
        public boolean isConfiguredToCollectRegisterData() {
            return this.getComSchedule().get().isConfiguredToCollectRegisterData();
        }

        @Override
        public boolean isConfiguredToCollectLoadProfileData() {
            return this.getComSchedule().get().isConfiguredToCollectLoadProfileData();
        }

        @Override
        public boolean isConfiguredToRunBasicChecks() {
            return this.getComSchedule().get().isConfiguredToRunBasicChecks();
        }

        @Override
        public boolean isConfiguredToCheckClock() {
            return this.getComSchedule().get().isConfiguredToCheckClock();
        }

        @Override
        public boolean isConfiguredToCollectEvents() {
            return this.getComSchedule().get().isConfiguredToCollectEvents();
        }

        @Override
        public boolean isConfiguredToSendMessages() {
            return this.getComSchedule().get().isConfiguredToSendMessages();
        }

        @Override
        public boolean isConfiguredToReadStatusInformation() {
            return this.getComSchedule().get().isConfiguredToReadStatusInformation();
        }

        @Override
        public boolean isConfiguredToUpdateTopology() {
            return this.getComSchedule().get().isConfiguredToUpdateTopology();
        }
    }

    public static class SingleScheduledComTaskExecutionBuilder extends AbstractComTaskExecutionBuilder {

        public SingleScheduledComTaskExecutionBuilder(ComTaskExecutionImpl instance) {
            super(instance);
        }
    }

    public abstract static class AbstractComTaskExecutionBuilder implements ComTaskExecutionBuilder {

        private final ComTaskExecutionImpl comTaskExecution;

        protected AbstractComTaskExecutionBuilder(ComTaskExecutionImpl instance) {
            this.comTaskExecution = instance;
        }

        public ComTaskExecution getComTaskExecution() {
            return this.comTaskExecution;
        }

        @Override
        public ComTaskExecutionBuilder useDefaultConnectionTask(boolean useDefaultConnectionTask) {
            this.comTaskExecution.setUseDefaultConnectionTask(useDefaultConnectionTask);
            return this;
        }

        @Override
        public ComTaskExecutionBuilder connectionTask(ConnectionTask<?, ?> connectionTask) {
            this.comTaskExecution.setConnectionTask(connectionTask);
            this.comTaskExecution.setUseDefaultConnectionTask(false);
            this.comTaskExecution.recalculateNextAndPlannedExecutionTimestamp();
            return this;
        }

        @Override
        public ComTaskExecutionBuilder priority(int priority) {
            this.comTaskExecution.setPlannedPriority(priority);
            return this;
        }

        @Override
        public ComTaskExecutionBuilder ignoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound) {
            this.comTaskExecution.setIgnoreNextExecutionSpecsForInbound(ignoreNextExecutionSpecsForInbound);
            return this;
        }

        @Override
        public ComTaskExecutionBuilder scheduleNow() {
            this.comTaskExecution.scheduleNow();
            return this;
        }

        @Override
        public ComTaskExecutionBuilder runNow() {
            this.comTaskExecution.runNow();
            return this;
        }

        @Override
        public void putOnHold() {
            this.comTaskExecution.putOnHold();
        }

        @Override
        public void resume() {
            this.comTaskExecution.resume();
        }

        @Override
        @SuppressWarnings("unchecked")
        public ComTaskExecution add() {
            this.comTaskExecution.prepareForSaving();
            this.comTaskExecution.getConnectionTask().ifPresent(ct -> ((ServerConnectionTask) ct).scheduledComTaskRescheduled(this.comTaskExecution));
            return this.comTaskExecution;
        }

    }

    public class ComTaskExecutionUpdaterImpl implements ComTaskExecutionUpdater {

        private final ComTaskExecutionImpl comTaskExecution;
        private boolean connectionTaskSchedulingMayHaveChanged = false;

        protected ComTaskExecutionUpdaterImpl(ComTaskExecutionImpl comTaskExecution) {
            this.comTaskExecution = comTaskExecution;
        }

        public ComTaskExecutionImpl getComTaskExecution() {
            return this.comTaskExecution;
        }

        protected ComTaskExecutionUpdater self() {
            return this;
        }

        @Override
        public ComTaskExecutionUpdater useDefaultConnectionTask(boolean useDefaultConnectionTask) {
            this.comTaskExecution.setUseDefaultConnectionTask(useDefaultConnectionTask);
            return this;
        }

        @Override
        public ComTaskExecutionUpdater connectionTask(ConnectionTask<?, ?> connectionTask) {
            this.comTaskExecution.setConnectionTask(connectionTask);
            this.comTaskExecution.setUseDefaultConnectionTask(false);
            this.comTaskExecution.recalculateNextAndPlannedExecutionTimestamp();
            return this;
        }

        @Override
        public ComTaskExecutionUpdater priority(int executionPriority) {
            this.comTaskExecution.setPlannedPriority(executionPriority);
            return this;
        }

        @Override
        public ComTaskExecutionUpdater ignoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound) {
            this.comTaskExecution.setIgnoreNextExecutionSpecsForInbound(ignoreNextExecutionSpecsForInbound);
            return this;
        }

        @Override
        public ComTaskExecutionUpdater forceNextExecutionTimeStampAndPriority(Instant nextExecutionTimestamp, int priority) {
            this.connectionTaskSchedulingMayHaveChanged = true;
            this.comTaskExecution.setNextExecutionTimestamp(nextExecutionTimestamp);
            this.comTaskExecution.setExecutingPriority(priority);
            return this;
        }

        @Override
        public ComTaskExecutionUpdater forceLastExecutionStartTimestamp(Instant lastExecutionStartTimestamp) {
            this.comTaskExecution.setLastExecutionStartTimestamp(lastExecutionStartTimestamp);
            return this;
        }

        @Override
        public ComTaskExecutionUpdater useDefaultConnectionTask(ConnectionTask<?, ?> defaultConnectionTask) {
            this.comTaskExecution.setDefaultConnectionTask(defaultConnectionTask);
            this.connectionTaskSchedulingMayHaveChanged = true;
            return this;
        }

        @Override
        public ComTaskExecutionUpdater calledByComTaskExecution() {
            this.comTaskExecution.calledByConnectionTask = true;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ComTaskExecutionImpl update() {
            this.comTaskExecution.update();
            if (this.connectionTaskSchedulingMayHaveChanged) {
                this.comTaskExecution.getConnectionTask().ifPresent(ct -> ((ServerConnectionTask) ct).scheduledComTaskRescheduled(this.comTaskExecution));
            }
            return this.comTaskExecution;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ComTaskExecutionImpl updateFields(String... fieldNames) {
            this.comTaskExecution.update(fieldNames);
            return this.comTaskExecution;
        }

        @Override
        public ComTaskExecutionUpdater createNextExecutionSpecs(TemporalExpression temporalExpression) {
            this.comTaskExecution.comSchedule = ValueReference.absent();
            this.comTaskExecution.setNextExecutionSpecsFrom(temporalExpression);
            this.comTaskExecution.behavior = new ManualBehavior();
            return this;
        }

        @Override
        public ComTaskExecutionUpdater addSchedule(ComSchedule comSchedule) {
            this.comTaskExecution.setIgnoreNextExecutionSpecsForInbound(true);
            nextExecutionSpecs.set(comSchedule.getNextExecutionSpecs());
            this.comTaskExecution.comSchedule.set(comSchedule);
            this.comTaskExecution.behavior = new ScheduledBehavior();
            if (this.comTaskExecution.getNextExecutionTimestamp() == null) {
                this.comTaskExecution.recalculateNextAndPlannedExecutionTimestamp();
            }
            return this;
        }

        @Override
        public ComTaskExecutionUpdater removeSchedule() {
            this.comTaskExecution.behavior = new ManualBehavior();
            this.comTaskExecution.comSchedule = ValueReference.absent();
            this.comTaskExecution.nextExecutionSpecs = ValueReference.absent();
            return this;
        }

        @Override
        public ComTaskExecutionUpdater removeNextExecutionSpec() {
            this.comTaskExecution.behavior = new ManualBehavior();
            this.comTaskExecution.nextExecutionSpecs = ValueReference.absent();
            return this;
        }
    }

}