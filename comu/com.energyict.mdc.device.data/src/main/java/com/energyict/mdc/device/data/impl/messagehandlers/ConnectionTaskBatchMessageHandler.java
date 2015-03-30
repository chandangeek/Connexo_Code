package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskQueueMessage;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;

/**
 * This message handler will trigger connections to rerun.
 * Created by bvn on 3/25/15.
 */
public class ConnectionTaskBatchMessageHandler implements MessageHandler {

    private ConnectionTaskService connectionTaskService;
    private JsonService jsonService;

    @Override
    public void process(Message message) {
        ConnectionTaskQueueMessage connectionTaskQueueMessage = jsonService.deserialize(message.getPayload(), ConnectionTaskQueueMessage.class);
        ConnectionTask connectionTask = connectionTaskService.findConnectionTask(connectionTaskQueueMessage.connectionTaskId).orElseThrow(() -> new RuntimeException());
        switch (connectionTaskQueueMessage.action) {
            case "scheduleNow": scheduleNow(connectionTask);
                break;
            default: // TODO log ("Not implemented: "+connectionTaskQueueMessage.action);
        }
    }

    private void scheduleNow(ConnectionTask connectionTask) {
        if (!connectionTask.isObsolete()) {
            if (connectionTask instanceof ScheduledConnectionTask) {
                ((ScheduledConnectionTask) connectionTask).scheduleNow();
            } else {
                // TODO Only scheduled supported
            }
        } else {
            // TODO LOG OBSOLETE
        }
    }

    @Override
    public void onMessageDelete(Message message) {

    }

    public MessageHandler init(ConnectionTaskService connectionTaskService, JsonService jsonService) {
        this.connectionTaskService = connectionTaskService;
        this.jsonService = jsonService;
        return this;
    }
}
