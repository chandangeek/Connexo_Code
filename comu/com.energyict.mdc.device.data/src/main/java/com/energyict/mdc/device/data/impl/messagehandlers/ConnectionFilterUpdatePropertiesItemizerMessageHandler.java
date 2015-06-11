package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.FilterFactory;
import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.ItemizeConnectionFilterUpdatePropertiesQueueMessage;
import com.energyict.mdc.device.data.tasks.UpdateConnectionTaskPropertiesQueueMessage;
import java.util.List;
import java.util.Optional;

/**
 * This message handler will update connection properties. Supports both a group of connections identified by a filter and an exhaustive list
 */
public class ConnectionFilterUpdatePropertiesItemizerMessageHandler implements MessageHandler {

    private ConnectionTaskService connectionTaskService;
    private MessageService messageService;
    private JsonService jsonService;
    private FilterFactory filterFactory;

    @Override
    public void process(Message message) {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(ConnectionTaskService.CONNECTION_PROP_UPDATER_QUEUE_DESTINATION);
        if (destinationSpec.isPresent()) {
            ItemizeConnectionFilterUpdatePropertiesQueueMessage filterQueueMessage = jsonService.deserialize(message.getPayload(), ItemizeConnectionFilterUpdatePropertiesQueueMessage.class);
            ConnectionTaskFilterSpecification connectionTaskFilterSpecification = filterFactory.buildFilterFromMessage(filterQueueMessage.connectionTaskFilterSpecification);
            List<ConnectionTask> connectionTasks = connectionTaskService.findConnectionTasksByFilter(connectionTaskFilterSpecification, 0, Integer.MAX_VALUE-1);
            connectionTasks.stream().forEach(c -> processMessagePost(new UpdateConnectionTaskPropertiesQueueMessage(c.getId(), filterQueueMessage.propertyValues), destinationSpec.get()));
        } else {
            // LOG failure
        }

    }

    private void processMessagePost(QueueMessage message, DestinationSpec destinationSpec) {
        String json = jsonService.serialize(message);
        destinationSpec.message(json).send();
    }


    @Override
    public void onMessageDelete(Message message) {

    }

    public MessageHandler init(ConnectionTaskService connectionTaskService, FilterFactory filterFactory, MessageService messageService, JsonService jsonService) {
        this.connectionTaskService = connectionTaskService;
        this.filterFactory = filterFactory;
        this.messageService = messageService;
        this.jsonService = jsonService;
        return this;
    }

}
