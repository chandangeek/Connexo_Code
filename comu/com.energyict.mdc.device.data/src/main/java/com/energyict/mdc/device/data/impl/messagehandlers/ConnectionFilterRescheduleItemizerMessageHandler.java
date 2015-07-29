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
import com.energyict.mdc.device.data.tasks.RescheduleConnectionTaskQueueMessage;
import com.energyict.mdc.device.data.tasks.ItemizeConnectionFilterRescheduleQueueMessage;
import java.util.List;
import java.util.Optional;

/**
 * This message handler will trigger connections to rerun. Supports both a group of connections identified by a filter and an exhaustive list
 * Created by bvn on 3/25/15.
 */
public class ConnectionFilterRescheduleItemizerMessageHandler implements MessageHandler {

    private ConnectionTaskService connectionTaskService;
    private MessageService messageService;
    private JsonService jsonService;
    private FilterFactory filterFactory;

    @Override
    public void process(Message message) {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_DESTINATION);
        if (destinationSpec.isPresent()) {
            ItemizeConnectionFilterRescheduleQueueMessage filterQueueMessage = jsonService.deserialize(message.getPayload(), ItemizeConnectionFilterRescheduleQueueMessage.class);
            ConnectionTaskFilterSpecification connectionTaskFilterSpecification = filterFactory.buildFilterFromMessage(filterQueueMessage.connectionTaskFilterSpecification);
            List<ConnectionTask> connectionTasks = connectionTaskService.findConnectionTasksByFilter(connectionTaskFilterSpecification, 0, Integer.MAX_VALUE-1);
            connectionTasks.stream().forEach(c -> processMessagePost(new RescheduleConnectionTaskQueueMessage(c.getId(), filterQueueMessage.action), destinationSpec.get()));
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
