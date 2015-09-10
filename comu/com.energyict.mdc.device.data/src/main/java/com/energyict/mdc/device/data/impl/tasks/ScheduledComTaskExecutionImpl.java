package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.constraintvalidators.ComTasksInComScheduleMustHaveSameConfigurationSettings;
import com.energyict.mdc.device.data.impl.constraintvalidators.UniqueComSchedulePerDevice;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecutionUpdater;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@UniqueComSchedulePerDevice(groups = {Save.Create.class, Save.Update.class})
@ComTasksInComScheduleMustHaveSameConfigurationSettings(groups = {Save.Create.class, Save.Update.class})
public class ScheduledComTaskExecutionImpl extends ComTaskExecutionImpl implements ScheduledComTaskExecution {

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.COMSCHEDULE_IS_REQUIRED + "}")
    private Reference<ComSchedule> comSchedule = ValueReference.absent();

    @Inject
    public ScheduledComTaskExecutionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ServerConnectionTaskService connectionTaskService, ServerCommunicationTaskService communicationTaskService, SchedulingService schedulingService) {
        super(dataModel, eventService, thesaurus, clock, communicationTaskService, schedulingService);
    }

    public ScheduledComTaskExecutionImpl initialize(Device device, ComSchedule comSchedule) {
        this.initializeDevice(device);
        this.setIgnoreNextExecutionSpecsForInbound(true);
        this.setExecutingPriority(ComTaskExecution.DEFAULT_PRIORITY);
        this.setPlannedPriority(ComTaskExecution.DEFAULT_PRIORITY);
        this.setUseDefaultConnectionTask(true);
        this.comSchedule.set(comSchedule);
        return this;
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
        }
        this.comSchedule.set(comSchedule);
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
    public List<ComTask> getComTasks() {
        return getComSchedule().getComTasks();
    }

    @Override
    public boolean executesComSchedule(ComSchedule comSchedule) {
        return comSchedule != null && this.comSchedule.get().getId() == comSchedule.getId();
    }

    @Override
    public boolean executesComTask(ComTask comTask) {
        return comTask != null && this.getComSchedule().containsComTask(comTask);
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