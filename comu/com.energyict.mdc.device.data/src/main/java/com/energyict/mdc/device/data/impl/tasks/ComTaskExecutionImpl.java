package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.data.ComTaskExecutionDependant;
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
import com.energyict.mdc.device.data.impl.constraintvalidators.ConnectionTaskIsRequiredWhenNotUsingDefault;
import com.energyict.mdc.device.data.impl.constraintvalidators.UniqueComTaskExecutionPerDevice;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.TemporalExpression;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import org.hibernate.validator.constraints.Range;

/**
 * Implementation of a ComTaskExecution
 *
 * Copyrights EnergyICT
 * Date: 11/04/14
 * Time: 15:09
 * <p/>
 */
@ConnectionTaskIsRequiredWhenNotUsingDefault
@UniqueComTaskExecutionPerDevice
public class ComTaskExecutionImpl extends PersistentIdObject<ComTaskExecution> implements ServerComTaskExecution, PersistenceAware {

    private final Clock clock;
    private final DeviceDataService deviceDataService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final SchedulingService schedulingService;

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DEVICE_IS_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.COMTASK_IS_REQUIRED + "}")
    private Reference<ComTask> comTask = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ARE_REQUIRED + "}")
    private Reference<ProtocolDialectConfigurationProperties> protocolDialectConfigurationProperties = ValueReference.absent();

    private Reference<ComSchedule> comScheduleReference = ValueReference.absent();

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
    @Range(min = TaskPriorityConstants.HIGHEST_PRIORITY, max = TaskPriorityConstants.LOWEST_PRIORITY, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.PRIORITY_NOT_IN_RANGE + "}")
    private int executionPriority;
    @Range(min = TaskPriorityConstants.HIGHEST_PRIORITY, max = TaskPriorityConstants.LOWEST_PRIORITY, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.PRIORITY_NOT_IN_RANGE + "}")
    private int priority;
    private int currentRetryCount;
    private boolean lastExecutionFailed;
    private boolean useDefaultConnectionTask;
    private boolean ignoreNextExecutionSpecsForInbound;

    /**
     * NextExecutionSpec attributes
     */
    private long nextExecutionSpecId;
    private boolean myNextExecutionSpec;
    private NextExecutionSpecHolder nextExecutionSpecHolder = new NoNextExecutionSpecHolder();

    @Override
    public void postLoad() {
        if (this.nextExecutionSpecId > 0) {
            if (myNextExecutionSpec) {
                this.nextExecutionSpecHolder = new MyNextExecutionSpecHolder(this.nextExecutionSpecId);
            } else {
                this.nextExecutionSpecHolder = new MasterNextExecutionSpecHolder(this.nextExecutionSpecId);
            }
        } else {
            this.nextExecutionSpecHolder = new NoNextExecutionSpecHolder();
        }
    }

    private void setComSchedule(ComSchedule comSchedule) {
        if (comSchedule==null) {
            this.comScheduleReference.setNull();
        }
        this.comScheduleReference.set(comSchedule);
    }

    /**
     * Serves as a <i>provider</i> for the current NextExecutionSpec.
     * The NextExecutionSpec will either be owned by:
     * <ul>
     * <li>We, the ComTaskExecution</li>
     * <li>The MasterSchedule</li>
     * </ul>
     * Depending on the ownership we need to take actions in order to save/update/delete the NextExecutionSpec.
     * My responsibility is to provide you with the correct NextExecutionSpec
     */
    private interface NextExecutionSpecHolder {

        NextExecutionSpecs getNextExecutionSpec();

        void updateTemporalExpression(TemporalExpression temporalExpression);

        void save();

        void delete();

        /**
         * @return true if we own the NextExecutionSpecs, false otherwise
         */
        boolean myNextExecutionSpec();
    }

    @Inject
    public ComTaskExecutionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, DeviceDataService deviceDataService, DeviceConfigurationService deviceConfigurationService, SchedulingService schedulingService) {
        super(ComTaskExecution.class, dataModel, eventService, thesaurus);
        this.clock = clock;
        this.deviceDataService = deviceDataService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.schedulingService = schedulingService;
    }

    ComTaskExecutionImpl initialize(Device device, ComTaskEnablement comTaskEnablement) {
        this.device.set(device);
        this.comTask.set(comTaskEnablement.getComTask());
        if (comTaskEnablement.getNextExecutionSpecs() != null) {
            this.nextExecutionSpecHolder = new MyNextExecutionSpecHolder(comTaskEnablement.getNextExecutionSpecs().getTemporalExpression());
        } else {
            this.nextExecutionSpecHolder = new NoNextExecutionSpecHolder();
        }
        this.ignoreNextExecutionSpecsForInbound = comTaskEnablement.isIgnoreNextExecutionSpecsForInbound();
        this.executionPriority = comTaskEnablement.getPriority();
        this.priority = comTaskEnablement.getPriority();
        setUseDefaultConnectionTask(comTaskEnablement.usesDefaultConnectionTask());
        this.protocolDialectConfigurationProperties.set(comTaskEnablement.getProtocolDialectConfigurationProperties().orNull());
        return this;
    }

    @Override
    public boolean isScheduled() {
        return this.nextExecutionSpecHolder.getNextExecutionSpec() != null;
    }

    @Override
    public boolean isAdhoc() {
        return !isScheduled();
    }

    @Override
    public Device getDevice() {
        return this.device.get();   // we do an explicit get because Device is required and should not be null
    }

    @Override
    public ComTask getComTask() {
        return comTask.get();       // we do an explicit get because ComTask is required and should not be null
    }

    @Override
    public ComSchedule getComSchedule() {
        return comScheduleReference.get();
    }

    @Override
    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties() {
        return protocolDialectConfigurationProperties.orNull();
    }

    private void setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
        this.protocolDialectConfigurationProperties.set(protocolDialectConfigurationProperties);
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
    public int getPriority() {
        return this.executionPriority;
    }

    private void setExecutingPriority(int executingPriority) {
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
    public int getMaxNumberOfTries() {
        return this.getComTask().getMaxNumberOfTries();
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

    private void setUseDefaultConnectionTask(boolean useDefaultConnectionTask) {
        this.useDefaultConnectionTask = useDefaultConnectionTask;
        if (this.useDefaultConnectionTask) {
            this.connectionTask.set(this.deviceDataService.findDefaultConnectionTaskForDevice(getDevice()));
        }
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
            this.connectionTask.set(updatedVersionOfMyself.getConnectionTask());
        }
    }

    private void validateMakeObsolete() {
        if (this.isObsolete()) {
            throw new ComTaskExecutionIsAlreadyObsoleteException(this.getThesaurus(), this);
        } else if (this.comPort.isPresent()) {
            throw new ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException(this.getThesaurus(), this, this.getExecutingComPort().getComServer());
        }

        if (this.useDefaultConnectionTask) {
            ConnectionTask<?,?> defaultConnectionTaskForDevice = this.deviceDataService.findDefaultConnectionTaskForDevice(getDevice());
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

    private void setConnectionTask(ConnectionTask<?, ?> connectionTask) {
        this.connectionTask.set(connectionTask);
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
    public NextExecutionSpecs getNextExecutionSpecs() {
        return this.nextExecutionSpecHolder.getNextExecutionSpec();
    }

    private void createMyNextExecutionSpecs(TemporalExpression temporalExpression) {
        this.nextExecutionSpecHolder = new MyNextExecutionSpecHolder(temporalExpression);
    }

    private void createOrUpdateMyNextExecutionSpecs(TemporalExpression temporalExpression) {
        if (this.nextExecutionSpecHolder.myNextExecutionSpec()) {
            this.nextExecutionSpecHolder.updateTemporalExpression(temporalExpression);
        } else {
            this.nextExecutionSpecHolder = new MyNextExecutionSpecHolder(temporalExpression);
        }
    }

    private void removeNextExecutionSpec() {
        this.nextExecutionSpecHolder.delete();
        this.nextExecutionSpecHolder = new NoNextExecutionSpecHolder();
        this.updateNextExecutionTimestamp();
    }

    private void setMasterScheduleNextExecutionSpec(NextExecutionSpecs nextExecutionSpec) {
        this.nextExecutionSpecHolder = new MasterNextExecutionSpecHolder(nextExecutionSpec);
    }

    @Override
    public boolean isIgnoreNextExecutionSpecsForInbound() {
        return this.ignoreNextExecutionSpecsForInbound;
    }

    private void setIgnoreNextExecutionSpecsForInbound(boolean ignoreNextExecutionSpecsForInbound) {
        this.ignoreNextExecutionSpecsForInbound = ignoreNextExecutionSpecsForInbound;
    }

    @Override
    public Date getPlannedNextExecutionTimestamp() {
        return this.plannedNextExecutionTimestamp;
    }

    @Override
    public int getPlannedPriority() {
        return this.priority;
    }

    @Override
    public void updateNextExecutionTimestamp() {
        recalculateNextAndPlannedExecutionTimestamp();
        post();
    }

    private void recalculateNextAndPlannedExecutionTimestamp() {
        Date plannedNextExecutionTimestamp = this.calculateNextExecutionTimestamp(this.clock.now());
        this.schedule(plannedNextExecutionTimestamp, plannedNextExecutionTimestamp);
    }

    protected Date calculateNextExecutionTimestamp(Date now) {
        return this.calculateNextExecutionTimestampFromBaseline(now);
    }

    private Date calculateNextExecutionTimestampFromBaseline(Date baseLine) {
        NextExecutionSpecs nextExecutionSpecs = this.getNextExecutionSpecs();
        if (nextExecutionSpecs != null) {
            Calendar calendar = Calendar.getInstance(this.clock.getTimeZone());
            calendar.setTime(baseLine);
            return nextExecutionSpecs.getNextTimestamp(calendar);
        } else {
            return null;
        }
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

    private void setNextExecutionTimestamp(Date nextExecutionTimestamp) {
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
        if (comTaskDefinedRescheduleDelay == null) {
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
        if (isAdhoc()) {
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
    protected void assignConnectionTask(ConnectionTask<?,?> connectionTask) {
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
        this.dataModel.remove(this);
    }

    @Override
    protected void validateDelete() {
        // nothing to validate
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

    private <T extends ProtocolTask> boolean isConfiguredToCollectDataOfClass (Class<T> protocolTaskClass) {
        for (ProtocolTask protocolTask : getComTask().getProtocolTasks()) {
            if (protocolTaskClass.isAssignableFrom(protocolTask.getClass())) {
                return true;
            }
        }
        return false;
    }

    /**
     * We don't do our own persistence, our device will take care of that
     */
    public void prepareForSaving() {
        this.nextExecutionSpecHolder.save();
        this.modificationDate = this.now();
        validateNotObsolete();
    }

    /**
     * Need to delete the nextExecutionSpec as it is not Referenced by the ComTaskExecution
     */
    public void delete() {
        this.nextExecutionSpecHolder.delete();
        this.deleteComTaskSessions();
        super.delete();
    }

    private void deleteComTaskSessions() {
        List<ComTaskExecutionDependant> modulesImplementing = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(ComTaskExecutionDependant.class);
        for (ComTaskExecutionDependant comTaskExecutionDependant : modulesImplementing) {
            comTaskExecutionDependant.comTaskExecutionDeleted(this);
        }
    }

    protected Date now() {
        return this.clock.now();
    }

    protected void validateNotObsolete() {
        if (this.obsoleteDate != null) {
            throw new CannotUpdateObsoleteComTaskExecutionException(this.getThesaurus(), this);
        }
    }

    private class MasterNextExecutionSpecHolder implements NextExecutionSpecHolder {

        private final NextExecutionSpecs masterScheduleNextExecutionSpec;

        private MasterNextExecutionSpecHolder(NextExecutionSpecs masterScheduleNextExecutionSpec) {
            this.masterScheduleNextExecutionSpec = masterScheduleNextExecutionSpec;
        }

        public MasterNextExecutionSpecHolder(long nextExecutionSpecId) {
            masterScheduleNextExecutionSpec = ComTaskExecutionImpl.this.schedulingService.findNextExecutionSpecs(nextExecutionSpecId);
        }

        @Override
        public NextExecutionSpecs getNextExecutionSpec() {
            return masterScheduleNextExecutionSpec;
        }

        @Override
        public void updateTemporalExpression(TemporalExpression temporalExpression) {
            // nothing to update
        }

        @Override
        public void save() {
            ComTaskExecutionImpl.this.myNextExecutionSpec = false;
            ComTaskExecutionImpl.this.nextExecutionSpecId = masterScheduleNextExecutionSpec.getId();
        }

        @Override
        public void delete() {
            // nothing to delete
        }

        @Override
        public boolean myNextExecutionSpec() {
            return false;
        }
    }

    private class MyNextExecutionSpecHolder implements NextExecutionSpecHolder {

        private long nextExecutionSpecId;
        private NextExecutionSpecs nextExecutionSpecs;

        private MyNextExecutionSpecHolder(long id) {
            this.nextExecutionSpecId = id;
            this.nextExecutionSpecs = ComTaskExecutionImpl.this.schedulingService.findNextExecutionSpecs(this.nextExecutionSpecId);
        }

        private MyNextExecutionSpecHolder(TemporalExpression temporalExpression) {
            this.nextExecutionSpecs = ComTaskExecutionImpl.this.schedulingService.newNextExecutionSpecs(temporalExpression);
        }

        @Override
        public NextExecutionSpecs getNextExecutionSpec() {
            return this.nextExecutionSpecs;
        }

        @Override
        public void updateTemporalExpression(TemporalExpression temporalExpression) {
            this.nextExecutionSpecs.setTemporalExpression(temporalExpression);
        }

        @Override
        public void save() {
            ComTaskExecutionImpl.this.myNextExecutionSpec = true;
            this.nextExecutionSpecs.save();
            ComTaskExecutionImpl.this.nextExecutionSpecId = this.nextExecutionSpecs.getId();
        }

        @Override
        public void delete() {
            this.nextExecutionSpecs.delete();
        }

        @Override
        public boolean myNextExecutionSpec() {
            return true;
        }
    }

    private class NoNextExecutionSpecHolder implements NextExecutionSpecHolder {

        @Override
        public NextExecutionSpecs getNextExecutionSpec() {
            // TODO maybe return something ???
            return null;
        }

        @Override
        public void updateTemporalExpression(TemporalExpression temporalExpression) {
            // nothing to update
        }

        @Override
        public void save() {
            ComTaskExecutionImpl.this.nextExecutionSpecId = 0;
            ComTaskExecutionImpl.this.myNextExecutionSpec = true;
        }

        @Override
        public void delete() {
            // really nothing to delete ...
        }

        @Override
        public boolean myNextExecutionSpec() {
            return false;
        }
    }

    public static abstract class ComTaskExecutionBuilder implements ComTaskExecution.ComTaskExecutionBuilder {

        private final ComTaskExecutionImpl comTaskExecution;

        protected ComTaskExecutionBuilder(Provider<ComTaskExecutionImpl> comTaskExecutionProvider, Device device, ComTaskEnablement comTaskEnablement) {
            this.comTaskExecution = comTaskExecutionProvider.get().initialize(device, comTaskEnablement);
        }

        @Override
        public ComTaskExecutionBuilder setUseDefaultConnectionTask(boolean useDefaultConnectionTask) {
            this.comTaskExecution.setUseDefaultConnectionTask(useDefaultConnectionTask);
            return this;
        }

        @Override
        public ComTaskExecutionBuilder setConnectionTask(ConnectionTask<?, ?> connectionTask) {
            this.comTaskExecution.setConnectionTask(connectionTask);
            this.comTaskExecution.setUseDefaultConnectionTask(false);
            this.comTaskExecution.recalculateNextAndPlannedExecutionTimestamp();
            return this;
        }

        @Override
        public ComTaskExecutionBuilder setPriority(int executionPriority) {
            this.comTaskExecution.setExecutingPriority(executionPriority);
            return this;
        }

        @Override
        public ComTaskExecutionBuilder createNextExecutionSpec(TemporalExpression temporalExpression) {
            this.comTaskExecution.createMyNextExecutionSpecs(temporalExpression);
            this.comTaskExecution.recalculateNextAndPlannedExecutionTimestamp();
            return this;
        }

        @Override
        public ComTaskExecution.ComTaskExecutionBuilder setMasterNextExecutionSpec(NextExecutionSpecs masterNextExecutionSpec) {
            this.comTaskExecution.setMasterScheduleNextExecutionSpec(masterNextExecutionSpec);
            return this;
        }

        @Override
        public ComTaskExecutionBuilder setIgnoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound) {
            this.comTaskExecution.setIgnoreNextExecutionSpecsForInbound(ignoreNextExecutionSpecsForInbound);
            return this;
        }

        @Override
        public ComTaskExecutionBuilder setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
            this.comTaskExecution.setProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties);
            return this;
        }

        @Override
        public ComTaskExecution.ComTaskExecutionBuilder comSchedule(ComSchedule comSchedule) {
            this.comTaskExecution.comScheduleReference.set(comSchedule);
            return this;
        }

        @Override
        public ComTaskExecution add() {
            this.comTaskExecution.prepareForSaving();
            return this.comTaskExecution;
        }
    }

    public static abstract class ComTaskExecutionUpdater implements ComTaskExecution.ComTaskExecutionUpdater {

        private final ComTaskExecutionImpl comTaskExecution;

        protected ComTaskExecutionUpdater(ComTaskExecutionImpl comTaskExecution) {
            this.comTaskExecution = comTaskExecution;
        }

        @Override
        public ComTaskExecutionUpdater setUseDefaultConnectionTaskFlag(boolean useDefaultConnectionTask) {
            this.comTaskExecution.setUseDefaultConnectionTask(useDefaultConnectionTask);
            return this;
        }

        @Override
        public ComTaskExecutionUpdater setConnectionTask(ConnectionTask<?, ?> connectionTask) {
            this.comTaskExecution.setConnectionTask(connectionTask);
            this.comTaskExecution.setUseDefaultConnectionTask(false);
            this.comTaskExecution.recalculateNextAndPlannedExecutionTimestamp();
            return this;
        }

        @Override
        public ComTaskExecutionUpdater setPriority(int executionPriority) {
            this.comTaskExecution.setExecutingPriority(executionPriority);
            return this;
        }

        @Override
        public ComTaskExecutionUpdater createOrUpdateNextExecutionSpec(TemporalExpression temporalExpression) {
            this.comTaskExecution.createOrUpdateMyNextExecutionSpecs(temporalExpression);
            this.comTaskExecution.recalculateNextAndPlannedExecutionTimestamp();
            return this;
        }

        @Override
        public ComTaskExecution.ComTaskExecutionUpdater removeNextExecutionSpec() {
            this.comTaskExecution.removeNextExecutionSpec();
            return this;
        }

        @Override
        public ComTaskExecution.ComTaskExecutionUpdater setMasterNextExecutionSpec(NextExecutionSpecs masterNextExecutionSpec) {
            this.comTaskExecution.setMasterScheduleNextExecutionSpec(masterNextExecutionSpec);
            return this;
        }

        @Override
        public ComTaskExecutionUpdater setIgnoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound) {
            this.comTaskExecution.setIgnoreNextExecutionSpecsForInbound(ignoreNextExecutionSpecsForInbound);
            return this;
        }

        @Override
        public ComTaskExecutionUpdater setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
            this.comTaskExecution.setProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties);
            return this;
        }

        @Override
        public ComTaskExecution.ComTaskExecutionUpdater setNextExecutionTimeStampAndPriority(Date nextExecutionTimestamp, int priority) {
            this.comTaskExecution.setNextExecutionTimestamp(nextExecutionTimestamp);
            this.comTaskExecution.setExecutingPriority(priority);
            return this;
        }

        @Override
        public ComTaskExecution update() {
            this.comTaskExecution.prepareForSaving();
            this.comTaskExecution.save();
            return this.comTaskExecution;
        }
    }
}
