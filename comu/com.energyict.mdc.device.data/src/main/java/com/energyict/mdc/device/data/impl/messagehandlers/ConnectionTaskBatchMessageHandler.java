package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskQueueMessage;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This message handler will trigger connections to rerun.
 * Created by bvn on 3/25/15.
 */
public class ConnectionTaskBatchMessageHandler implements MessageHandler {

    private static final Logger LOGGER = Logger.getLogger(ConnectionTaskBatchMessageHandler.class.getSimpleName());

    private ConnectionTaskService connectionTaskService;
    private JsonService jsonService;

    @Override
    public void process(Message message) {
        ConnectionTaskQueueMessage connectionTaskQueueMessage = jsonService.deserialize(message.getPayload(), ConnectionTaskQueueMessage.class);
        ConnectionTask connectionTask = connectionTaskService.findConnectionTask(connectionTaskQueueMessage.connectionTaskId).orElseThrow(() -> new RuntimeException());
        switch (connectionTaskQueueMessage.action) {
            case "scheduleNow": scheduleNow(connectionTask);
                break;
            default: LOGGER.log(Level.WARNING, "Unknown action for ConnectionTask: "+connectionTaskQueueMessage.action);
        }
    }

    private void scheduleNow(ConnectionTask connectionTask) {
        if (!connectionTask.isObsolete()) {
            if (connectionTask instanceof ScheduledConnectionTask) {
                LOGGER.info("Connection task '"+connectionTask.getName()+"': scheduleNow()");
                ((ScheduledConnectionTask) connectionTask).scheduleNow();
            } else {
                LOGGER.info("Connection task '"+connectionTask.getName()+"' skipped: not a ScheduledConnectionTask");
            }
        } else {
            LOGGER.info("Connection task '"+connectionTask.getName()+"' skipped: it is obsolete");
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
