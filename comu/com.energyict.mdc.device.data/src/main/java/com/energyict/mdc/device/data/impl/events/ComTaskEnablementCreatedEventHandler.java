/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.ItemizeComTaskEnablementQueueMessage;
import com.energyict.mdc.device.data.exceptions.NoDestinationSpecFound;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventConstants;

import java.util.HashMap;
import java.util.Map;

@Component(name = "com.energyict.mdc.device.data.comtaskenablement.created.eventhandler", service = TopicHandler.class, immediate = true)
public class ComTaskEnablementCreatedEventHandler implements TopicHandler {

    static final String TOPIC = com.energyict.mdc.device.config.events.EventType.COMTASKENABLEMENT_CREATED.topic();

    private volatile DeviceDataModelService deviceDataModelService;

    public ComTaskEnablementCreatedEventHandler() {
        super();
    }

    public ComTaskEnablementCreatedEventHandler(DeviceDataModelService deviceDataModelService) {
        this.setDeviceDataModelService(deviceDataModelService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        ComTaskEnablement source = (ComTaskEnablement) localEvent.getSource();
        if (source.getDeviceConfiguration().isActive()) {
            notifyInterestedPartiesOfChange(source);
        }
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
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }
}