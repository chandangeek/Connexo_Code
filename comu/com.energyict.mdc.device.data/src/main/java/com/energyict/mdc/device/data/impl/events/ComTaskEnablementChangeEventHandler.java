package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.energyict.mdc.device.data.comtaskenablement.eventhandler", service = TopicHandler.class, immediate = true)
public class ComTaskEnablementChangeEventHandler implements TopicHandler {

    private volatile DeviceDataModelService deviceDataModelService;
    private volatile SchedulingService schedulingService;

    public ComTaskEnablementChangeEventHandler() {
        super();
    }

    public ComTaskEnablementChangeEventHandler(DeviceDataModelService deviceDataModelService, SchedulingService schedulingService) {
        this.setDeviceDataModelService(deviceDataModelService);
        this.setSchedulingService(schedulingService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        ComTaskEnablement source = (ComTaskEnablement) localEvent.getSource();
        if (source.getDeviceConfiguration().isActive()) {
            handleOnActiveDeviceConfig(source);
        }
    }

    private void handleOnActiveDeviceConfig(ComTaskEnablement comTaskEnablement) {
        List<ComSchedule> affectedComSchedules = this.schedulingService.findAllSchedules().stream()
                .filter(comSchedule -> comSchedule.containsComTask(comTaskEnablement.getComTask()))
                .filter(comSchedule -> comSchedule.getComTasks().size() > 1)
                .collect(Collectors.toList());
        if (!affectedComSchedules.isEmpty() && !hasNoUsageOnDevices(comTaskEnablement, affectedComSchedules)) {
            throw new VetoComTaskEnablementChangeException(getThesaurus(), comTaskEnablement);
        }
    }

    private boolean hasNoUsageOnDevices(ComTaskEnablement comTaskEnablement, List<ComSchedule> affectedComSchedules) {
        return this.deviceDataModelService
                .dataModel()
                .query(ComTaskExecution.class, Device.class, DeviceConfiguration.class)
                .select(conditionForDeviceConfig(comTaskEnablement).and(conditionForComSchedules(affectedComSchedules)), null, false, null, 1, 1)
                .isEmpty();
    }

    private Condition conditionForDeviceConfig(ComTaskEnablement comTaskEnablement) {
        return where(ComTaskExecutionFields.DEVICE.fieldName() + "." + DeviceFields.DEVICECONFIGURATION.fieldName()).isEqualTo(comTaskEnablement.getDeviceConfiguration());
    }

    private Condition conditionForComSchedules(List<ComSchedule> affectedComSchedules) {
        return where(ComTaskExecutionFields.COM_SCHEDULE.fieldName()).in(affectedComSchedules);
    }

    private Thesaurus getThesaurus() {
        return this.deviceDataModelService.thesaurus();
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/config/comtaskenablement/UPDATED";
    }

    @Reference
    public void setSchedulingService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }
}