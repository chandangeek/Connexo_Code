/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Range;
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
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.common.device.data.ConnectionInitiationTask;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.ConnectionProperty;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.scheduling.NextExecutionSpecs;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTaskProperty;
import com.energyict.mdc.common.tasks.TaskPriorityConstants;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.cps.SIMCardCustomPropertySet;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFields;
import com.energyict.mdc.device.data.tasks.EarliestNextExecutionTimeStampAndPriority;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.journal.ProtocolJournal;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;

import com.energyict.protocol.exceptions.ConnectionException;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Lists;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link ScheduledConnectionTask} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-16 (11:07)
 */
@ValidNextExecutionSpecsWithMinimizeConnectionsStrategy(groups = {Save.Create.class, Save.Update.class})
@XmlRootElement
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlAccessorType(XmlAccessType.NONE)
public class ScheduledConnectionTaskImpl extends OutboundConnectionTaskImpl<PartialScheduledConnectionTask> implements ScheduledConnectionTask, PersistenceAware {

    private SchedulingService schedulingService;
    private static final Logger LOGGER = Logger.getLogger(ScheduledConnectionTaskImpl.class.getName());
    private ComWindow comWindow;
    private Reference<NextExecutionSpecs> nextExecutionSpecs = ValueReference.absent();
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.OUTBOUND_CONNECTION_TASK_STRATEGY_REQUIRED + "}")
    private ConnectionStrategy connectionStrategy;
    private Instant nextExecutionTimestamp;
    private Instant plannedNextExecutionTimestamp;
    @SuppressWarnings("unused")
    private int priority;
    @Range(min = 1, max = 16, message = '{' + MessageSeeds.Keys.INVALID_NUMBER_OF_SIMULTANEOUS_CONNECTIONS + '}', groups = {Save.Create.class, Save.Update.class})
    private int numberOfSimultaneousConnections = 1;
    private Reference<ConnectionInitiationTask> initiationTask = ValueReference.absent();
    private int maxNumberOfTries = -1;
    private UpdateStrategy updateStrategy = new Noop();
    private boolean calledByComtaskExecution = false;
    private ServerCommunicationTaskService communicationTaskService;
    private CustomPropertySetService customPropertySetService;
    private boolean strategyChange = false;
    private TaskStatus taskStatus;
    private ProtocolJournal protocolJournal;

    protected ScheduledConnectionTaskImpl() {
        super();
    }

    @Inject
    protected ScheduledConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ServerConnectionTaskService connectionTaskService, ServerCommunicationTaskService communicationTaskService, ProtocolPluggableService protocolPluggableService, SchedulingService schedulingService, CustomPropertySetService customPropertySetService) {
        super(dataModel, eventService, thesaurus, clock, connectionTaskService, communicationTaskService, protocolPluggableService);
        this.schedulingService = schedulingService;
        this.communicationTaskService = communicationTaskService;
        this.customPropertySetService = customPropertySetService;
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

//    @Override
//    public void update() {
//        this.updateStrategy.prepare();
//        Save.UPDATE.save(this.getDataModel(), this, Save.Create.class, Save.Update.class);
//        this.updateStrategy.complete();
//        this.updateStrategy = new Noop();
//        this.getDataModel().touch(getDevice());
//    }

    @Override
    @XmlAttribute
    public NextExecutionSpecs getNextExecutionSpecs() {
        return this.updateStrategy.getNextExecutionSpecs();
    }

    @Override
    public void setNextExecutionSpecsFrom(TemporalExpression temporalExpression) {
        if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(this.getConnectionStrategy())) {
            this.updateStrategy = this.updateStrategy.createSchedule(temporalExpression);
            this.updateStrategy.prepare();
            this.nextExecutionTimestamp = this.calculateNextPlannedExecutionTimestamp();
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
        this.comWindow = comWindow == null || comWindow.isEmpty() ? null : comWindow;
    }

    @XmlAttribute
    public Boolean getStrategyChange() {
        return strategyChange;
    }

    @Override
    @XmlAttribute
    public ConnectionStrategy getConnectionStrategy() {
        return connectionStrategy;
    }

    @Override
    public void setConnectionStrategy(ConnectionStrategy newConnectionStrategy) {
        this.updateStrategy = this.updateStrategy.connectionStrategyChanged(this.connectionStrategy, newConnectionStrategy);
        this.connectionStrategy = newConnectionStrategy;
    }

    @Override
    public void notifyCreated() {
        super.notifyCreated();
        this.updateStrategy = new Noop();
    }

    private void prepareStrategyChange(ConnectionStrategy oldStrategy) {
        this.strategyChange = true;
        if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(oldStrategy)) {
            // Old strategy is to minimize connections and therefore the new strategy must be as soon as possible
            this.updateNextExecutionTimeStampBasedOnComTask();
        } else {
            // Old strategy is asap and therefore the new strategy is to minimize connections
            this.updateNextExecutionTimestamp(PostingMode.LATER);
        }
        this.strategyChange = false;
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

    private EarliestNextExecutionTimeStampAndPriority getEarliestNextExecutionTimeStampAndPriority() {
        Condition condition = where(ComTaskExecutionFields.CONNECTIONTASK.fieldName()).isEqualTo(this)
                .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull())
                .and(where(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()).isNotNull());

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
        this.notifyComTaskExecutionsThatConnectionMethodHasChanged(PostingMode.LATER);
    }

    private void notifyComTaskExecutionsThatConnectionMethodHasChanged(PostingMode postingMode) {
        LOGGER.info("Call ScheduledConnectionTaskImpl#notifyComTaskExecutionsThatConnectionMethodHasChanged ");
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
            if (!calledByComtaskExecution) {
                this.synchronizeScheduledComTaskExecution(this.getNextExecutionTimestamp(), priority);
            }
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
        Calendar calendar = Calendar.getInstance(getUTCTimeZone());
        calendar.setTimeInMillis(this.now().toEpochMilli());
        this.plannedNextExecutionTimestamp = this.applyComWindowIfAny(this.getNextExecutionSpecs().getNextTimestamp(calendar).toInstant());
        return this.schedule(this.plannedNextExecutionTimestamp, postingMode);
    }

    @Override
    public void scheduledComTaskRescheduled(ComTaskExecution comTask) {
        if (this.connectionStrategy.equals(ConnectionStrategy.MINIMIZE_CONNECTIONS)) {
            calledByComtaskExecution = true;
            if (comTask.getNextExecutionTimestamp() == null) {
                updateNextExecutionTimeStampBasedOnComTask();
            } else {
                this.schedule(comTask.getNextExecutionTimestamp().minusMillis(1));
            }
        } else {
            this.schedule(comTask.getNextExecutionTimestamp());
        }
        setExecutingComPort(null);
        update(ConnectionTaskFields.NEXT_EXECUTION_TIMESTAMP.fieldName(), ConnectionTaskFields.COM_PORT.fieldName());
    }

    @Override
    public void scheduledComTaskChangedPriority(ComTaskExecution comTask) {
        if (this.needToSynchronizePriorityChanges()) {
            doNotTouchParentDevice();
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
            ComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getUpdater();
            comTaskExecutionUpdater.calledByComTaskExecution();
            comTaskExecutionUpdater.forceNextExecutionTimeStampAndPriority(nextExecutionTimestamp, priority);
            comTaskExecutionUpdater.updateFields(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName(),
                    ComTaskExecutionFields.PLANNEDNEXTEXECUTIONTIMESTAMP.fieldName(),
                    ComTaskExecutionFields.EXECUTION_PRIORITY.fieldName());

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
    protected void doExecutionCompleted(List<String> updatedFields) {
        super.doExecutionCompleted(updatedFields);
        if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(getConnectionStrategy())) {
            this.schedule(this.calculateNextPlannedExecutionTimestamp(), PostingMode.NOW);
            updatedFields.add(ConnectionTaskFields.NEXT_EXECUTION_TIMESTAMP.fieldName());
            updatedFields.add(ConnectionTaskFields.PRIORITY.fieldName());
        } else {
            updateNextExecutionTimeStampBasedOnComTask();
        }
    }

    private Instant calculateNextPlannedExecutionTimestamp() {
        return this.applyComWindowIfAny(this.calculateNextExecutionTimestamp(this.now()));
    }

    public Instant applyComWindowIfAny(Instant calculatedNextExecutionTimestamp) {
        Calendar calendar = Calendar.getInstance(getUTCTimeZone());
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
        Calendar calendar = Calendar.getInstance(getUTCTimeZone());
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
            this.schedule(this.now());
        } else {
            updateNextExecutionTimeStampBasedOnComTask();
        }
    }

    @Override
    protected void doExecutionRescheduled() {
        super.doExecutionRescheduled();
        schedule(calculateNextRescheduleTimestamp());
    }

    private Instant calculateNextRescheduleTimestamp() {
        Instant failureDate = this.now();
        Calendar calendar = Calendar.getInstance(getUTCTimeZone());
        calendar.setTimeInMillis(failureDate.toEpochMilli());
        TimeDuration retryDelay = getRescheduleRetryDelay();
        retryDelay.addTo(calendar);
        applyComWindowIfAny(calendar);

        return calendar.getTime().toInstant();
    }

    private Instant calculateNextExecutionTimestamp(Instant now) {
        return this.calculateNextExecutionTimestampFromBaseline(now, this.getNextExecutionSpecs());
    }

    private Instant calculateNextExecutionTimestampFromBaseline(Instant baseLine, NextExecutionSpecs nextExecutionSpecs) {
        Calendar calendar = Calendar.getInstance(getUTCTimeZone());
        calendar.setTimeInMillis(baseLine.toEpochMilli());
        if (nextExecutionSpecs != null) {
            return nextExecutionSpecs.getNextTimestamp(calendar).toInstant();
        } else {
            return getPlannedNextExecutionTimestamp();
        }
    }

    @Override
    public Instant scheduleNow() {
        this.getScheduledComTasks().stream()
                .filter(comTaskExecution -> EnumSet.of(TaskStatus.Failed, TaskStatus.Retrying, TaskStatus.NeverCompleted, TaskStatus.Pending).contains(comTaskExecution.getStatus()))
                .filter(comTaskExecution -> !comTaskExecution.isObsolete())
                .sorted(Comparator.comparingLong(ComTaskExecution::getId))
                .map(comTaskExecution -> communicationTaskService.findAndLockComTaskExecutionById(comTaskExecution.getId()))
                .flatMap(Functions.asStream())
                .filter(comTaskExecution -> EnumSet.of(TaskStatus.Failed, TaskStatus.Retrying, TaskStatus.NeverCompleted, TaskStatus.Pending).contains(comTaskExecution.getStatus()))
                .filter(comTaskExecution -> !comTaskExecution.isObsolete())
                .forEach(ComTaskExecution::runNow);
        return scheduleConnectionNow();
    }

    /**
     * Updates the next execution of this ConnectionTask so that it will get picked up as soon as possible.
     * ComTaskExecutions linked to this connection will remain unaltered
     *
     * @return The timestamp on which this ScheduledConnectionTask is scheduled.
     */
    public Instant scheduleConnectionNow() {
        return this.schedule(this.now(), PostingMode.NOW);
    }

    public Instant schedule(Instant when) {
        if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(this.connectionStrategy)) {
            when = calculateNextExecutionTimestamp(when);
        }
        return this.schedule(when, PostingMode.NOW);
    }


    private Instant schedule(Instant when, PostingMode postingMode) {
        doNotTouchParentDevice();
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
            return this.schedule(when);
        }
    }

    private void triggerComTasks(Instant when) {
        getScheduledComTasks().stream()
                .filter(this::needsTriggering)
                .filter(Predicates.not(ComTaskExecution::isObsolete))
                .sorted(Comparator.comparingLong(ComTaskExecution::getId))
                .map(scheduledComTask -> communicationTaskService.findAndLockComTaskExecutionById(scheduledComTask.getId()))
                .flatMap(Functions.asStream())
                .filter(this::needsTriggering)
                .filter(Predicates.not(ComTaskExecution::isObsolete))
                .forEach(comTaskExecution -> comTaskExecution.schedule(when));
    }

    private boolean needsTriggering(ComTaskExecution scheduledComTask) {
        return EnumSet.complementOf(EnumSet.of(TaskStatus.Waiting, TaskStatus.OnHold, TaskStatus.Busy)).contains(scheduledComTask.getStatus());
    }

    @Override
    public Instant getNextExecutionTimestamp() {
        return this.nextExecutionTimestamp;
    }

    @Override
    @XmlAttribute
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
    @XmlAttribute
    public int getNumberOfSimultaneousConnections() {
        return numberOfSimultaneousConnections;
    }

    @Override
    public void setNumberOfSimultaneousConnections(int numberOfSimultaneousConnections) {
        this.numberOfSimultaneousConnections = numberOfSimultaneousConnections;
    }

    @Override
    @XmlAttribute
    public TaskStatus getTaskStatus() {
        if (this.now() != null) {
            taskStatus = ServerConnectionTaskStatus.getApplicableStatusFor(this, this.now());
        }
        return taskStatus;
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
        Integer highestPriority = TaskPriorityConstants.DEFAULT_PRIORITY;
        if (!this.strategyChange) {
            if (earliestNextExecutionTimeStampAndPriority == null || earliestNextExecutionTimeStampAndPriority.earliestNextExecutionTimestamp == null) {
                when = null;
            } else {
                highestPriority = earliestNextExecutionTimeStampAndPriority.priority;
                if (earliestNextExecutionTimeStampAndPriority.earliestNextExecutionTimestamp != null && when.isBefore(earliestNextExecutionTimeStampAndPriority.earliestNextExecutionTimestamp)) {
                    when = earliestNextExecutionTimeStampAndPriority.earliestNextExecutionTimestamp;
                }
                when = this.applyComWindowIfAny(when);
                if (!calledByComtaskExecution) {
                    this.synchronizeScheduledComTaskExecution(when, highestPriority);
                }
            }
        }
        this.applyNextExecutionTimestampAndPriority(when, highestPriority, postingMode);
        calledByComtaskExecution = false;
        return when;
    }

    private void synchronizeScheduledComTaskExecution(Instant when, int priority) {
        updateNextExecutionTimeStampAndPriority(when, priority);
    }

    private void applyNextExecutionTimestampAndPriority(Instant when, int priority, PostingMode postingMode) {
        this.nextExecutionTimestamp = when;
        boolean priorityChanged = this.priority != priority;
        this.priority = priority;
        postingMode.executeOn(this, priorityChanged);
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
        List<ConnectionTaskProperty> adaptedProperties = adaptToUPLValues(properties);
        validator.validate(adaptedProperties);
        ConnectionType connectionType = this.getConnectionType();
        List<ConnectionProperty> connectionProperties = this.castToConnectionProperties(adaptedProperties);
        connectionProperties.add(new ComPortNameProperty(comPort));
        connectionProperties.addAll(getDeviceIdentificationProperties());
        connectionProperties.addAll(getSIMcardProperties());
        connectionProperties.addAll(getConnectionProviderProperty());
        connectionType.setProtocolJournaling(protocolJournal);
        return connectionType.connect(connectionProperties);
    }

    private List<ConnectionTaskProperty> getConnectionProviderProperty() {
        List<ConnectionTaskProperty> connectionTaskPropertyList = new ArrayList<>();

        connectionTaskPropertyList.add(newProperty("connectionTask.id", this.getId(), Instant.now()));
        connectionTaskPropertyList.add(newProperty("connectionTask.name", this.getName(), Instant.now()));

        return connectionTaskPropertyList;
    }

    private List<ConnectionTaskProperty> getDeviceIdentificationProperties() {
        List<ConnectionTaskProperty> deviceIDs = new ArrayList<>();

        deviceIDs.add(newProperty("serialNumber", getDevice().getSerialNumber(), Instant.now()));
        deviceIDs.add(newProperty("mrID", getDevice().getmRID(), Instant.now()));

        return deviceIDs;
    }

    private List<ConnectionTaskProperty> getSIMcardProperties() {
        try {
            Optional<RegisteredCustomPropertySet> activeSet = getCustomPropertySetService().findActiveCustomPropertySet(SIMCardCustomPropertySet.CPS_ID);
            if (activeSet.isPresent()) {
                CustomPropertySet simCustomProperty = activeSet.get().getCustomPropertySet();
                CustomPropertySetValues simProps = getCustomPropertySetService().getUniqueValuesFor(simCustomProperty, getDevice(), Instant.now());
                return toConnectionProperties(simProps);
            }
        } catch (Exception ex) {
            System.err.println(ex);
        }
        return Collections.emptyList();
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        this.getConnectionType().disconnect(comChannel);
    }

    @Override
    @XmlAttribute
    public int getMaxNumberOfTries() {
        if (getConnectionStrategy().equals(ConnectionStrategy.AS_SOON_AS_POSSIBLE)) {
            int getMaxConnectionRetries = 0;
            if (this.getPartialConnectionTask() != null) {
                getMaxConnectionRetries = this.getPartialConnectionTask().getNumberOfRetriesConnectionMethod();
            }
            return getMaxConnectionRetries != 0 ? getMaxConnectionRetries : DEFAULT_MAX_NUMBER_OF_TRIES;
        } else {
            if (this.maxNumberOfTries == -1) {
                this.maxNumberOfTries =
                        this.getScheduledComTasks()
                                .stream()
                                .map(ComTaskExecution::getMaxNumberOfTries)
                                .min(Integer::compare)
                                .orElse(-1);
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
    @XmlTransient
    public List<ComTaskExecution> getScheduledComTasks() {
        return this.communicationTaskService.findComTaskExecutionsByConnectionTask(this).find();
    }

    public CustomPropertySetService getCustomPropertySetService() {
        return this.customPropertySetService;
    }

    @Override
    public void setProtocolJournaling(ProtocolJournal protocolJournal) {
        this.protocolJournal = protocolJournal;
        getConnectionType().setProtocolJournaling(protocolJournal);
    }

    @Override
    public void journal(String message) {
        this.protocolJournal.addToJournal(message);
    }

    private enum PostingMode {
        NOW {
            @Override
            void executeOn(ScheduledConnectionTaskImpl connectionTask, boolean priorityChanged) {
                connectionTask.updateStrategy.prepare();
                List<String> fields = Lists.newArrayList(ConnectionTaskFields.NEXT_EXECUTION_SPECS.fieldName(),
                        ConnectionTaskFields.PLANNED_NEXT_EXECUTION_TIMESTAMP.fieldName(),
                        ConnectionTaskFields.NEXT_EXECUTION_TIMESTAMP.fieldName());
                if (priorityChanged) {
                    fields.add(ConnectionTaskFields.PRIORITY.fieldName());
                }
                connectionTask.update(fields.toArray(new String[fields.size()]));
                connectionTask.notifyUpdated();
                connectionTask.updateStrategy.complete();
            }
        },

        LATER {
            @Override
            void executeOn(ScheduledConnectionTaskImpl connectionTask, boolean priorityChanged) {
                // Do not post now as the connection task will do it later
            }
        };

        abstract void executeOn(ScheduledConnectionTaskImpl connectionTask, boolean priorityChanged);
    }

    private class ComPortNameProperty implements ConnectionProperty {
        private ComPort comPort;

        private ComPortNameProperty(ComPort comPort) {
            super();
            this.comPort = comPort;
        }

        @Override
        public String getName() {
            return com.energyict.mdc.upl.io.ConnectionType.Property.COMP_PORT_NAME.getName();
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
            if (newConnectionStrategy.equals(oldConnectionStrategy) || oldConnectionStrategy == null) {
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

        private boolean alreadyRecalculated = false;

        private Reschedule(TemporalExpression temporalExpression) {
            super();
            this.getNextExecutionSpecs().setTemporalExpression(temporalExpression);
        }

        @Override
        public void prepare() {
            if (!alreadyRecalculated) {
                this.getNextExecutionSpecs().update();
                doUpdateNextExecutionTimestamp(PostingMode.LATER);
                alreadyRecalculated = true;
            }
        }

        @Override
        public void complete() {
            alreadyRecalculated = false;
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
        public Device.ScheduledConnectionTaskBuilder setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties properties) {
            scheduledConnectionTask.setProtocolDialectConfigurationProperties(properties);
            return this;
        }

        @Override
        public Device.ScheduledConnectionTaskBuilder setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus status) {
            this.scheduledConnectionTask.setStatus(status);
            return this;
        }
    }

}
