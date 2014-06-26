package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class ScheduledComTaskExecutionImpl extends ComTaskExecutionImpl implements ScheduledComTaskExecution {

    private Reference<ComSchedule> comScheduleReference = ValueReference.absent();

    @Inject
    public ScheduledComTaskExecutionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, DeviceDataService deviceDataService, DeviceConfigurationService deviceConfigurationService, SchedulingService schedulingService) {
        super(dataModel, eventService, thesaurus, clock, deviceDataService, schedulingService);
    }

    @Override
    public ComSchedule getComSchedule() {
        return comScheduleReference.get();
    }

    @Override
    public boolean isScheduled() {
        return true;
    }

    @Override
    public boolean isAdHoc() {
        return false;
    }

    private void setComSchedule(ComSchedule comSchedule) {
        if (comSchedule==null) {
            this.comScheduleReference.setNull();
        }
        this.comScheduleReference.set(comSchedule);
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
            if (comTask.getMaxNumberOfTries()<minimalNrOfRetries) {
                minimalNrOfRetries=comTask.getMaxNumberOfTries();
            }
        }

        return minimalNrOfRetries;
    }

    @Override
    public List<ComTask> getComTasks() {
        return getComSchedule().getComTasks();
    }

    @Override
    boolean usesComSchedule(ComSchedule comSchedule) {
        return comSchedule != null && this.comScheduleReference.get().getId() == comSchedule.getId();
    }

    @Override
    boolean usesComTask(ComTask comTask) {
        return false;
    }

    interface ScheduledComTaskExecutionBuilder extends ComTaskExecutionBuilder<ScheduledComTaskExecutionBuilder, ScheduledComTaskExecutionImpl> {
        public ScheduledComTaskExecutionBuilder comSchedule(ComSchedule comSchedule);
    }


    public static class ScheduledComTaskExecutionBuilderImpl
            extends AbstractComTaskExecutionBuilder<ScheduledComTaskExecutionBuilder, ScheduledComTaskExecutionImpl>
            implements ScheduledComTaskExecutionBuilder {

        protected ScheduledComTaskExecutionBuilderImpl(ScheduledComTaskExecutionImpl comTaskExecution, Device device, ComTaskEnablement comTaskEnablement) {
            super(comTaskExecution, device, comTaskEnablement, ScheduledComTaskExecutionBuilder.class);
        }

        public ScheduledComTaskExecutionBuilder comSchedule(ComSchedule comSchedule) {
            this.comTaskExecution.setComSchedule(comSchedule);
            return self;
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

    @Override
    public boolean performsIdenticalTask(ComTaskExecutionImpl comTaskExecution) {
        return comTaskExecution != null && comTaskExecution.usesComSchedule(this.getComSchedule());
    }

    interface ScheduledComTaskExecutionUpdater extends ComTaskExecutionUpdater<ScheduledComTaskExecutionUpdater, ScheduledComTaskExecutionImpl> {
        public ScheduledComTaskExecutionUpdater comSchedule(ComSchedule comSchedule);

        ScheduledComTaskExecutionUpdater createOrUpdateNextExecutionSpec(TemporalExpression temporalExpression);
    }

    class ScheduledComTaskExecutionUpdaterImpl
        extends AbstractComTaskExecutionUpdater<ScheduledComTaskExecutionUpdater, ScheduledComTaskExecutionImpl>
        implements ScheduledComTaskExecutionUpdater {

        protected ScheduledComTaskExecutionUpdaterImpl(ScheduledComTaskExecutionImpl comTaskExecution) {
            super(comTaskExecution, ScheduledComTaskExecutionUpdater.class);
        }

        @Override
        public ScheduledComTaskExecutionUpdater comSchedule(ComSchedule comSchedule) {
            super.comTaskExecution.setComSchedule(comSchedule);
            return self;
        }

        @Override
        public ScheduledComTaskExecutionUpdater createOrUpdateNextExecutionSpec(TemporalExpression temporalExpression) {
            this.comTaskExecution.createOrUpdateMyNextExecutionSpecs(temporalExpression);
            this.comTaskExecution.recalculateNextAndPlannedExecutionTimestamp();
            return self;
        }
    }


}
