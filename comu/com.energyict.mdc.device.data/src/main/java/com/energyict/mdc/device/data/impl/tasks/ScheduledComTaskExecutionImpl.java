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
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecutionUpdater;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScheduledComTaskExecutionImpl extends ComTaskExecutionImpl implements ScheduledComTaskExecution {

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.COMSCHEDULE_IS_REQUIRED + "}")
    private Reference<ComSchedule> comSchedule = ValueReference.absent();
    private Reference<NextExecutionSpecs> nextExecutionSpecs = ValueReference.absent();

    @Inject
    public ScheduledComTaskExecutionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ServerConnectionTaskService connectionTaskService, ServerCommunicationTaskService communicationTaskService, SchedulingService schedulingService) {
        super(dataModel, eventService, thesaurus, clock, communicationTaskService, schedulingService);
    }

    public ScheduledComTaskExecutionImpl initialize(Device device, ComTaskEnablement comTaskEnablement, ComSchedule comSchedule) {
        this.initializeDevice(device);
        this.setIgnoreNextExecutionSpecsForInbound(true);
        this.setExecutingPriority(ComTaskExecution.DEFAULT_PRIORITY);
        this.setPlannedPriority(ComTaskExecution.DEFAULT_PRIORITY);
        this.setUseDefaultConnectionTask(comTaskEnablement.usesDefaultConnectionTask());
        this.setProtocolDialectConfigurationProperties(comTaskEnablement.getProtocolDialectConfigurationProperties());
        this.comTask.set(comTaskEnablement.getComTask());
        this.comSchedule.set(comSchedule);
        nextExecutionSpecs.set(comSchedule.getNextExecutionSpecs());
        return this;
    }

    @Override
    public void prepareForSaving() {
        this.recalculateNextAndPlannedExecutionTimestamp();
        super.prepareForSaving();
    }

    @Override
    protected void validateAndCreate() {
        this.recalculateNextAndPlannedExecutionTimestamp();
        super.validateAndCreate();
    }

    @Override
    public ComSchedule getComSchedule() {
        return comSchedule.get();
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

    private void setComSchedule(ComSchedule comSchedule) {
        if (comSchedule == null) {
            this.comSchedule.setNull();
            this.nextExecutionSpecs.setNull();
            return;
        }
        this.comSchedule.set(comSchedule);
        this.nextExecutionSpecs.set(comSchedule.getNextExecutionSpecs());
    }

    @Override
    public Optional<NextExecutionSpecs> getNextExecutionSpecs() {
        return Optional.of(this.comSchedule.get().getNextExecutionSpecs());
    }

    @Override
    public boolean isConfiguredToCollectRegisterData() {
        return getComSchedule().isConfiguredToCollectRegisterData();
    }

    @Override
    public boolean isConfiguredToCollectLoadProfileData() {
        return getComSchedule().isConfiguredToCollectLoadProfileData();
    }

    @Override
    public boolean isConfiguredToRunBasicChecks() {
        return getComSchedule().isConfiguredToRunBasicChecks();
    }

    @Override
    public boolean isConfiguredToCheckClock() {
        return getComSchedule().isConfiguredToCheckClock();
    }

    @Override
    public boolean isConfiguredToCollectEvents() {
        return getComSchedule().isConfiguredToCollectEvents();
    }

    @Override
    public boolean isConfiguredToSendMessages() {
        return getComSchedule().isConfiguredToSendMessages();
    }

    @Override
    public boolean isConfiguredToReadStatusInformation() {
        return getComSchedule().isConfiguredToReadStatusInformation();
    }

    @Override
    public boolean isConfiguredToUpdateTopology() {
        return getComSchedule().isConfiguredToUpdateTopology();
    }

    @Override
    public int getMaxNumberOfTries() {
        int minimalNrOfRetries = Integer.MAX_VALUE;
        for (ComTask comTask : this.getComSchedule().getComTasks()) {
            if (comTask.getMaxNumberOfTries() < minimalNrOfRetries) {
                minimalNrOfRetries = comTask.getMaxNumberOfTries();
            }
        }
        return minimalNrOfRetries;
    }

    @Override
    public boolean executesComSchedule(ComSchedule comSchedule) {
        return comSchedule != null && this.comSchedule.get().getId() == comSchedule.getId();
    }

    @Override
    public boolean executesComTask(ComTask comTask) {
        return comTask != null && this.getComSchedule().containsComTask(comTask);
    }

    @Override
    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties() {
        return null;
    }

    public static class ScheduledComTaskExecutionBuilderImpl
            extends AbstractComTaskExecutionBuilder<ScheduledComTaskExecution, ScheduledComTaskExecutionImpl>
            implements ComTaskExecutionBuilder<ScheduledComTaskExecution> {

        protected ScheduledComTaskExecutionBuilderImpl(ScheduledComTaskExecutionImpl comTaskExecution) {
            super(comTaskExecution);
        }

    }

    @Override
    public ScheduledComTaskExecutionUpdater getUpdater() {
        return new ScheduledComTaskExecutionUpdaterImpl(this);
    }

    @Override
    public List<ProtocolTask> getProtocolTasks() {
        List<ProtocolTask> protocolTasks = new ArrayList<>();
        for (ComTask comTask : getComSchedule().getComTasks()) {
            protocolTasks.addAll(comTask.getProtocolTasks());
        }
        return protocolTasks;
    }

    class ScheduledComTaskExecutionUpdaterImpl
            extends AbstractComTaskExecutionUpdater<ScheduledComTaskExecutionUpdater, ScheduledComTaskExecution, ScheduledComTaskExecutionImpl>
            implements ScheduledComTaskExecutionUpdater {

        protected ScheduledComTaskExecutionUpdaterImpl(ScheduledComTaskExecutionImpl comTaskExecution) {
            super(comTaskExecution, ScheduledComTaskExecutionUpdater.class);
        }
    }

}