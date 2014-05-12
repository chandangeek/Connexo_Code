package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.tasks.AdHocComTaskExecution;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.ComTask;
import javax.inject.Inject;
import javax.inject.Provider;

public class AdHocComTaskExecutionImpl extends ComTaskExecutionImpl implements AdHocComTaskExecution {

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.COMTASK_IS_REQUIRED + "}")
    private Reference<ComTask> comTask = ValueReference.absent();

    @Inject
    public AdHocComTaskExecutionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, DeviceDataService deviceDataService, DeviceConfigurationService deviceConfigurationService, SchedulingService schedulingService) {
        super(dataModel, eventService, thesaurus, clock, deviceDataService, deviceConfigurationService, schedulingService);
    }

    @Override
    public boolean isScheduled() {
        return false;
    }

    @Override
    public boolean isAdHoc() {
        return true;
    }

    @Override
    public ComTask getComTask() {
        return comTask.get();       // we do an explicit get because ComTask is required and should not be null
    }

    @Override
    AdHocComTaskExecutionImpl initialize(Device device, ComTaskEnablement comTaskEnablement) {
        AdHocComTaskExecutionImpl initialize = super.initialize(device, comTaskEnablement);
        this.comTask.set(comTaskEnablement.getComTask());
        return initialize;
    }

    interface AdHocComTaskExecutionBuilder extends ComTaskExecutionBuilder<AdHocComTaskExecutionBuilder, AdHocComTaskExecutionImpl> {

    }

    class AdHocComTaskExecutionBuilderImpl extends AbstractComTaskExecutionBuilder<AdHocComTaskExecutionBuilder, AdHocComTaskExecutionImpl> {

        protected AdHocComTaskExecutionBuilderImpl(Provider<AdHocComTaskExecutionImpl> provider, Device device, ComTaskEnablement comTaskEnablement) {
            super(provider.get(), device, comTaskEnablement, AdHocComTaskExecutionBuilder.class);
        }
    }
}
