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
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

public class AdHocComTaskExecutionImpl extends ComTaskExecutionImpl implements AdHocComTaskExecution {

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.COMTASK_IS_REQUIRED + "}")
    private Reference<ComTask> comTask = ValueReference.absent();

    @Inject
    public AdHocComTaskExecutionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, DeviceDataService deviceDataService, DeviceConfigurationService deviceConfigurationService, SchedulingService schedulingService) {
        super(dataModel, eventService, thesaurus, clock, deviceDataService, schedulingService);
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
    public int getMaxNumberOfTries() {
        return getComTask().getMaxNumberOfTries();
    }

    @Override
    public ComTask getComTask() {
        return comTask.get();       // we do an explicit get because ComTask is required and should not be null
    }

    @Override
    public List<ComTask> getComTasks() {
        return Arrays.asList(getComTask());
    }

    @Override
    AdHocComTaskExecutionImpl initialize(Device device, ComTaskEnablement comTaskEnablement) {
        AdHocComTaskExecutionImpl initialize = super.initialize(device, comTaskEnablement);
        this.comTask.set(comTaskEnablement.getComTask());
        return initialize;
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

    @Override
    public AdHocComTaskExecutionUpdater getUpdater() {
        return new AdHocComTaskExecutionUpdaterImpl(this);
    }

    @Override
    boolean usesComSchedule(ComSchedule comSchedule) {
        return false;
    }

    @Override
    boolean usesComTask(ComTask comTask) {
        return comTask != null && comTask.getId() == this.getComTask().getId();
    }

    @Override
    public boolean performsIdenticalTask(ComTaskExecutionImpl comTaskExecution) {
        return comTaskExecution != null && comTaskExecution.usesComTask(this.getComTask());
    }

    interface AdHocComTaskExecutionBuilder extends ComTaskExecutionBuilder<AdHocComTaskExecutionBuilder, AdHocComTaskExecutionImpl> {

    }

    public static class AdHocComTaskExecutionBuilderImpl extends AbstractComTaskExecutionBuilder<AdHocComTaskExecutionBuilder, AdHocComTaskExecutionImpl> implements AdHocComTaskExecutionBuilder{

        protected AdHocComTaskExecutionBuilderImpl(AdHocComTaskExecutionImpl adHocComTaskExecution, Device device, ComTaskEnablement comTaskEnablement) {
            super(adHocComTaskExecution, device, comTaskEnablement, AdHocComTaskExecutionBuilder.class);
        }
    }

    public interface AdHocComTaskExecutionUpdater extends ComTaskExecutionUpdater<AdHocComTaskExecutionUpdater, AdHocComTaskExecutionImpl> {
    }

    class AdHocComTaskExecutionUpdaterImpl
        extends AbstractComTaskExecutionUpdater<AdHocComTaskExecutionUpdater, AdHocComTaskExecutionImpl>
        implements AdHocComTaskExecutionUpdater {

        protected AdHocComTaskExecutionUpdaterImpl(AdHocComTaskExecutionImpl comTaskExecution) {
            super(comTaskExecution, AdHocComTaskExecutionUpdater.class);
        }

    }

    @Override
    public List<ProtocolTask> getProtocolTasks() {
        return Collections.unmodifiableList(this.getComTask().getProtocolTasks());
    }
}
