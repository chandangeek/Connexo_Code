package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.EarliestNextExecutionTimeStampAndPriority;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.SerialConnectionPropertyNames;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Instant;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link ScheduledConnectionTask} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-16 (11:07)
 */
@ValidNextExecutionSpecsWithMinimizeConnectionsStrategy(groups = {Save.Create.class, Save.Update.class})
public class ScheduledConnectionTaskImpl extends OutboundConnectionTaskImpl<PartialScheduledConnectionTask> implements ScheduledConnectionTask, PersistenceAware {

    private final SchedulingService schedulingService;
    private ComWindow comWindow;
    private Reference<NextExecutionSpecs> nextExecutionSpecs = ValueReference.absent();
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.OUTBOUND_CONNECTION_TASK_STRATEGY_REQUIRED + "}")
    private ConnectionStrategy connectionStrategy;
    private Instant nextExecutionTimestamp;
    private Instant plannedNextExecutionTimestamp;
    @SuppressWarnings("unused")
    private int priority;
    private boolean allowSimultaneousConnections;
    private Reference<ConnectionInitiationTask> initiationTask = ValueReference.absent();
    private int maxNumberOfTries = -1;
    private UpdateStrategy updateStrategy = new Noop();

    private final ServerCommunicationTaskService communicationTaskService;

    @Inject
    protected ScheduledConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ServerConnectionTaskService connectionTaskService, ServerCommunicationTaskService communicationTaskService, ProtocolPluggableService protocolPluggableService, SchedulingService schedulingService) {
        super(dataModel, eventService, thesaurus, clock, connectionTaskService, communicationTaskService, protocolPluggableService);
        this.schedulingService = schedulingService;
        this.communicationTaskService = communicationTaskService;
    }

    @Override
    public void postLoad() {
        super.postLoad();
        this.setCommunicationWindowToNullWhenEmpty();
    }

    private void setCommunicationWindowToNullWhenEmpty() {
        if (this.comWindow != null && this.comWindow.isEmpty()) {
            this.comWindow = null;
        }
    }

    @Override
    protected void validateAndCreate() {
        if (this.nextExecutionSpecs.isPresent()) {
            this.getNextExecutionSpecs().save();
        }
        super.validateAndCreate();
        if (this.getNextExecutionSpecs() != null) {
            this.doUpdateNextExecutionTimestamp(PostingMode.NOW);
        } else {
            // ConnectionStrategy must be ASAP
            this.updateNextExecutionTimeStampBasedOnComTask();
        }
    }

    @Override
    public void save() {
        super.save();
        this.updateStrategy = new Noop();
    }

    @Override
    protected void validateAndUpdate() {
        this.updateStrategy.prepare();
        super.validateAndUpdate();
        this.updateStrategy.complete();
    }

    @Override
    protected void update() {
        this.updateStrategy.prepare();
        super.update();
        this.updateStrategy.complete();
    }

    @Override
    protected void doDelete() {
        NextExecutionSpecs nextExecutionSpecs = this.getNextExecutionSpecs();
        super.doDelete();
        if (nextExecutionSpecs != null) {
            nextExecutionSpecs.delete();
        }
    }

    @Override
    public NextExecutionSpecs getNextExecutionSpecs() {
        return this.updateStrategy.getNextExecutionSpecs();
    }

    @Override
    public void setNextExecutionSpecsFrom(TemporalExpression temporalExpression) {
        // Ignore the new value in case of ASAP
        if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(this.getConnectionStrategy())) {
            if (this.nextExecutionSpecs.isPresent() && this.getId() != 0) {
                this.updateStrategy = this.updateStrategy.schedulingChanged(temporalExpression);
            } else {
                this.updateStrategy = this.updateStrategy.createSchedule(temporalExpression);
            }
        }
    }

    private void setNextExecutionSpecs(NextExecutionSpecs nextExecutionSpecs) {
        this.nextExecutionSpecs.set(nextExecutionSpecs);
    }

    @Override
    public ComWindow getCommunicationWindow() {
        return this.comWindow;
    }

    @Override
    public void setCommunicationWindow(ComWindow comWindow) {
        this.comWindow = comWindow;
    }

    @Override
    public ConnectionStrategy getConnectionStrategy() {
        return connectionStrategy;
    }

    @Override
    public void setConnectionStrategy(ConnectionStrategy newConnectionStrategy) {
        this.updateStrategy = this.updateStrategy.connectionStrategyChanged(this.connectionStrategy, newConnectionStrategy);
        this.connectionStrategy = newConnectionStrategy;
    }

    private void prepareStrategyChange(ConnectionStrategy oldStrategy) {
        if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(oldStrategy)) {
            // Old strategy is to minimize connections and therefore the new strategy must be as soon as possible
            this.updateNextExecutionTimeStampBasedOnComTask();
        } else {
            // Old strategy is asap and therefore the new strategy is to minimize connections
            this.updateNextExecutionTimestamp(PostingMode.LATER);
            this.rescheduleComTaskExecutions();
        }
    }

    /**
     * Sets the {@link #nextExecutionTimestamp} based on the nextExecutionTimeStamp of the dependant ComTaskExecutions.
     */
    private void updateNextExecutionTimeStampBasedOnComTask() {
        EarliestNextExecutionTimeStampAndPriority earliestNextExecutionTimeStampAndPriority = this.getEarliestNextExecutionTimeStampAndPriority();
        if (earliestNextExecutionTimeStampAndPriority != null) {
            if (ConnectionStrategy.AS_SOON_AS_POSSIBLE.equals(this.getConnectionStrategy())) {
                this.doAsSoonAsPossibleSchedule(earliestNextExecutionTimeStampAndPriority.earliestNextExecutionTimestamp, PostingMode.LATER);
            } else {
                this.doMinimizeConnectionsSchedule(earliestNextExecutionTimeStampAndPriority.earliestNextExecutionTimestamp, PostingMode.LATER);
            }
        }
    }

    /**
     * Updates the next execution timestamps of the dependent ComTaskExecutions.
     */
    private void rescheduleComTaskExecutions() {
        for (ComTaskExecution comTaskExecution : this.getScheduledComTasks()) {
            if (!comTaskExecution.isOnHold()) {
                if (comTaskExecution.usesSharedSchedule()) {
                    comTaskExecution.updateNextExecutionTimestamp();
                } else {
                    comTaskExecution.schedule(comTaskExecution.getNextExecutionTimestamp());
                }
            }
        }
    }

    private EarliestNextExecutionTimeStampAndPriority getEarliestNextExecutionTimeStampAndPriority() {
        Condition condition = where(ComTaskExecutionFields.CONNECTIONTASK.fieldName()).isEqualTo(this)
                .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());

        List<ComTaskExecution> comTaskExecutions = this.getDataModel().mapper(ComTaskExecution.class).select(condition,
                Order.ascending(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()),
                Order.descending(ComTaskExecutionFields.PLANNED_PRIORITY.fieldName()));

        if (!comTaskExecutions.isEmpty()) {
            ComTaskExecution comTaskExecution = comTaskExecutions.get(0);
            return new EarliestNextExecutionTimeStampAndPriority(comTaskExecution.getNextExecutionTimestamp(), comTaskExecution.getExecutionPriority());
        }
        return null;
    }

    @Override
    protected void doSetAsDefault() {
        super.doSetAsDefault();
        this.notifyComTaskExecutionsThatDefaultHasBeenSet(PostingMode.LATER);
    }

    protected void notifyComTaskExecutionsThatDefaultHasBeenSet(PostingMode postingMode) {
        EarliestNextExecutionTimeStampAndPriority earliestNextExecutionTimestampAndPriority = this.getEarliestNextExecutionTimeStampAndPriority();
        if (ConnectionStrategy.AS_SOON_AS_POSSIBLE.equals(this.getConnectionStrategy())) {
            if (earliestNextExecutionTimestampAndPriority != null) {
                this.doAsSoonAsPossibleSchedule(
                        earliestNextExecutionTimestampAndPriority.earliestNextExecutionTimestamp,
                        earliestNextExecutionTimestampAndPriority,
                        postingMode);
            }
        } else {
            int priority;
            if (earliestNextExecutionTimestampAndPriority == null) {
                priority = TaskPriorityConstants.DEFAULT_PRIORITY;
            } else {
                priority = earliestNextExecutionTimestampAndPriority.priority;
            }
            this.synchronizeScheduledComTaskExecution(this.getNextExecutionTimestamp(), priority);
        }
    }

    @Override
    public Instant updateNextExecutionTimestamp() {
        return this.updateNextExecutionTimestamp(PostingMode.NOW);
    }

    private Instant updateNextExecutionTimestamp(PostingMode postingMode) {
        if (isActive() && this.getNextExecutionSpecs() != null) {
            return this.doUpdateNextExecutionTimestamp(postingMode);
        } else {
            return null;
        }
    }

    private Instant doUpdateNextExecutionTimestamp(PostingMode postingMode) {
        Calendar calendar = Calendar.getInstance(getClocksTimeZone());
        calendar.setTimeInMillis(this.now().toEpochMilli());
        this.plannedNextExecutionTimestamp = this.applyComWindowIfAny(this.getNextExecutionSpecs().getNextTimestamp(calendar).toInstant());
        return this.schedule(this.plannedNextExecutionTimestamp, postingMode);
    }

    @Override
    public void scheduledComTaskRescheduled(ComTaskExecution comTask) {
        if (ConnectionStrategy.AS_SOON_AS_POSSIBLE.equals(this.getConnectionStrategy())) {
            this.schedule(comTask.getNextExecutionTimestamp());
        }
    }

    @Override
    public void scheduledComTaskChangedPriority(ComTaskExecution comTask) {
        if (this.needToSynchronizePriorityChanges()) {
            EarliestNextExecutionTimeStampAndPriority earliestNextExecutionTimeStampAndPriority = this.getEarliestNextExecutionTimeStampAndPriority();

            /* earliestNextExecutionTimeStampAndPriority is only null when there are
             * no scheduled com tasks, but since this method is called by one, there's got to be at least one right. */
            if (earliestNextExecutionTimeStampAndPriority != null) {
                updateNextExecutionTimeStampAndPriority(earliestNextExecutionTimeStampAndPriority.earliestNextExecutionTimestamp, earliestNextExecutionTimeStampAndPriority.priority);
            }
        }
    }

    private void updateNextExecutionTimeStampAndPriority(Instant nextExecutionTimestamp, int priority) {
        Condition condition = where(ComTaskExecutionFields.CONNECTIONTASK.fieldName()).isEqualTo(this)
                .and(where(ComTaskExecutionFields.PLANNEDNEXTEXECUTIONTIMESTAMP.fieldName()).isLessThan(nextExecutionTimestamp))
                .and(where("obsoleteDate").isNull());
        List<ComTaskExecution> comTaskExecutions = this.getDataModel().mapper(ComTaskExecution.class).select(condition);
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            ComTaskExecutionUpdater<? extends ComTaskExecutionUpdater<?, ?>, ? extends ComTaskExecution> comTaskExecutionUpdater = comTaskExecution.getUpdater();
            comTaskExecutionUpdater.forceNextExecutionTimeStampAndPriority(nextExecutionTimestamp, priority);
            comTaskExecutionUpdater.update();
        }
    }

    @Override
    public void setDynamicMaxNumberOfTries(int maxNumberOfTries) {
        this.maxNumberOfTries = maxNumberOfTries;
    }

    private boolean needToSynchronizePriorityChanges() {
        return !this.allowsSimultaneousConnections();
    }

    @Override
    protected void doExecutionCompleted() {
        super.doExecutionCompleted();
        if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(getConnectionStrategy())) {
            this.schedule(this.calculateNextPlannedExecutionTimestamp());
        }
    }

    private Instant calculateNextPlannedExecutionTimestamp() {
        return this.applyComWindowIfAny(this.calculateNextExecutionTimestamp(this.now()));
    }

    public Instant applyComWindowIfAny(Instant calculatedNextExecutionTimestamp) {
        Calendar calendar = Calendar.getInstance(getClocksTimeZone());
        calendar.setTimeInMillis(calculatedNextExecutionTimestamp.toEpochMilli());
        this.applyComWindowIfAny(calendar);
        return calendar.getTime().toInstant();
    }

    private void applyComWindowIfAny(Calendar calendar) {
        ComWindow comWindow = this.getCommunicationWindow();
        if (comWindow != null) {
            if (comWindow.includes(calendar)) {
                return; // All is fine, get out asap
            } else if (comWindow.after(calendar)) {
                comWindow.getStart().copyTo(calendar);
            } else {
                /* Timestamp must be after ComWindow,
                 * advance one day and set time to start of the ComWindow. */
                calendar.add(Calendar.DATE, 1);
                comWindow.getStart().copyTo(calendar);
            }
        }
    }

    @Override
    protected boolean doWeNeedToRetryTheConnectionTask() {
        if (getConnectionStrategy().equals(ConnectionStrategy.AS_SOON_AS_POSSIBLE)) {
            Condition condition =
                    where(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()).isNotNull().
                            and(comTaskNotExecutingCondition()).
                            and(comTaskIsRetrying()).
                            and(connectionTaskIsThisOne());
            return !this.getDataModel().mapper(ComTaskExecution.class).select(condition).isEmpty();
        } else {
            return super.doWeNeedToRetryTheConnectionTask();
        }
    }

    private Condition comTaskNotExecutingCondition() {
        return where(ComTaskExecutionFields.COMPORT.fieldName()).isNull();
    }

    private Condition comTaskIsRetrying() {
        return where(ComTaskExecutionFields.CURRENTRETRYCOUNT.fieldName()).isGreaterThan(0);
    }

    private Condition connectionTaskIsThisOne() {
        return where(ComTaskExecutionFields.CONNECTIONTASK.fieldName()).isEqualTo(this);
    }

    @Override
    protected void doExecutionAttemptFailed() {
        super.doExecutionAttemptFailed();
        this.schedule(this.calculateNextRetryExecutionTimestamp());
    }

    private Instant calculateNextRetryExecutionTimestamp() {
        Instant failureDate = this.now();
        Calendar calendar = Calendar.getInstance(getClocksTimeZone());
        calendar.setTimeInMillis(failureDate.toEpochMilli());
        TimeDuration baseRetryDelay = this.getRescheduleRetryDelay();
        TimeDuration failureRetryDelay = new TimeDuration(baseRetryDelay.getCount() * getCurrentRetryCount(), baseRetryDelay.getTimeUnitCode());
        failureRetryDelay.addTo(calendar);
        this.applyComWindowIfAny(calendar);
        return calendar.getTime().toInstant();
    }

    @Override
    protected void doExecutionFailed() {
        super.doExecutionFailed();
        this.resetCurrentRetryCount();
        if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(getConnectionStrategy())) {
            this.schedule(this.calculateNextPlannedExecutionTimestamp());
        }
    }

    private Instant calculateNextExecutionTimestamp(Instant now) {
        return this.calculateNextExecutionTimestampFromBaseline(now, this.getNextExecutionSpecs());
    }

    private Instant calculateNextExecutionTimestampFromBaseline(Instant baseLine, NextExecutionSpecs nextExecutionSpecs) {
        Calendar calendar = Calendar.getInstance(getClocksTimeZone());
        calendar.setTimeInMillis(baseLine.toEpochMilli());
        if (nextExecutionSpecs != null) {
            return nextExecutionSpecs.getNextTimestamp(calendar).toInstant();
        } else {
            return getPlannedNextExecutionTimestamp();
        }
    }

    @Override
    public Instant scheduleNow() {
        this.getScheduledComTasks().stream().
                filter(comTaskExecution -> EnumSet.of(TaskStatus.Failed, TaskStatus.Retrying, TaskStatus.NeverCompleted, TaskStatus.Pending).contains(comTaskExecution.getStatus())).
                filter(comTaskExecution -> !comTaskExecution.isObsolete()).
                forEach(ComTaskExecution::runNow);
        return scheduleConnectionNow();
    }

    /**
     * Updates the next execution of this ConnectionTask so that it will get picked up as soon as possible.
     * ComTaskExecutions linked to this connection will remain unaltered
     *
     * @return The timestamp on which this ScheduledConnectionTask is scheduled.
     */
    public Instant scheduleConnectionNow() {
        return this.schedule(this.now());
    }

    public Instant schedule(Instant when) {
        return this.schedule(when, PostingMode.NOW);
    }

    private Instant schedule(Instant when, PostingMode postingMode) {
        if (ConnectionStrategy.AS_SOON_AS_POSSIBLE.equals(this.getConnectionStrategy())) {
            return this.doAsSoonAsPossibleSchedule(when, postingMode);
        } else {
            return this.doMinimizeConnectionsSchedule(when, postingMode);
        }
    }

    @Override
    public Instant trigger(final Instant when) {
        if (ConnectionStrategy.AS_SOON_AS_POSSIBLE.equals(this.getConnectionStrategy())) {
            this.triggerComTasks(when);
        }
        this.resetCurrentRetryCount();
        if (ConnectionStrategy.AS_SOON_AS_POSSIBLE.equals(this.getConnectionStrategy())) {
            return this.doAsSoonAsPossibleSchedule(when, PostingMode.NOW);
        } else {
            return this.doMinimizeConnectionsSchedule(when, PostingMode.NOW);
        }
    }

    private void triggerComTasks(Instant when) {
        for (ComTaskExecution scheduledComTask : this.getScheduledComTasks()) {
            if (this.needsTriggering(scheduledComTask)) {
                scheduledComTask.schedule(when);
            }
        }
    }

    private boolean needsTriggering(ComTaskExecution scheduledComTask) {
        Set<TaskStatus> taskStatusesThatRequireTriggering = EnumSet.complementOf(EnumSet.of(TaskStatus.Waiting, TaskStatus.OnHold, TaskStatus.Busy));
        return taskStatusesThatRequireTriggering.contains(scheduledComTask.getStatus());
    }

    @Override
    public Instant getNextExecutionTimestamp() {
        return this.nextExecutionTimestamp;
    }

    protected void setNextExecutionTimestamp(Instant nextExecutionTimestamp) {
        this.nextExecutionTimestamp = nextExecutionTimestamp;
    }

    @Override
    public Instant getPlannedNextExecutionTimestamp() {
        return this.plannedNextExecutionTimestamp;
    }

    @Override
    public ConnectionInitiationTask getInitiatorTask() {
        return this.initiationTask.orNull();
    }

    @Override
    public void setInitiatorTask(ConnectionInitiationTask initiatorTask) {
        this.initiationTask.set(initiatorTask);
    }

    @Override
    public boolean isSimultaneousConnectionsAllowed() {
        return allowSimultaneousConnections;
    }

    @Override
    public void setSimultaneousConnectionsAllowed(boolean allowSimultaneousConnections) {
        this.allowSimultaneousConnections = allowSimultaneousConnections;
    }

    @Override
    public TaskStatus getTaskStatus() {
        return ServerConnectionTaskStatus.getApplicableStatusFor(this, this.now());
    }

    private Instant doAsSoonAsPossibleSchedule(Instant when, PostingMode postingMode) {
        EarliestNextExecutionTimeStampAndPriority earliestNextExecutionTimeStampAndPriority = this.getEarliestNextExecutionTimeStampAndPriority();
        return this.doAsSoonAsPossibleSchedule(when, earliestNextExecutionTimeStampAndPriority, postingMode);
    }

    private Instant doAsSoonAsPossibleSchedule(Instant when, EarliestNextExecutionTimeStampAndPriority earliestNextExecutionTimeStampAndPriority, PostingMode postingMode) {
        if (earliestNextExecutionTimeStampAndPriority == null) {
            // No ComTaskExecutions
            this.applyNextExecutionTimestampAndPriority(when, TaskPriorityConstants.DEFAULT_PRIORITY, postingMode);
            return when;
        } else {
            Instant earliestNextExecutionTimeStamp = earliestNextExecutionTimeStampAndPriority.earliestNextExecutionTimestamp;
            Integer highestPriority = earliestNextExecutionTimeStampAndPriority.priority;
            if (earliestNextExecutionTimeStamp == null
                    || (when != null && when.isBefore(earliestNextExecutionTimeStamp))) {
                this.applyNextExecutionTimestampAndPriority(when, highestPriority, postingMode);
                return when;
            } else if (!earliestNextExecutionTimeStamp.equals(this.getNextExecutionTimestamp())) {
                earliestNextExecutionTimeStamp = this.applyComWindowIfAny(earliestNextExecutionTimeStamp);
                this.applyNextExecutionTimestampAndPriority(earliestNextExecutionTimeStamp, highestPriority, postingMode);
                return earliestNextExecutionTimeStamp;
            } else {
                return this.getNextExecutionTimestamp();
            }
        }
    }

    private Instant doMinimizeConnectionsSchedule(Instant when, PostingMode postingMode) {
        EarliestNextExecutionTimeStampAndPriority earliestNextExecutionTimeStampAndPriority = this.getEarliestNextExecutionTimeStampAndPriority();
        Integer highestPriority;
        if (earliestNextExecutionTimeStampAndPriority == null) {
            highestPriority = TaskPriorityConstants.DEFAULT_PRIORITY;
        } else {
            highestPriority = earliestNextExecutionTimeStampAndPriority.priority;
        }
        this.applyNextExecutionTimestampAndPriority(when, highestPriority, postingMode);
        this.synchronizeScheduledComTaskExecution(when, highestPriority);
        return when;
    }

    private void synchronizeScheduledComTaskExecution(Instant when, int priority) {
        updateNextExecutionTimeStampAndPriority(when, priority);
    }

    private void applyNextExecutionTimestampAndPriority(Instant when, int priority, PostingMode postingMode) {
        this.setNextExecutionTimestamp(when);
        this.priority = priority;
        postingMode.executeOn(this);
    }

    @Override
    public ComChannel connect(ComPort comPort) throws ConnectionException {
        return this.connect(comPort, this.getProperties(), new TrustingConnectionTaskPropertyValidator());
    }

    @Override
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        return this.connect(comPort, properties, new MistrustingConnectionTaskPropertyValidator());
    }

    private ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties, ConnectionTaskPropertyValidator validator) throws ConnectionException {
        validator.validate(properties);
        ConnectionType connectionType = this.getConnectionType();
        List<ConnectionProperty> connectionProperties = this.toConnectionProperties(this.getProperties());
        connectionProperties.add(new ComPortNameProperty(comPort));
        return connectionType.connect(connectionProperties);
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        this.getConnectionType().disconnect(comChannel);
    }

    @Override
    public int getMaxNumberOfTries() {
        if (getConnectionStrategy().equals(ConnectionStrategy.AS_SOON_AS_POSSIBLE)) {
            return Integer.MAX_VALUE;
        } else {
            if (this.maxNumberOfTries == -1) {
                for (ComTaskExecution scheduledComTask : getScheduledComTasks()) {
                    if (this.maxNumberOfTries < scheduledComTask.getMaxNumberOfTries()) {
                        this.maxNumberOfTries = scheduledComTask.getMaxNumberOfTries();
                    }
                }
            }
            return this.maxNumberOfTries;
        }
    }

    @Override
    public void setMaxNumberOfTries(int maxNumberOfTries) {
        this.maxNumberOfTries = maxNumberOfTries;
    }

    @Override
    protected Class<PartialScheduledConnectionTask> getPartialConnectionTaskType() {
        return PartialScheduledConnectionTask.class;
    }

    @Override
    public List<ComTaskExecution> getScheduledComTasks() {
        return this.communicationTaskService.findComTaskExecutionsByConnectionTask(this).find();
    }

    private enum PostingMode {
        NOW {
            @Override
            void executeOn(ScheduledConnectionTaskImpl connectionTask) {
                connectionTask.update();
            }
        },

        LATER {
            @Override
            void executeOn(ScheduledConnectionTaskImpl connectionTask) {
                // Do not post now as the connection task will do it later
            }
        };

        abstract void executeOn(ScheduledConnectionTaskImpl connectionTask);
    }

    private class ComPortNameProperty implements ConnectionProperty {
        private ComPort comPort;

        private ComPortNameProperty(ComPort comPort) {
            super();
            this.comPort = comPort;
        }

        @Override
        public String getName() {
            return SerialConnectionPropertyNames.COMPORT_NAME_PROPERTY_NAME.propertyName();
        }

        @Override
        public Object getValue() {
            return this.comPort.getName();
        }
    }

    private interface UpdateStrategy {

        void prepare();

        void complete();

        UpdateStrategy connectionStrategyChanged(ConnectionStrategy oldConnectionStrategy, ConnectionStrategy newConnectionStrategy);

        NextExecutionSpecs getNextExecutionSpecs();

        UpdateStrategy createSchedule(TemporalExpression temporalExpression);

        UpdateStrategy schedulingChanged(TemporalExpression temporalExpression);

    }

    private abstract class DefaultStrategy implements UpdateStrategy {
        @Override
        public NextExecutionSpecs getNextExecutionSpecs() {
            return nextExecutionSpecs.orNull();
        }
    }

    /**
     * The No-Operation strategy that will be executed when
     * no attribute that requires a strategy was updated.
     */
    private class Noop extends DefaultStrategy {
        @Override
        public void prepare() {
            // No implementation required
        }

        @Override
        public void complete() {
            // No implementation required
        }

        @Override
        public UpdateStrategy connectionStrategyChanged(ConnectionStrategy oldConnectionStrategy, ConnectionStrategy newConnectionStrategy) {
            if (newConnectionStrategy.equals(oldConnectionStrategy) || oldConnectionStrategy==null) {
                return this;
            } else {
                return new StrategyChanged(oldConnectionStrategy);
            }
        }

        @Override
        public UpdateStrategy schedulingChanged(TemporalExpression temporalExpression) {
            return new Reschedule(temporalExpression);
        }

        @Override
        public UpdateStrategy createSchedule(TemporalExpression temporalExpression) {
            return new CreateSchedule(temporalExpression);
        }
    }

    /**
     * Represents the state where no {@link NextExecutionSpecs} existed
     * and new NextExecutionSpecs need to be created.
     */
    private class CreateSchedule extends DefaultStrategy {
        private final NextExecutionSpecs nextExecutionSpecs;

        protected CreateSchedule(TemporalExpression temporalExpression) {
            this.nextExecutionSpecs = schedulingService.newNextExecutionSpecs(temporalExpression);
        }

        @Override
        public void prepare() {
            this.nextExecutionSpecs.save();
            ScheduledConnectionTaskImpl.this.setNextExecutionSpecs(this.nextExecutionSpecs);
        }

        @Override
        public void complete() {
            // Nothing to complete for now
        }

        @Override
        public NextExecutionSpecs getNextExecutionSpecs() {
            return this.nextExecutionSpecs;
        }

        @Override
        public UpdateStrategy createSchedule(TemporalExpression temporalExpression) {
            return new CreateSchedule(temporalExpression);
        }

        @Override
        public UpdateStrategy schedulingChanged(TemporalExpression temporalExpression) {
            return this.createSchedule(temporalExpression);
        }

        @Override
        public UpdateStrategy connectionStrategyChanged(ConnectionStrategy oldConnectionStrategy, ConnectionStrategy newConnectionStrategy) {
            if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(oldConnectionStrategy)) {
                /* Switching to ASAP that does not use NextExecutionSpecs
                 * so forget about the NextExecutionSpecs that are under construction. */
                return new StrategyChanged(oldConnectionStrategy);
            } else {
                /* Switching from ASAP to MINIMIZE but still the NextExecutionSpecs were
                 * under construction while ASAP does not use NextExecutionSpecs.
                 * This must be a coding error. */
                throw new IllegalStateException("Should not have been possible to build NextExecutionSpecs in ASAP mode");
            }
        }
    }

    /**
     * Represents the state where we already had {@link NextExecutionSpecs}
     * but the {@link TemporalExpression} changed and we will therefore need to reschedule.
     */
    private class Reschedule extends DefaultStrategy {

        private Reschedule(TemporalExpression temporalExpression) {
            super();
            this.getNextExecutionSpecs().setTemporalExpression(temporalExpression);
        }

        @Override
        public void prepare() {
            this.getNextExecutionSpecs().save();
            doUpdateNextExecutionTimestamp(PostingMode.LATER);
        }

        @Override
        public void complete() {
            // Nothing to complete for now
        }

        @Override
        public UpdateStrategy createSchedule(TemporalExpression temporalExpression) {
            // Should not occur
            throw new IllegalStateException("Schedule already existed and was not expecting to have to create a new one");
        }

        @Override
        public UpdateStrategy schedulingChanged(TemporalExpression temporalExpression) {
            return new Reschedule(temporalExpression);
        }

        @Override
        public UpdateStrategy connectionStrategyChanged(ConnectionStrategy oldConnectionStrategy, ConnectionStrategy newConnectionStrategy) {
            /* Scheduling changed previously so old strategy must be MINIMIZE
             * and therefore we must be switching to ASAP but that does not
             * have scheduling so it suffices to switch the strategy
             * and that will delete the scheduling too. */
            return new StrategyChanged(oldConnectionStrategy);
        }
    }

    private class StrategyChanged extends DefaultStrategy {
        private final ConnectionStrategy oldConnectionStrategy;
        private NextExecutionSpecs obsolete;

        protected StrategyChanged(ConnectionStrategy oldConnectionStrategy) {
            super();
            this.oldConnectionStrategy = oldConnectionStrategy;
        }

        @Override
        public void prepare() {
            prepareStrategyChange(this.oldConnectionStrategy);
            if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(this.oldConnectionStrategy)) {
                // Old strategy is to minimize connections and therefore the new strategy must be as soon as possible
                this.obsolete = getNextExecutionSpecs();
                setNextExecutionSpecs(null);
            }
        }

        @Override
        public void complete() {
            if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(this.oldConnectionStrategy)) {
                // Old strategy is to minimize connections and therefore the new strategy must be as soon as possible
                this.obsolete.delete();
            }
        }

        @Override
        public UpdateStrategy connectionStrategyChanged(ConnectionStrategy oldConnectionStrategy, ConnectionStrategy newConnectionStrategy) {
            return new StrategyChanged(oldConnectionStrategy);
        }

        @Override
        public UpdateStrategy createSchedule(TemporalExpression temporalExpression) {
            return new CreateScheduleWithStrategyChange(temporalExpression, this.oldConnectionStrategy);
        }

        @Override
        public UpdateStrategy schedulingChanged(TemporalExpression temporalExpression) {
            /* The strategy either changes from ASAP to MINIMIZE
             * but since ASAP does not have a schedule, it cannot change.
             * The strategy can also change from MINIMIZE to ASAP
             * but setting the schedule for ASAP ignores the setters.
             * Therefore, this is an illegal state. */
            throw new IllegalStateException("Was not expecting the scheduling to change in combination with a strategy change");
        }
    }

    private class CreateScheduleWithStrategyChange extends CreateSchedule implements UpdateStrategy {
        private final ConnectionStrategy oldConnectionStrategy;
        private NextExecutionSpecs obsolete;

        protected CreateScheduleWithStrategyChange(TemporalExpression temporalExpression, ConnectionStrategy oldConnectionStrategy) {
            super(temporalExpression);
            this.oldConnectionStrategy = oldConnectionStrategy;
        }

        @Override
        public void prepare() {
            super.prepare();
            prepareStrategyChange(this.oldConnectionStrategy);
            if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(this.oldConnectionStrategy)) {
                // Old strategy is to minimize connections and therefore the new strategy must be as soon as possible
                this.obsolete = getNextExecutionSpecs();
                setNextExecutionSpecs(null);
            }
        }

        @Override
        public void complete() {
            super.complete();
            if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(this.oldConnectionStrategy)) {
                // Old strategy is to minimize connections and therefore the new strategy must be as soon as possible
                this.obsolete.delete();
            }
        }
    }

    public abstract static class AbstractScheduledConnectionTaskBuilder implements Device.ScheduledConnectionTaskBuilder {

        private final ScheduledConnectionTaskImpl scheduledConnectionTask;

        public AbstractScheduledConnectionTaskBuilder(ScheduledConnectionTaskImpl scheduledConnectionTask) {
            this.scheduledConnectionTask = scheduledConnectionTask;
        }

        protected ScheduledConnectionTaskImpl getScheduledConnectionTask() {
            return scheduledConnectionTask;
        }

        @Override
        public Device.ScheduledConnectionTaskBuilder setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus status) {
            this.scheduledConnectionTask.setStatus(status);
            return this;
        }
    }

}