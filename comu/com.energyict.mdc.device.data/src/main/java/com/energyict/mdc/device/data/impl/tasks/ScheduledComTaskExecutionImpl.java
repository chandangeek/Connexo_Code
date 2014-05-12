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
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import javax.inject.Inject;
import javax.inject.Provider;

public class ScheduledComTaskExecutionImpl extends ComTaskExecutionImpl implements ScheduledComTaskExecution {

    private Reference<ComSchedule> comScheduleReference = ValueReference.absent();

    @Inject
    public ScheduledComTaskExecutionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, DeviceDataService deviceDataService, DeviceConfigurationService deviceConfigurationService, SchedulingService schedulingService) {
        super(dataModel, eventService, thesaurus, clock, deviceDataService, deviceConfigurationService, schedulingService);
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

    interface ScheduledComTaskExecutionBuilder extends ComTaskExecutionBuilder<ScheduledComTaskExecutionBuilder, ScheduledComTaskExecutionImpl> {
        public ScheduledComTaskExecutionBuilder comSchedule(ComSchedule comSchedule);
    }


    class ScheduledComTaskExecutionBuilderImpl
            extends AbstractComTaskExecutionBuilder<ScheduledComTaskExecutionBuilder, ScheduledComTaskExecutionImpl>
            implements ScheduledComTaskExecutionBuilder {

        protected ScheduledComTaskExecutionBuilderImpl(Provider<ScheduledComTaskExecutionImpl> comTaskExecutionProvider, Device device, ComTaskEnablement comTaskEnablement) {
            super(comTaskExecutionProvider.get(), device, comTaskEnablement, ScheduledComTaskExecutionBuilder.class);
        }

        public ScheduledComTaskExecutionBuilder comSchedule(ComSchedule comSchedule) {
            this.comTaskExecution.comScheduleReference.set(comSchedule);
            return self;
        }


    }
}
