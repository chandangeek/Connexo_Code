package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.EventType;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecutionUpdater;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of a ComTaskExecution that serves the FirmwareUpgradeTask.
 * It is a special case of the ManuallyScheduledComTaskExec which is <i>always</i> AdHoc,
 * so no NextExecutionSpec is allowed.
 */
@ComTaskMustBeFirmwareManagement(groups = {Save.Create.class, Save.Update.class})
public class FirmwareComTaskExecutionImpl extends ComTaskExecutionImpl implements FirmwareComTaskExecution {

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.COMTASK_IS_REQUIRED + "}")
    private Reference<ComTask> comTask = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ARE_REQUIRED + "}")
    private Reference<ProtocolDialectConfigurationProperties> protocolDialectConfigurationProperties = ValueReference.absent();

    @Inject
    public FirmwareComTaskExecutionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ServerConnectionTaskService connectionTaskService, ServerCommunicationTaskService communicationTaskService, SchedulingService schedulingService) {
        super(dataModel, eventService, thesaurus, clock, communicationTaskService, schedulingService);
    }

    public FirmwareComTaskExecutionImpl initializeFirmwareTask(Device device, ComTaskEnablement comTaskEnablement) {
        super.initializeFrom(device, comTaskEnablement);
        this.setComTask(comTaskEnablement.getComTask());
        this.setProtocolDialectConfigurationProperties(comTaskEnablement.getProtocolDialectConfigurationProperties());
        return this;
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
        return this.comTask.get().getMaxNumberOfTries();
    }

    @Override
    public Optional<NextExecutionSpecs> getNextExecutionSpecs() {
        return Optional.empty();
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

    @Override
    protected Instant calculateNextExecutionTimestamp(Instant now) {
        if (this.getLastExecutionStartTimestamp() != null
                && this.getNextExecutionTimestamp() != null
                && this.getLastExecutionStartTimestamp().isAfter(this.getNextExecutionTimestamp())) {
            return null;
        } else {
            return this.getNextExecutionTimestamp();
        }

    }

    @Override
    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties() {
        return this.protocolDialectConfigurationProperties.orNull();
    }

    @Override
    public FirmwareComTaskExecutionUpdater getUpdater() {
        return new FirmwareComTaskExecutionUpdaterImpl(this);
    }

    @Override
    public ComTask getComTask() {
        return this.comTask.get();
    }

    @Override
    public List<ProtocolTask> getProtocolTasks() {
        return this.comTask.get().getProtocolTasks();
    }

    @Override
    public List<ComTask> getComTasks() {
        return Arrays.asList(this.comTask.get());
    }

    @Override
    public boolean executesComSchedule(ComSchedule comSchedule) {
        return false;
    }

    @Override
    public boolean executesComTask(ComTask comTask) {
        return comTask != null && comTask.getId() == this.comTask.get().getId();
    }

    public void setComTask(ComTask comTask) {
        this.comTask.set(comTask);
    }

    @Override
    public void executionStarted(ComPort comPort) {
        super.executionStarted(comPort);
        postEvent(EventType.FIRMWARE_COMTASKEXECUTION_STARTED);
    }

    @Override
    public void executionCompleted() {
        super.executionCompleted();
        postEvent(EventType.FIRMWARE_COMTASKEXECUTION_COMPLETED);
    }

    @Override
    public void executionFailed() {
        super.executionFailed();
        postEvent(EventType.FIRMWARE_COMTASKEXECUTION_FAILED);
    }

    public static class FirmwareComTaskExecutionBuilderImpl extends AbstractComTaskExecutionBuilder<FirmwareComTaskExecution, FirmwareComTaskExecutionImpl> implements ComTaskExecutionBuilder<FirmwareComTaskExecution> {

        protected FirmwareComTaskExecutionBuilderImpl(FirmwareComTaskExecutionImpl instance) {
            super(instance);
        }
    }

    class FirmwareComTaskExecutionUpdaterImpl
            extends AbstractComTaskExecutionUpdater<FirmwareComTaskExecutionUpdater, FirmwareComTaskExecution, FirmwareComTaskExecutionImpl>
            implements FirmwareComTaskExecutionUpdater {

        protected FirmwareComTaskExecutionUpdaterImpl(FirmwareComTaskExecutionImpl comTaskExecution) {
            super(comTaskExecution, FirmwareComTaskExecutionUpdater.class);
        }

        @Override
        public FirmwareComTaskExecutionUpdater protocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
            this.getComTaskExecution().setProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties);
            return self();
        }
    }

    private void setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
        this.protocolDialectConfigurationProperties.set(protocolDialectConfigurationProperties);
    }
}
