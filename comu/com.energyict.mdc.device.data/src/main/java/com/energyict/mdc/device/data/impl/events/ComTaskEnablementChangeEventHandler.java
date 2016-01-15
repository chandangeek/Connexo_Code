package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.scheduling.SchedulingService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
        this.schedulingService.findAllSchedules().stream()
                .filter(comSchedule -> comSchedule.containsComTask(comTaskEnablement.getComTask()))
                .filter(comSchedule -> comSchedule.getComTasks().size() > 1)
                .forEach(filter.comSchedules::add);
        if (!filter.comSchedules.isEmpty()
                && !this.deviceDataModelService.communicationTaskService().findComTaskExecutionsByFilter(filter, 0, 1).isEmpty()) {
            throw new VetoComTaskEnablementChangeException(getThesaurus(), comTaskEnablement);
        }
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