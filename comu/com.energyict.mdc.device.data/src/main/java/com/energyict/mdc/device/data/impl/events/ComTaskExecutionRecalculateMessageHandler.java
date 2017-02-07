/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventConstants;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.device.data.comschedule.recalculate.messagehandler", service = MessageHandler.class, immediate = true)
public class ComTaskExecutionRecalculateMessageHandler implements MessageHandler {

    private static final String TOPIC = "com/energyict/mdc/device/data/comschedule/UPDATED";

    private volatile JsonService jsonService;
    private volatile ServerCommunicationTaskService communicationTaskService;
    private volatile SchedulingService schedulingService;

    public ComTaskExecutionRecalculateMessageHandler() {
        super();
    }

    // For testing purposes
    ComTaskExecutionRecalculateMessageHandler(JsonService jsonService, ServerCommunicationTaskService communicationTaskService, SchedulingService schedulingService) {
        this();
        this.setJsonService(jsonService);
        this.setCommunicationTaskService(communicationTaskService);
        this.setSchedulingService(schedulingService);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setSchedulingService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Reference
    public void setCommunicationTaskService(ServerCommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(Message message) {
        Map<String, Object> messageProperties = this.jsonService.deserialize(message.getPayload(), Map.class);
        String topic = (String) messageProperties.get(EventConstants.EVENT_TOPIC);
        if (TOPIC.equals(topic)) {
            long comScheduleId = this.getLong("comScheduleId", messageProperties);
            long minId = this.getLong("minId", messageProperties);
            long maxId = this.getLong("maxId", messageProperties);
            Optional<ComSchedule> comSchedule = this.schedulingService.findSchedule(comScheduleId);
            List<ComTaskExecution> comTaskExecutions = this.communicationTaskService.findComTaskExecutionsByComScheduleWithinRange(comSchedule.get(), minId, maxId);
            for (ComTaskExecution comTaskExecution : comTaskExecutions) {
                comTaskExecution.updateNextExecutionTimestamp();
                comTaskExecution.getDevice().save();
            }

        }
    }

    private Long getLong(String key, Map<String, Object> messageProperties) {
        Object contents = messageProperties.get(key);
        if (contents instanceof Long) {
            return (Long) contents;
        } else {
            return ((Integer) contents).longValue();
        }
    }

}