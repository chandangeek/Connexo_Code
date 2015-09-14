package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecutionUpdater;
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

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.time.TemporalExpression;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ManuallyScheduledComTaskExecution} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-30 (10:59)
 */
public class ManuallyScheduledComTaskExecutionImpl extends ComTaskExecutionImpl implements ManuallyScheduledComTaskExecution {

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.COMTASK_IS_REQUIRED + "}")
    private Reference<ComTask> comTask = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ARE_REQUIRED + "}")
    private Reference<ProtocolDialectConfigurationProperties> protocolDialectConfigurationProperties = ValueReference.absent();
    @IsPresent(groups = {SaveScheduled.class}, message = "{" + MessageSeeds.Keys.NEXTEXECUTIONSPEC_IS_REQUIRED + "}")
    private Reference<NextExecutionSpecs> nextExecutionSpecs = ValueReference.absent();

    @Inject
    public ManuallyScheduledComTaskExecutionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ServerConnectionTaskService connectionTaskService, ServerCommunicationTaskService communicationTaskService, SchedulingService schedulingService) {
        super(dataModel, eventService, thesaurus, clock, communicationTaskService, schedulingService);
    }

    public ManuallyScheduledComTaskExecutionImpl initialize(Device device, ComTaskEnablement comTaskEnablement, TemporalExpression temporalExpression) {
        super.initializeFrom(device, comTaskEnablement);
        this.setComTask(comTaskEnablement.getComTask());
        this.setProtocolDialectConfigurationProperties(comTaskEnablement.getProtocolDialectConfigurationProperties());
        if (temporalExpression != null) {
            this.setNextExecutionSpecsFrom(temporalExpression);
        }
        return this;
    }

    public ManuallyScheduledComTaskExecutionImpl initializeAdhoc(Device device, ComTaskEnablement comTaskEnablement) {
        return this.initialize(device, comTaskEnablement, null);
    }

    @Override
    protected void validateAndCreate() {
        this.recalculateNextAndPlannedExecutionTimestamp();
        if (this.isAdHoc()) {
            Save.CREATE.save(this.getDataModel(), this, SaveAdHoc.class);
        } else {
            Save.CREATE.save(this.getDataModel(), this, SaveScheduled.class);
        }
    }

    @Override
    protected void validateAndUpdate() {
        if (this.isAdHoc()) {
            Save.UPDATE.save(this.getDataModel(), this, SaveAdHoc.class);
        } else {
            Save.UPDATE.save(this.getDataModel(), this, SaveScheduled.class);
        }
    }

    @Override
    public void prepareForSaving() {
        if (this.nextExecutionSpecs.isPresent()) {
            this.nextExecutionSpecs.get().save();
        }
        super.prepareForSaving();
    }

    @Override
    public void doDelete() {
        super.doDelete();
        if (!this.isAdHoc()) {
            this.nextExecutionSpecs.get().delete();
        }
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
        return !this.nextExecutionSpecs.isPresent();
    }

    @Override
    public Optional<NextExecutionSpecs> getNextExecutionSpecs() {
        return this.nextExecutionSpecs.getOptional();
    }

    public void setNextExecutionSpecsFrom(TemporalExpression temporalExpression) {
        if (temporalExpression == null) {
            if (!this.nextExecutionSpecs.isPresent()) {
                // No change
            } else {
                this.nextExecutionSpecs.setNull();
            }
        } else {
            if (this.nextExecutionSpecs.isPresent()) {
                this.nextExecutionSpecs.get().setTemporalExpression(temporalExpression);
            } else {
                NextExecutionSpecs nextExecutionSpecs1 = this.getSchedulingService().newNextExecutionSpecs(temporalExpression);
                nextExecutionSpecs1.save();
                this.nextExecutionSpecs.set(nextExecutionSpecs1);
                this.recalculateNextAndPlannedExecutionTimestamp();
            }
        }
    }

    @Override
    public int getMaxNumberOfTries() {
        return getComTask().getMaxNumberOfTries();
    }

    @Override
    public ComTask getComTask() {
        // we do an explicit get because ComTask is required and should not be null
        return comTask.get();
    }

    private void setComTask(ComTask comTask) {
        this.comTask.set(comTask);
    }

    @Override
    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties() {
        return protocolDialectConfigurationProperties.orNull();
    }

    @Override
    public List<ComTask> getComTasks() {
        return Arrays.asList(getComTask());
    }

    void setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
        this.protocolDialectConfigurationProperties.set(protocolDialectConfigurationProperties);
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
        for (ProtocolTask protocolTask : this.getComTask().getProtocolTasks()) {
            if (protocolTaskClass.isAssignableFrom(protocolTask.getClass())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean executesComSchedule(ComSchedule comSchedule) {
        return false;
    }

    @Override
    public boolean executesComTask(ComTask comTask) {
        return comTask != null && comTask.getId() == this.getComTask().getId();
    }

    @Override
    public List<ProtocolTask> getProtocolTasks() {
        return Collections.unmodifiableList(this.getComTask().getProtocolTasks());
    }

    @Override
    public ManuallyScheduledComTaskExecutionUpdater getUpdater() {
        return new ManuallyScheduledComTaskExecutionUpdaterImpl(this);
    }

    @Override
    protected Instant calculateNextExecutionTimestamp(Instant now) {
        if (this.isAdHoc()) {
            if (   this.getLastExecutionStartTimestamp() != null
                && this.getNextExecutionTimestamp() != null
                && this.getLastExecutionStartTimestamp().isAfter(this.getNextExecutionTimestamp())) {
                return null;
            }
            else {
                return this.getNextExecutionTimestamp();
            }
        } else {
            return super.calculateNextExecutionTimestamp(now);
        }
    }

    public static class ManuallyScheduledComTaskExecutionBuilderImpl extends AbstractComTaskExecutionBuilder<ManuallyScheduledComTaskExecution, ManuallyScheduledComTaskExecutionImpl> implements ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> {

        protected ManuallyScheduledComTaskExecutionBuilderImpl(ManuallyScheduledComTaskExecutionImpl manuallyScheduledComTaskExecution) {
            super(manuallyScheduledComTaskExecution);
        }

    }

    class ManuallyScheduledComTaskExecutionUpdaterImpl
            extends AbstractComTaskExecutionUpdater<ManuallyScheduledComTaskExecutionUpdater, ManuallyScheduledComTaskExecution, ManuallyScheduledComTaskExecutionImpl>
            implements ManuallyScheduledComTaskExecutionUpdater {

        protected ManuallyScheduledComTaskExecutionUpdaterImpl(ManuallyScheduledComTaskExecutionImpl comTaskExecution) {
            super(comTaskExecution, ManuallyScheduledComTaskExecutionUpdater.class);
        }

        @Override
        public ManuallyScheduledComTaskExecutionUpdater protocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
            this.getComTaskExecution().setProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties);
            return self();
        }

        @Override
        public ManuallyScheduledComTaskExecutionUpdater scheduleAccordingTo(TemporalExpression temporalExpression) {
            this.getComTaskExecution().setNextExecutionSpecsFrom(temporalExpression);
            this.getComTaskExecution().recalculateNextAndPlannedExecutionTimestamp();
            return self();
        }

        @Override
        public ManuallyScheduledComTaskExecutionUpdater removeSchedule() {
            this.getComTaskExecution().setNextExecutionSpecsFrom(null);
            return self();
        }
    }

    /**
     * Uses as a marker interface for javax.validation when
     * saving an ad-hoc manually scheduled task.
     */
    interface SaveAdHoc {
    }

    /**
     * Uses as a marker interface for javax.validation when
     * saving a manually scheduled task.
     */
    interface SaveScheduled {
    }

}