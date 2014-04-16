package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.NextExecutionSpecs;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.config.TemporalExpression;
import com.energyict.mdc.device.data.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.exceptions.CannotUpdateObsoleteComTaskExecutionException;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsAlreadyObsoleteException;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.CreateEventType;
import com.energyict.mdc.device.data.impl.DeleteEventType;
import com.energyict.mdc.device.data.impl.PersistentIdObject;
import com.energyict.mdc.device.data.impl.UpdateEventType;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.tasks.ComTask;
import org.hibernate.validator.constraints.Range;

import javax.inject.Inject;

import javax.inject.Provider;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 11/04/14
 * Time: 15:09
 * <p/>
 * <p/>
 * TODO validation
 */
@ConnectionTaskIsRequiredWhenNotUsingDefault
public class ComTaskExecutionImpl extends PersistentIdObject<ComTaskExecution> implements ServerComTaskExecution, PersistenceAware {

    private final Clock clock;
    private final DeviceDataService deviceDataService;
    private final DeviceConfigurationService deviceConfigurationService;

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DEVICE_IS_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.COMTASK_IS_REQUIRED + "}")
    private Reference<ComTask> comTask = ValueReference.absent();
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.CONNECTIONTASK_IS_REQUIRED + "}")
    private Reference<ConnectionTask<?, ?>> connectionTask = ValueReference.absent();
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ARE_REQUIRED + "}")
    private Reference<ProtocolDialectConfigurationProperties> protocolDialectConfigurationProperties = ValueReference.absent();

    private Reference<ComPort> comPort = ValueReference.absent();

    private Date nextExecutionTimeStamp;
    private Date lastExecutionTimeStamp;
    private Date executionStart;
    private Date lastSuccessfulCompletionTimestamp;
    private Date plannedNextExecutionTimeStamp;
    private Date obsoleteDate;
    private Date modificationDate;

    /**
     * ExecutionPriority can be overruled by the Minimize ConnectionTask
     */
    @Range(min = TaskPriorityConstants.HIGHEST_PRIORITY, max = TaskPriorityConstants.LOWEST_PRIORITY, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.PRIORITY_NOT_IN_RANGE + "}")
    private int executionPriority;
    @Range(min = TaskPriorityConstants.HIGHEST_PRIORITY, max = TaskPriorityConstants.LOWEST_PRIORITY, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.PRIORITY_NOT_IN_RANGE + "}")
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
        if(this.nextExecutionSpecId > 0){
            if(myNextExecutionSpec){
                this.nextExecutionSpecHolder = new MyNextExecutionSpecHolder(this.nextExecutionSpecId);
            } else {
                this.nextExecutionSpecHolder = new MasterNextExecutionSpecHolder(this.nextExecutionSpecId);
            }
        } else {
            this.nextExecutionSpecHolder = new NoNextExecutionSpecHolder();
        }
    }

    /**
     * Serves as a <i>provider</i> for the current NextExecutionSpec.
     * The NextExecutionSpec will either be owned by:
     * <ul>
     *     <li>We, the ComTaskExecution</li>
     *     <li>The MasterSchedule</li>
     * </ul>
     * Depending on the ownership we need to take actions in order to save/update/delete the NextExecutionSpec.
     * My responsibility is to provide you with the correct NextExecutionSpec
     */
    private interface NextExecutionSpecHolder{
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
    public ComTaskExecutionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, DeviceDataService deviceDataService, DeviceConfigurationService deviceConfigurationService) {
        super(ComTaskExecution.class, dataModel, eventService, thesaurus);
        this.clock = clock;
        this.deviceDataService = deviceDataService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    public ComTaskExecutionImpl initialize(Device device, ComTaskEnablement comTaskEnablement) {
        this.device.set(device);
        this.comTask.set(comTaskEnablement.getComTask());
        if(comTaskEnablement.getNextExecutionSpecs() != null){
            this.nextExecutionSpecHolder = new MyNextExecutionSpecHolder(comTaskEnablement.getNextExecutionSpecs().getTemporalExpression());
        } else {
            this.nextExecutionSpecHolder = new NoNextExecutionSpecHolder();
        }
        this.ignoreNextExecutionSpecsForInbound = comTaskEnablement.isIgnoreNextExecutionSpecsForInbound();
        this.executionPriority = comTaskEnablement.getPriority();
        this.priority = comTaskEnablement.getPriority();
        this.useDefaultConnectionTask = comTaskEnablement.useDefaultConnectionTask();
        this.protocolDialectConfigurationProperties.set(comTaskEnablement.getProtocolDialectConfigurationProperties());
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
    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties() {
        return protocolDialectConfigurationProperties.orNull();
    }

    private void setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties){
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
        //TODO JP-1125
        return ServerComTaskStatus.getApplicableStatusFor(this);
    }

    @Override
    public boolean isOnHold() {
        return this.nextExecutionTimeStamp == null;
    }

    @Override
    public Date getNextExecutionTimestamp() {
        return this.nextExecutionTimeStamp;
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
        if(this.useDefaultConnectionTask){
            this.connectionTask.setNull();
        }
    }

    @Override
    public Date getExecutionStartedTimestamp() {
        return this.executionStart;
    }

    @Override
    public void makeObsolete() {
        reloadMyselfForObsoletion();
        validateMakeObsolete();
        this.obsoleteDate = this.now();
        this.post();
    }

    /**
     * We need to check if this task is currently running or someone else made it obsolete.
     * We are already in a Transaction so we don't wrap it again.
     */
    private void reloadMyselfForObsoletion() {
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
        } else if (this.connectionTask.get().getExecutingComServer() != null) {
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

    private void setConnectionTask(ConnectionTask<?, ?> connectionTask){
        this.connectionTask.set(connectionTask);
        setUseDefaultConnectionTask(!this.connectionTask.isPresent());
    }

    @Override
    public Date getLastExecutionStartTimestamp() {
        return this.lastExecutionTimeStamp;
    }

    @Override
    public Date getLastSuccessfulCompletionTimestamp() {
        return this.lastSuccessfulCompletionTimestamp;
    }

    @Override
    public NextExecutionSpecs getNextExecutionSpecs() {
        return this.nextExecutionSpecHolder.getNextExecutionSpec();
    }

    private void createMyNextExecutionSpecs(TemporalExpression temporalExpression){
        this.nextExecutionSpecHolder = new MyNextExecutionSpecHolder(temporalExpression);
    }

    private void createOrUpdateMyNextExecutionSpecs(TemporalExpression temporalExpression){
        if(this.nextExecutionSpecHolder.myNextExecutionSpec()){
            this.nextExecutionSpecHolder.updateTemporalExpression(temporalExpression);
        } else {
            this.nextExecutionSpecHolder = new MyNextExecutionSpecHolder(temporalExpression);
        }
    }

    private void removeNextExecutionSpec(){
        this.nextExecutionSpecHolder.delete();
        this.nextExecutionSpecHolder = new NoNextExecutionSpecHolder();
    }

    private void setMasterScheduleNextExecutionSpec(NextExecutionSpecs nextExecutionSpec){
        this.nextExecutionSpecHolder = new MasterNextExecutionSpecHolder(nextExecutionSpec);
    }

    @Override
    public boolean isIgnoreNextExecutionSpecsForInbound() {
        return this.ignoreNextExecutionSpecsForInbound;
    }

    private void setIgnoreNextExecutionSpecsForInbound(boolean ignoreNextExecutionSpecsForInbound){
        this.ignoreNextExecutionSpecsForInbound = ignoreNextExecutionSpecsForInbound;
    }

    @Override
    public Date getPlannedNextExecutionTimestamp() {
        return this.plannedNextExecutionTimeStamp;
    }

    @Override
    public int getPlannedPriority() {
        return this.priority;
    }

    @Override
    public void updateNextExecutionTimestamp() {
        //TODO JP-1125
    }

    @Override
    public void putOnHold() {
        this.schedule(null);
    }

    @Override
    public void scheduleNow() {
        this.schedule(this.clock.now());
    }

    @Override
    public void schedule(Date when) {

    }

    @Override
    public boolean attemptLock(ComPort comPort) {
        //TODO JP-1125
        return false;
    }

    @Override
    public void unlock() {
        //TODO JP-1125
    }

    @Override
    public void executionCompleted() {
        //TODO JP-1125
    }

    @Override
    public void executionFailed() {
        //TODO JP-1125
    }

    @Override
    public boolean lastExecutionFailed() {
        return this.lastExecutionFailed;
    }

    @Override
    public void executionStarted(ComPort comPort) {
        //TODO JP-1125
    }

    @Override
    public void connectionTaskCreated(Device device, ConnectionTask<?, ?> connectionTask) {
        //TODO JP-1125

    }

    @Override
    public void connectionTaskRemoved() {
        //TODO JP-1125

    }

    @Override
    public void updateConnectionTask(ConnectionTask<?, ?> connectionTask) {
        //TODO JP-1125

    }

    @Override
    public void updateToUseDefaultConnectionTask(ConnectionTask<?, ?> connectionTask) {
        //TODO JP-1125

    }

    @Override
    public void updateToUseNonExistingDefaultConnectionTask() {
        //TODO JP-1125

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

    }

    @Override
    protected void validateDelete() {

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
            masterScheduleNextExecutionSpec = ComTaskExecutionImpl.this.deviceConfigurationService.findNextExecutionSpecs(nextExecutionSpecId);
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

        private MyNextExecutionSpecHolder(long id){
            this.nextExecutionSpecId = id;
            this.nextExecutionSpecs = ComTaskExecutionImpl.this.deviceConfigurationService.findNextExecutionSpecs(this.nextExecutionSpecId);
        }

        private MyNextExecutionSpecHolder(TemporalExpression temporalExpression){
            this.nextExecutionSpecs = ComTaskExecutionImpl.this.deviceConfigurationService.newNextExecutionSpecs(temporalExpression);
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
        public ComTaskExecutionBuilder setUseDefaultConnectionTask(boolean useDefaultConnectionTask){
            this.comTaskExecution.setUseDefaultConnectionTask(useDefaultConnectionTask);
            return this;
        }

        @Override
        public ComTaskExecutionBuilder setConnectionTask(ConnectionTask<?, ?> connectionTask){
            this.comTaskExecution.setConnectionTask(connectionTask);
            return this;
        }

        @Override
        public ComTaskExecutionBuilder setPriority(int executionPriority){
            this.comTaskExecution.setExecutingPriority(executionPriority);
            return this;
        }

        @Override
        public ComTaskExecutionBuilder createNextExecutionSpec(TemporalExpression temporalExpression){
            this.comTaskExecution.createMyNextExecutionSpecs(temporalExpression);
            return this;
        }

        @Override
        public ComTaskExecution.ComTaskExecutionBuilder setMasterNextExecutionSpec(NextExecutionSpecs masterNextExecutionSpec) {
            this.comTaskExecution.setMasterScheduleNextExecutionSpec(masterNextExecutionSpec);
            return this;
        }

        @Override
        public ComTaskExecutionBuilder setIgnoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound){
            this.comTaskExecution.setIgnoreNextExecutionSpecsForInbound(ignoreNextExecutionSpecsForInbound);
            return this;
        }

        @Override
        public ComTaskExecutionBuilder setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties){
            this.comTaskExecution.setProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties);
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
        public ComTaskExecutionUpdater setUseDefaultConnectionTask(boolean useDefaultConnectionTask){
            this.comTaskExecution.setUseDefaultConnectionTask(useDefaultConnectionTask);
            return this;
        }

        @Override
        public ComTaskExecutionUpdater setConnectionTask(ConnectionTask<?, ?> connectionTask){
            this.comTaskExecution.setConnectionTask(connectionTask);
            return this;
        }

        @Override
        public ComTaskExecutionUpdater setPriority(int executionPriority){
            this.comTaskExecution.setExecutingPriority(executionPriority);
            return this;
        }

        @Override
        public ComTaskExecutionUpdater createOrUpdateNextExecutionSpec(TemporalExpression temporalExpression){
            this.comTaskExecution.createOrUpdateMyNextExecutionSpecs(temporalExpression);
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
        public ComTaskExecutionUpdater setIgnoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound){
            this.comTaskExecution.setIgnoreNextExecutionSpecsForInbound(ignoreNextExecutionSpecsForInbound);
            return this;
        }

        @Override
        public ComTaskExecutionUpdater setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties){
            this.comTaskExecution.setProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties);
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
