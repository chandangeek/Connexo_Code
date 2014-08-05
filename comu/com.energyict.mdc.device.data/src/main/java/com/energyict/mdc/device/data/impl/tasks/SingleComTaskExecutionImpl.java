package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
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
import com.elster.jupiter.util.time.Clock;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides code reuse opportunities for components that
 * provide an implementation for the {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}
 * interface that execute a single {@link com.energyict.mdc.tasks.ComTask} at a time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-30 (14:14)
 */
public abstract class SingleComTaskExecutionImpl extends ComTaskExecutionImpl {

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.COMTASK_IS_REQUIRED + "}")
    private Reference<ComTask> comTask = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ARE_REQUIRED + "}")
    private Reference<ProtocolDialectConfigurationProperties> protocolDialectConfigurationProperties = ValueReference.absent();

    @Inject
    public SingleComTaskExecutionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, DeviceDataService deviceDataService, SchedulingService schedulingService) {
        super(dataModel, eventService, thesaurus, clock, deviceDataService, schedulingService);
    }

    protected void initializeFrom(Device device, ComTaskEnablement comTaskEnablement) {
        super.initializeFrom(device, comTaskEnablement);
        this.comTask.set(comTaskEnablement.getComTask());
        this.protocolDialectConfigurationProperties.set(comTaskEnablement.getProtocolDialectConfigurationProperties().orNull());
    }

    public ComTask getComTask() {
        // we do an explicit get because ComTask is required and should not be null
        return comTask.get();
    }

    @Override
    public List<ComTask> getComTasks() {
        return Arrays.asList(getComTask());
    }

    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties() {
        return protocolDialectConfigurationProperties.orNull();
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

    private <T extends ProtocolTask> boolean isConfiguredToCollectDataOfClass (Class<T> protocolTaskClass) {
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
    public boolean performsIdenticalTask(ComTaskExecutionImpl comTaskExecution) {
        return comTaskExecution != null && comTaskExecution.executesComTask(this.getComTask());
    }

    @Override
    public List<ProtocolTask> getProtocolTasks() {
        return Collections.unmodifiableList(this.getComTask().getProtocolTasks());
    }

}