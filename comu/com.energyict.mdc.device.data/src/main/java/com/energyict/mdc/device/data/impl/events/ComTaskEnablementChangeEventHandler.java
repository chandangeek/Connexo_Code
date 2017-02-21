/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.ItemizeComTaskEnablementQueueMessage;
import com.energyict.mdc.device.data.exceptions.NoDestinationSpecFound;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.energyict.mdc.device.data.comtaskenablement.eventhandler", service = TopicHandler.class, immediate = true)
public class ComTaskEnablementChangeEventHandler implements TopicHandler {

    static final String TOPIC = com.energyict.mdc.device.config.events.EventType.COMTASKENABLEMENT_UPDATED.topic();

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
        validateComTaskEnablementChangeIsAllowed(comTaskEnablement);
        notifyInterestedPartiesOfChange(comTaskEnablement);
    }

    private void validateComTaskEnablementChangeIsAllowed(ComTaskEnablement comTaskEnablement) {
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
        return where(ComTaskExecutionFields.DEVICE.fieldName() + "." + DeviceFields.DEVICECONFIGURATION.fieldName()).isEqualTo(comTaskEnablement.getDeviceConfiguration())
                .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
    }

    private Condition conditionForComSchedules(List<ComSchedule> affectedComSchedules) {
        return where(ComTaskExecutionFields.COM_SCHEDULE.fieldName()).in(affectedComSchedules);
    }

    private void notifyInterestedPartiesOfChange(ComTaskEnablement comTaskEnablement) {
        ItemizeComTaskEnablementQueueMessage itemizeComTaskEnablementQueueMessage = new ItemizeComTaskEnablementQueueMessage(comTaskEnablement.getId());
        DestinationSpec destinationSpec = deviceDataModelService.messageService().getDestinationSpec(ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_QUEUE_DESTINATION).orElseThrow(new NoDestinationSpecFound(getThesaurus(), ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_QUEUE_DESTINATION));
        Map<String, Object> message = createComTaskEnablementQueueMessage(itemizeComTaskEnablementQueueMessage);
        destinationSpec.message(deviceDataModelService.jsonService().serialize(message)).send();
    }

    private Map<String, Object> createComTaskEnablementQueueMessage(ItemizeComTaskEnablementQueueMessage itemizeComTaskEnablementQueueMessage) {
        Map<String, Object> message = new HashMap<>(2);
        message.put(EventConstants.EVENT_TOPIC, ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_ACTION);
        message.put(ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_MESSAGE_VALUE, deviceDataModelService.jsonService().serialize(itemizeComTaskEnablementQueueMessage));
        return message;
    }

    private Thesaurus getThesaurus() {
        return this.deviceDataModelService.thesaurus();
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
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