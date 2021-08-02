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
import com.elster.jupiter.orm.impl.DataModelImpl;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.InboundConnectionTask;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.common.scheduling.NextExecutionSpecs;
import com.energyict.mdc.common.tasks.BasicCheckTask;
import com.energyict.mdc.common.tasks.ClockTask;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.common.tasks.ComTaskExecutionTrigger;
import com.energyict.mdc.common.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.LoadProfilesTask;
import com.energyict.mdc.common.tasks.LogBooksTask;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.common.tasks.RegistersTask;
import com.energyict.mdc.common.tasks.ServerComTaskExecution;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.common.tasks.TaskPriorityConstants;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.common.tasks.TopologyTask;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.device.data.exceptions.CannotUpdateObsoleteComTaskExecutionException;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsAlreadyObsoleteException;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException;
import com.energyict.mdc.device.data.impl.CreateEventType;
import com.energyict.mdc.device.data.impl.DeleteEventType;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.EventType;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.PersistentIdObject;
import com.energyict.mdc.device.data.impl.TaskStatusTranslationKeys;
import com.energyict.mdc.device.data.impl.UpdateEventType;
import com.energyict.mdc.device.data.impl.constraintvalidators.ComTasksMustBeEnabledByDeviceConfiguration;
import com.energyict.mdc.device.data.impl.constraintvalidators.ManuallyScheduledNextExecSpecRequired;
import com.energyict.mdc.device.data.impl.constraintvalidators.SaveScheduled;
import com.energyict.mdc.device.data.impl.constraintvalidators.SharedScheduleComScheduleRequired;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.impl.NextExecutionSpecsImpl;
import com.energyict.mdc.tasks.impl.ComTaskDefinedByUserImpl;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Logger;

@ComTasksMustBeEnabledByDeviceConfiguration(groups = {Save.Create.class})
@ManuallyScheduledNextExecSpecRequired(groups = {SaveScheduled.class})
@SharedScheduleComScheduleRequired(groups = {Save.Create.class, Save.Update.class})
@ComTaskMustBeFirmwareManagement(groups = {Save.Create.class, Save.Update.class})
@ConsumableConnectionFunctionMustBeSupportedByTheDeviceProtocol(groups = {Save.Create.class, Save.Update.class})
public class ComTaskExecutionImpl extends PersistentIdObject<ComTaskExecution> implements ServerComTaskExecution {

    private static final Logger LOGGER = Logger.getLogger(ComTaskExecutionImpl.class.getName());

    public enum ComTaskExecType {
        SHARED_SCHEDULE_COM_TASK_EXECUTION_DISCRIMINATOR,
        MANUALLY_SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR,
        FIRMWARE_COM_TASK_EXECUTION_DISCRIMINATOR
    }

    private Clock clock;

    private CommunicationTaskService communicationTaskService;
    private ConnectionTaskService connectionTaskService;
    private SchedulingService schedulingService;
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
    private boolean isTracing;
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

    protected long connectionFunctionId;
    private Optional<ConnectionFunction> connectionFunction = Optional.empty();

    private boolean obsolete;
    private boolean executing;
    private boolean scheduledManually;
    private String statusDisplayName;
    private int maxNumberOfTries;
    private TaskStatus taskStatus;
    private int currentTryCount;
    private boolean usingComTaskExecutionTriggers = true;

    public ComTaskExecutionImpl() {
        super();
    }

    @Inject
    public ComTaskExecutionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, CommunicationTaskService communicationTaskService, SchedulingService schedulingService,
                                ConnectionTaskService connectionTaskService) {
        super(ComTaskExecution.class, dataModel, eventService, thesaurus);
        this.clock = clock;
        this.communicationTaskService = communicationTaskService;
        this.schedulingService = schedulingService;
        this.usingComTaskExecutionTriggers = shouldUseComTaskExecutionTriggers(dataModel);
        this.connectionTaskService = connectionTaskService;
    }

    private boolean shouldUseComTaskExecutionTriggers(DataModel dataModel) {
        return Optional.ofNullable(dataModel).filter(dm -> dm instanceof DataModelImpl)
                .map(DataModelImpl.class::cast).map(DataModelImpl::getOrmService)
                .map(ormService -> ormService.getProperty("com.elster.jupiter.comtaskexecution.useTriggers"))
                .filter(value -> value.trim().length() > 0)
                .map(Boolean::valueOf)
                .orElse(true);
    }

    @Override
    public boolean isUsingComTaskExecutionTriggers() {
        return usingComTaskExecutionTriggers;
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
        if (comTaskEnablement.usesDefaultConnectionTask()) {
            this.setUseDefaultConnectionTask(comTaskEnablement.usesDefaultConnectionTask());
        } else if (comTaskEnablement.getConnectionFunction().isPresent()) {
            this.setConnectionFunction(comTaskEnablement.getConnectionFunction().get());
        } else {
            this.doSetUseDefaultConnectionTask(false);
            this.doSetConnectionFunction(null);
            setConnectionTaskIfExists(device, comTaskEnablement);
        }
        this.isTracing = false;
    }

    @JsonIgnore
    @XmlTransient
    public Behavior getBehavior() {
        if (this.behavior == null) {
            if (comTaskExecType != null) {
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
        }
        return behavior;
    }

    void initializeDevice(Device device) {
        this.device.set(device);
    }

    @Override
    public boolean usesSharedSchedule() {
        if (this.getBehavior() != null) {
            return this.getBehavior().usesSharedSchedule();
        }
        return false;
    }

    @Override
    @XmlAttribute
    public boolean isScheduledManually() {
        if (this.getBehavior() != null) {
            scheduledManually = this.getBehavior().isScheduledManually();
        }
        return scheduledManually;
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public boolean isAdHoc() {
        return this.getBehavior().isAdHoc();
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public boolean isFirmware() {
        if (this.getBehavior() != null) {
            return this.getBehavior().isFirmware();
        }
        return false;
    }

    @Override
    @XmlElement(type = DeviceImpl.class, name = "device")
    public Device getDevice() {
        return this.device.get();   // we do an explicit get because Device is required and should not be null
    }

    @Override
    public void setDevice(Device device) {
        this.device.set(device);
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public ComPort getExecutingComPort() {
        return comPort.orNull();
    }

    @Override
    public boolean isExecuting() {
        return comPort.isPresent()
                ||
                (connectionTask.isPresent() && connectionTask.getOptional().isPresent()
                        && (connectionTask.get().getExecutingComPort() != null)
                        && comTaskStartedAfterConnectionStarted()
                        && ((getNextExecutionTimestamp() != null
                        && getNextExecutionTimestamp().isBefore(clock.instant())
                        && connectionTask.get().getLastCommunicationStart().isAfter(getNextExecutionTimestamp()))
                        || (getNextExecutionTimestamp() == null && isIgnoreNextExecutionSpecsForInbound()
                        && connectionTask.get() instanceof InboundConnectionTask))
                );
    }

    private boolean comTaskStartedAfterConnectionStarted() {
        return getLastExecutionStartTimestamp() != null
                && getConnectionTask().get().getLastCommunicationStart() != null
                && getConnectionTask().get().getLastCommunicationStart().isBefore(getLastExecutionStartTimestamp());
    }

    @Override
    @XmlAttribute
    public int getExecutionPriority() {
        return this.executionPriority;
    }

    void setExecutingPriority(int executingPriority) {
        this.executionPriority = executingPriority;
    }

    @Override
    @XmlAttribute
    public TaskStatus getStatus() {
        if (this.now() != null) {
            taskStatus = ServerComTaskStatus.getApplicableStatusFor(this, this.now());
        }
        return taskStatus;
    }

    public void setStatus(TaskStatus status) {
        this.taskStatus = status;
    }

    @Override
    @XmlAttribute
    public String getStatusDisplayName() {
        if (getStatus() != null && getThesaurus() != null) {
            statusDisplayName = TaskStatusTranslationKeys.translationFor(getStatus(), getThesaurus());
        }
        return statusDisplayName;
    }

    @Override
    @XmlAttribute
    public Instant getNextExecutionTimestamp() {
        return this.nextExecutionTimestamp;
    }

    @Override
    @XmlAttribute
    public int getMaxNumberOfTries() {
        maxNumberOfTries = this.device.get().getDeviceConfiguration().getComTaskEnablementFor(this.getComTask()).get().getMaxNumberOfTries();
        return maxNumberOfTries;
    }

    @Override
    @XmlAttribute
    public int getCurrentTryCount() {
        currentTryCount = this.getCurrentRetryCount() + 1;
        return currentTryCount;
    }

    private int getCurrentRetryCount() {
        return this.currentRetryCount;
    }

    @Override
    public boolean usesDefaultConnectionTask() {
        return this.useDefaultConnectionTask;
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public Optional<ConnectionFunction> getConnectionFunction() {
        if (!this.connectionFunction.isPresent() && this.connectionFunctionId != 0) {
            Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = getDevice().getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass();
            List<ConnectionFunction> supportedConnectionFunctions = deviceProtocolPluggableClass.isPresent()
                    ? deviceProtocolPluggableClass.get().getConsumableConnectionFunctions()
                    : Collections.emptyList();
            this.connectionFunction = supportedConnectionFunctions.stream().filter(cf -> cf.getId() == this.connectionFunctionId).findFirst();
        }
        return this.connectionFunction;
    }

    @Override
    public void save() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update() {
        LOGGER.info("CXO-11731: UPDATE EXECUTION TASK = " + this.toString());
        Save.UPDATE.save(getDataModel(), this, Save.Create.class, Save.Update.class);
        LOGGER.info("CXO-11731: Updated.");
        this.notifyUpdated();
    }

    @Override
    public String toString() {
        return " DEVICE=" + (device.isPresent() ? device.get().getId() : "NO_DEVICE") +
                " COMTASK=" + (comTask.isPresent() ? comTask.get().getId() : "NO_COMTASK") +
                " COMSCHEDULE =" + (comSchedule.isPresent() ? comSchedule.get().getId() : "NO_COMSCHEDULE ") +
                " NEXTEXECUTIONSPECS =" + (nextExecutionSpecs.isPresent() ? nextExecutionSpecs.get().getId() : "NO_NEXTEXECUTIONSPECS") +
                "LASTEXECUTIONTIMESTAMP =" + lastExecutionTimestamp +
                "NEXTEXECUTIONTIMESTAMP = " + nextExecutionTimestamp +
                "COMPORT =" + (comPort.isPresent() ? comPort.get().getId() : "NO_COMPORT") +
                "OBSOLETE_DATE = " + obsoleteDate +
                "PRIORITY = " + executionPriority +
                "USEDEFAULTCONNECTIONTASK =  " + useDefaultConnectionTask +
                "CURRENTRETRYCOUNT = " + currentRetryCount +
                "PLANNEDNEXTEXECUTIONTIMESTAMP = " + plannedNextExecutionTimestamp +
                "EXECUTIONPRIORITY = " + executionPriority +
                "EXECUTIONSTART = " + executionStart +
                "LASTSUCCESSFULCOMPLETION =" + lastSuccessfulCompletionTimestamp +
                "LASTEXECUTIONFAILED = " + lastExecutionFailed +
                "ONHOLD =  " + onHold +
                "CONNECTIONTASK = " + connectionTask +
                "IGNORENEXTEXECSPECS = " + ignoreNextExecutionSpecsForInbound +
                "CONNECTIONFUNCTION = " + (connectionFunction.isPresent() ? connectionFunction.get().getId() : "NO_CONNECTIONFUNCTION") +
                "LASTSESSION =  " + (lastSession.isPresent() ? lastSession.get().getId() : "NO_LASTSESSION") +
                "LASTSESS_HIGHESTPRIOCOMPLCODE = " + lastSessionHighestPriorityCompletionCode +
                "LASTSESS_SUCCESSINDICATOR = " + lastSessionSuccessIndicator +
                "MODTIME= " + modTime +
                "USERNAME=" + userName +
                "VERSION=" + version;
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
            this.doSetConnectionFunction(null);
        }
    }

    private void doSetUseDefaultConnectionTask(boolean useDefaultConnectionTask) {
        this.useDefaultConnectionTask = useDefaultConnectionTask;
    }

    @Override
    public void setConnectionFunction(ConnectionFunction connectionFunction) {
        doSetConnectionFunction(connectionFunction);
        this.setConnectionTask(null);
        this.doSetUseDefaultConnectionTask(connectionFunction == null);
    }

    private void doSetConnectionFunction(ConnectionFunction connectionFunction) {
        this.connectionFunction = Optional.ofNullable(connectionFunction);
        this.connectionFunctionId = connectionFunction != null ? connectionFunction.getId() : 0;
    }

    void setDefaultConnectionTask(ConnectionTask<?, ?> defaultConnectionTask) {
        this.useDefaultConnectionTask = true;
        setConnectionTask(defaultConnectionTask);
    }

    private void setConnectionTaskBasedOnConnectionFunction(ConnectionTask<?, ?> connectionTask) {
        doSetConnectionFunction(connectionTask.getPartialConnectionTask().getConnectionFunction().get());
        setConnectionTask(connectionTask);
    }

    @Override
    @XmlAttribute
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
        if (isObsolete()) {
            throw new ComTaskExecutionIsAlreadyObsoleteException(this, getThesaurus(), MessageSeeds.COM_TASK_EXECUTION_IS_ALREADY_OBSOLETE);
        } else if (comPort.isPresent()) {
            throw new ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException(this, getExecutingComPort(), getThesaurus(), MessageSeeds.COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE);
        }
        if (useDefaultConnectionTask || connectionFunctionId != 0) {
            postEvent(EventType.COMTASKEXECUTION_VALIDATE_OBSOLETE);
        } else if (connectionTask.isPresent() && connectionTask.get().getExecutingComPort() != null) {
            throw new ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException(this, connectionTask.get()
                    .getExecutingComPort(), getThesaurus(), MessageSeeds.COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE);
        }
    }

    @Override
    @XmlAttribute
    public boolean isObsolete() {
        obsolete = this.obsoleteDate != null;
        return obsolete;
    }

    @Override
    @XmlAttribute
    public Instant getObsoleteDate() {
        return obsoleteDate;
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public Optional<ConnectionTask<?, ?>> getConnectionTask() {
        return connectionTask.getOptional();
    }

    void setConnectionTask(ConnectionTask<?, ?> connectionTask) {
        this.connectionTask.set(connectionTask);
        if (connectionTask != null) {
            connectionTaskId = connectionTask.getId();
        } else {
            connectionTaskId = 0;
        }
    }

    @Override
    public boolean usesSameConnectionTaskAs(ComTaskExecution anotherTask) {
        if (anotherTask instanceof ComTaskExecutionImpl) {
            ComTaskExecutionImpl comTaskExecution = (ComTaskExecutionImpl) anotherTask;
            return connectionTaskId == comTaskExecution.connectionTaskId;
        } else {
            if (anotherTask.getConnectionTask().isPresent()) {
                return connectionTaskId == anotherTask.getConnectionTask().get().getId();
            } else {
                return false;
            }
        }
    }

    @Override
    @XmlAttribute
    public Instant getLastExecutionStartTimestamp() {
        return lastExecutionTimestamp;
    }

    @Override
    public void sessionCreated(ComTaskExecutionSession session) {
        if (lastSession.isPresent()) {
            if (session.endsAfter(lastSession.get())) {
                setLastSessionAndUpdate(session);
            }
        } else {
            setLastSessionAndUpdate(session);
        }
    }

    private void setLastSessionAndUpdate(ComTaskExecutionSession session) {
        setLastSession(session);
        getDataModel()
                .update(this,
                        ComTaskExecutionFields.LAST_SESSION.fieldName(),
                        ComTaskExecutionFields.LAST_SESSION_HIGHEST_PRIORITY_COMPLETION_CODE.fieldName(),
                        ComTaskExecutionFields.LAST_SESSION_SUCCESSINDICATOR.fieldName());
    }

    private void setLastSession(ComTaskExecutionSession session) {
        lastSession.set(session);
        lastSessionHighestPriorityCompletionCode = session.getHighestPriorityCompletionCode();
        lastSessionSuccessIndicator = session.getSuccessIndicator();
    }

    @Override
    @XmlTransient
    public Optional<ComTaskExecutionSession> getLastSession() {
        Optional<ComTaskExecutionSession> optional = lastSession.getOptional();
        if (optional.isPresent()) {
            return java.util.Optional.of(optional.get());
        } else {
            return java.util.Optional.empty();
        }
    }

    @Override
    @XmlAttribute
    public Instant getLastSuccessfulCompletionTimestamp() {
        return lastSuccessfulCompletionTimestamp;
    }

    @Override
    @XmlElement(type = NextExecutionSpecsImpl.class)
    public Optional<NextExecutionSpecs> getNextExecutionSpecs() {
        if (this.getBehavior() != null) {
            if (this.getBehavior().getNextExecutionSpecs().isPresent()) {
                nextExecutionSpecs.set(this.getBehavior().getNextExecutionSpecs().get());
            }
        }
        return nextExecutionSpecs.getOptional();
    }

    public void setNextExecutionSpecs(NextExecutionSpecs nextExecutionSpecs) {
        this.nextExecutionSpecs.set(nextExecutionSpecs);
    }

    @Override
    @XmlAttribute
    public boolean isIgnoreNextExecutionSpecsForInbound() {
        return ignoreNextExecutionSpecsForInbound;
    }

    void setIgnoreNextExecutionSpecsForInbound(boolean ignoreNextExecutionSpecsForInbound) {
        this.ignoreNextExecutionSpecsForInbound = ignoreNextExecutionSpecsForInbound;
    }

    @Override
    @XmlAttribute
    public Instant getPlannedNextExecutionTimestamp() {
        return plannedNextExecutionTimestamp;
    }

    @Override
    @XmlAttribute
    public int getPlannedPriority() {
        return plannedPriority;
    }

    void setPlannedPriority(int plannedPriority) {
        this.plannedPriority = plannedPriority;
    }

    @Override
    public boolean isTracing() {
        return this.isTracing;
    }

    void setIsTracing(boolean isTracing){
        this.isTracing = isTracing;
    }

    @Override
    public void updateNextExecutionTimestamp() {
        recalculateNextAndPlannedExecutionTimestamp();
        updateForScheduling(true);
    }

    void recalculateNextAndPlannedExecutionTimestamp() {
        Instant plannedNextExecutionTimestamp = this.getComSchedule()
                .flatMap(ComSchedule::getPlannedDate)
                .orElse(this.calculateNextExecutionTimestamp(clock.instant()));
        schedule(plannedNextExecutionTimestamp, plannedNextExecutionTimestamp);
    }

    protected Instant calculateNextExecutionTimestamp(Instant now) {
        if (isAdHoc()) {
            if (getLastExecutionStartTimestamp() != null
                    && getNextExecutionTimestamp() != null
                    && getLastExecutionStartTimestamp().isAfter(getNextExecutionTimestamp())) {
                return null;
            } else {
                return getNextExecutionTimestamp();
            }
        } else {
            return calculateNextExecutionTimestampFromBaseline(now);
        }
    }

    private Instant calculateNextExecutionTimestampFromBaseline(Instant baseLine) {
        NextExecutionSpecs nextExecutionSpecs = getNextExecutionSpecs().get();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(clock.getZone()));
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

    private void removeTrigger() {
        comTaskExecutionTriggers.stream()
                .filter(trigger -> trigger.getTriggerTimeStamp().equals(getNextExecutionTimestamp()))
                .findFirst()
                .ifPresent(
                        comTaskExecutionTrigger -> {
                            comTaskExecutionTriggers.remove(comTaskExecutionTrigger);
                            getDataModel().touch(this);
                        }

                );
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
        setExecutingComPort(null);
        setExecutionStartedTimestamp(null);

        nextExecutionTimestamp = applyCommunicationTriggersTo(Optional.ofNullable(nextExecutionTimestamp));
        if (nextExecutionTimestamp != null) { // nextExecutionTimestamp is null when putting on hold
            nextExecutionTimestamp = defineNextExecutionTimeStamp(nextExecutionTimestamp);
        }
        setPlannedNextExecutionTimestamp(plannedNextExecutionTimestamp);
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
        Optional<ComTaskExecutionTrigger> earliestComTaskExecutionTrigger = Optional.empty();
        if (usingComTaskExecutionTriggers) {
            earliestComTaskExecutionTrigger = getComTaskExecutionTriggers().stream()
                    .filter(comTaskExecutionTrigger -> getLastExecutionStartTimestamp() == null || comTaskExecutionTrigger.getTriggerTimeStamp().isAfter(getLastExecutionStartTimestamp()))
                    .sorted((e1, e2) -> e1.getTriggerTimeStamp().compareTo(e2.getTriggerTimeStamp())).findFirst();
        }
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
    @XmlAttribute
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
    public boolean shouldExecuteWithPriority() {
        return communicationTaskService.shouldExecuteWithPriority(this);
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public ComTaskExecutionUpdater getUpdater() {
        return new ComTaskExecutionUpdaterImpl(this);
    }

    @Override
    @JsonIgnore
    @XmlTransient
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
        LOGGER.info("CXO-11731: UPDATE FOR RESCHEDULING EXECUTION TASK = " + this.toString());
        this.update(ComTaskExecutionFields.COMPORT.fieldName(),
                ComTaskExecutionFields.LASTSUCCESSFULCOMPLETIONTIMESTAMP.fieldName(),
                ComTaskExecutionFields.LASTEXECUTIONFAILED.fieldName(),
                ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName(),
                ComTaskExecutionFields.CURRENTRETRYCOUNT.fieldName(),
                ComTaskExecutionFields.EXECUTIONSTART.fieldName(),
                ComTaskExecutionFields.ONHOLD.fieldName(),
                ComTaskExecutionFields.PLANNEDNEXTEXECUTIONTIMESTAMP.fieldName());
        if (informConnectionTask) {
            this.getConnectionTask().ifPresent(ct -> {
                if (!calledByConnectionTask) {
                    ((ServerConnectionTask<?, ?>) ct).scheduledComTaskRescheduled(this);
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
        markSuccessfullyCompleted();
        Instant rescheduleDate = calculateNextExecutionTimestamp(clock.instant());
        doReschedule(rescheduleDate);
        LOGGER.info("[comtaskexec] executionCompleted for " + getDevice().getName() + "; reschedule for " + rescheduleDate);
        updateForScheduling(true);
        getBehavior().comTaskCompleted();
        this.postEvent(EventType.COMTASKEXECUTION_COMPLETION);
    }

    @Override
    public void executionRescheduled(Instant rescheduleDate) {
        currentRetryCount++;    // increment the current number of retries
        if (currentRetryCount < getMaxNumberOfTries()) {
            LOGGER.info("[comtaskexec] executionRescheduled for " + getDevice().getName() +
                    "; currentRetryCount=" + currentRetryCount + "; reschedule for " + rescheduleDate);
            doReschedule(rescheduleDate);
        } else {
            doExecutionFailed();
        }
        updateForScheduling(true);
    }

    public void executionRescheduledToComWindow(Instant comWindowStartDate) {
        doReschedule(comWindowStartDate);
        updateForScheduling(true);
    }

    /**
     * Marks this ComTaskExecution as successfully completed.
     */
    private void markSuccessfullyCompleted() {
        this.lastSuccessfulCompletionTimestamp = clock.instant();
        this.resetCurrentRetryCount();
        removeTrigger();
    }

    private void doReschedule(Instant nextExecutionTimestamp) {
        doReschedule(nextExecutionTimestamp, nextExecutionTimestamp);
    }

    @Override
    public void executionFailed() {
        this.currentRetryCount++;
        if (this.currentRetryCount < getMaxNumberOfTries()) {
            this.doExecutionAttemptFailed();
        } else {
            this.doExecutionFailed();
        }
        updateForScheduling(true);
    }

    @Override
    public void executionFailed(boolean noRetry) {
        if (noRetry) {
            this.currentRetryCount++;
            this.doExecutionFailed();
            updateForScheduling(true);
        } else {
            executionFailed();
        }
    }

    protected void doExecutionAttemptFailed() {
        this.lastExecutionFailed = true;
        Instant rescheduleDate = calculateNextExecutionTimestampAfterFailure();
        LOGGER.info("[comtaskexec] doExecutionFailed for " + getDevice().getName() + "; rescheduled for " + rescheduleDate);
        this.doReschedule(rescheduleDate);
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

    void doExecutionFailed() {
        this.lastExecutionFailed = true;
        removeTrigger();
        this.resetCurrentRetryCount();
        if (isAdHoc()) {
            LOGGER.info("[comtaskexec] doExecutionFailed for " + getDevice().getName() + "; ad-hoc task, no reschedule date ");
            this.doReschedule(null, null);
        } else {
            Instant rescheduleDate = calculateNextExecutionTimestamp(clock.instant());
            LOGGER.info("[comtaskexec] doExecutionFailed for " + getDevice().getName() + "; rescheduled for " + rescheduleDate);
            this.doReschedule(rescheduleDate);
        }
        getBehavior().comTaskFailed();
    }

    @Override
    @XmlAttribute
    public boolean isLastExecutionFailed() {
        return this.lastExecutionFailed;
    }

    @Override
    @XmlAttribute
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
        getBehavior().comTaskStarted();
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
    public void injectConnectionTask(OutboundConnectionTask connectionTask) {
        if (!this.connectionTask.isPresent()) {
            if (connectionTask.getId() != connectionTaskId) {
                String msg = "The injected " + OutboundConnectionTask.class.getSimpleName() + " is not the task that is configured on this " + ComTaskExecution.class.getSimpleName()
                        + ". Expected " + connectionTaskId + " but got " + connectionTask.getId();
                throw new IllegalArgumentException(msg);
            }
            this.connectionTask.set(connectionTask);
        }
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public List<ComTaskExecutionTrigger> getComTaskExecutionTriggers() {
        return comTaskExecutionTriggers;
    }

    @Override
    public void addNewComTaskExecutionTrigger(Instant triggerTimeStamp) {
        if (usingComTaskExecutionTriggers && !getComTaskExecutionTriggers().stream().anyMatch(trigger -> trigger.getTriggerTimeStamp().getEpochSecond() == triggerTimeStamp.getEpochSecond())) {
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
        if (clock != null) {
            return clock.instant();
        }
        return null;
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
    public long getConnectionFunctionId() {
        return connectionFunctionId;
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public List<ComTask> getComTasks() {
        return Collections.singletonList(getComTask());
    }

    public void setComTasks(List<ComTask> ignore) {

    }

    @Override
    @XmlElement(type = ComTaskDefinedByUserImpl.class, name = "comTask")
    public ComTask getComTask() {
        return this.comTask.get();
    }

    @Override
    public void setComTask(ComTask comTask) {
        this.comTask.set(comTask);
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public Optional<ComSchedule> getComSchedule() {
        return this.comSchedule.getOptional();
    }

    public void setComSchedule(ComSchedule comSchedule) {
        this.comSchedule.set(comSchedule);
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public boolean isConfiguredToCollectRegisterData() {
        return isConfiguredToCollectDataOfClass(RegistersTask.class);
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public boolean isConfiguredToCollectLoadProfileData() {
        return isConfiguredToCollectDataOfClass(LoadProfilesTask.class);
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public boolean isConfiguredToRunBasicChecks() {
        return isConfiguredToCollectDataOfClass(BasicCheckTask.class);
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public boolean isConfiguredToCheckClock() {
        return isConfiguredToCollectDataOfClass(ClockTask.class);
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public boolean isConfiguredToCollectEvents() {
        return isConfiguredToCollectDataOfClass(LogBooksTask.class);
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public boolean isConfiguredToSendMessages() {
        return isConfiguredToCollectDataOfClass(MessagesTask.class);
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public boolean isConfiguredToReadStatusInformation() {
        return isConfiguredToCollectDataOfClass(StatusInformationTask.class);
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public boolean isConfiguredToUpdateTopology() {
        return isConfiguredToCollectDataOfClass(TopologyTask.class);
    }

    private <T extends ProtocolTask> boolean isConfiguredToCollectDataOfClass(Class<T> protocolTaskClass) {
        for (ProtocolTask protocolTask : this.getProtocolTasks()) {
            if (protocolTaskClass.isAssignableFrom(protocolTask.getClass())) {
                return true;
            }
        }
        return false;
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
         * {@link ComSchedule}
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

        void comTaskStarted();

        void comTaskCompleted();

        void comTaskFailed();
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
        public Optional<NextExecutionSpecs> getNextExecutionSpecs() {
            return Optional.empty();
        }

        @Override
        public boolean isFirmware() {
            return true;
        }

        @Override
        public void comTaskStarted() {
            postEvent(EventType.FIRMWARE_COMTASKEXECUTION_STARTED);
        }

        @Override
        public void comTaskCompleted() {
            postEvent(EventType.FIRMWARE_COMTASKEXECUTION_COMPLETED);
        }

        @Override
        public void comTaskFailed() {
            postEvent(EventType.FIRMWARE_COMTASKEXECUTION_FAILED);
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
        public Optional<NextExecutionSpecs> getNextExecutionSpecs() {
            return ComTaskExecutionImpl.this.nextExecutionSpecs.getOptional();

        }

        @Override
        public boolean isFirmware() {
            return false;
        }

        @Override
        public void comTaskStarted() {
            postEvent(EventType.MANUAL_COMTASKEXECUTION_STARTED);
        }

        @Override
        public void comTaskCompleted() {
            postEvent(EventType.MANUAL_COMTASKEXECUTION_COMPLETED);
        }

        @Override
        public void comTaskFailed() {
            postEvent(EventType.MANUAL_COMTASKEXECUTION_FAILED);
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
        public Optional<NextExecutionSpecs> getNextExecutionSpecs() {
            return Optional.of(getComSchedule().get().getNextExecutionSpecs());
        }

        @Override
        public boolean isFirmware() {
            return false;
        }

        @Override
        public void comTaskStarted() {
            postEvent(EventType.SCHEDULED_COMTASKEXECUTION_STARTED);
        }

        @Override
        public void comTaskCompleted() {
            postEvent(EventType.SCHEDULED_COMTASKEXECUTION_COMPLETED);
        }

        @Override
        public void comTaskFailed() {
            postEvent(EventType.SCHEDULED_COMTASKEXECUTION_FAILED);
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
        public AbstractComTaskExecutionBuilder useDefaultConnectionTask(boolean useDefaultConnectionTask) {
            this.comTaskExecution.setUseDefaultConnectionTask(useDefaultConnectionTask);
            return this;
        }

        @Override
        public AbstractComTaskExecutionBuilder setConnectionFunction(ConnectionFunction connectionFunction) {
            this.comTaskExecution.setConnectionFunction(connectionFunction);
            return this;
        }

        @Override
        public AbstractComTaskExecutionBuilder connectionTask(ConnectionTask<?, ?> connectionTask) {
            this.comTaskExecution.setConnectionTask(connectionTask);
            this.comTaskExecution.setUseDefaultConnectionTask(false);
            this.comTaskExecution.recalculateNextAndPlannedExecutionTimestamp();
            return this;
        }

        @Override
        public AbstractComTaskExecutionBuilder priority(int priority) {
            this.comTaskExecution.setPlannedPriority(priority);
            return this;
        }

        @Override
        public AbstractComTaskExecutionBuilder ignoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound) {
            this.comTaskExecution.setIgnoreNextExecutionSpecsForInbound(ignoreNextExecutionSpecsForInbound);
            return this;
        }

        @Override
        public AbstractComTaskExecutionBuilder scheduleNow() {
            this.comTaskExecution.scheduleNow();
            return this;
        }

        @Override
        public AbstractComTaskExecutionBuilder runNow() {
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
        public ComTaskExecution add() {
            this.comTaskExecution.prepareForSaving();
            this.comTaskExecution.getConnectionTask().ifPresent(ct -> ((ServerConnectionTask<?, ?>) ct).scheduledComTaskRescheduled(this.comTaskExecution));
            return this.comTaskExecution;
        }

    }

    public class ComTaskExecutionUpdaterImpl implements ComTaskExecutionUpdater {

        private final ComTaskExecutionImpl comTaskExecution;
        private boolean connectionTaskSchedulingMayHaveChanged = false;

        protected ComTaskExecutionUpdaterImpl(ComTaskExecutionImpl comTaskExecution) {
            this.comTaskExecution = comTaskExecution;
        }

        @JsonIgnore
        @XmlTransient
        public ComTaskExecutionImpl getComTaskExecution() {
            return this.comTaskExecution;
        }

        protected ComTaskExecutionUpdater self() {
            return this;
        }

        @Override
        public ComTaskExecutionUpdater connectionTask(ConnectionTask<?, ?> connectionTask) {
            this.comTaskExecution.setConnectionTask(connectionTask);
            this.comTaskExecution.doSetUseDefaultConnectionTask(false);
            this.comTaskExecution.doSetConnectionFunction(null);
            this.comTaskExecution.recalculateNextAndPlannedExecutionTimestamp();
            return this;
        }

        @Override
        public ComTaskExecutionUpdater priority(int executionPriority) {
            this.comTaskExecution.setPlannedPriority(executionPriority);
            return this;
        }

        @Override
        public ComTaskExecutionUpdater setIsTracing(boolean isTracing) {
            this.comTaskExecution.setIsTracing(isTracing);
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
        public ComTaskExecutionUpdater useDefaultConnectionTask(boolean useDefaultConnectionTask) {
            this.comTaskExecution.setUseDefaultConnectionTask(useDefaultConnectionTask);
            return this;
        }

        @Override
        public ComTaskExecutionUpdater useDefaultConnectionTask(ConnectionTask<?, ?> defaultConnectionTask) {
            this.comTaskExecution.setDefaultConnectionTask(defaultConnectionTask);
            this.connectionTaskSchedulingMayHaveChanged = true;
            return this;
        }

        @Override
        public ComTaskExecutionUpdater setConnectionFunction(ConnectionFunction connectionFunction) {
            this.comTaskExecution.setConnectionFunction(connectionFunction);
            this.connectionTaskSchedulingMayHaveChanged = true;
            return this;
        }

        @Override
        public ComTaskExecutionUpdater useConnectionTaskBasedOnConnectionFunction(ConnectionTask<?, ?> connectionTask) {
            this.comTaskExecution.setConnectionTaskBasedOnConnectionFunction(connectionTask);
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
            connectionTaskSchedulingMayHaveChanged = true;
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
            connectionTaskSchedulingMayHaveChanged = true;
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

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {
        //Ignore, only used for JSON
    }

    @Override
    public void removeSchedule() {
        setPlannedNextExecutionTimestamp(null);
        schedule(null);
    }

}
