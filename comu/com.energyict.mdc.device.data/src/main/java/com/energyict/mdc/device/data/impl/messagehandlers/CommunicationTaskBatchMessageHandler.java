package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionQueueMessage;

/**
 * This message handler will trigger connections to rerun.
 * Created by bvn on 3/25/15.
 */
public class CommunicationTaskBatchMessageHandler implements MessageHandler {

    private CommunicationTaskService communicationTaskService;
    private JsonService jsonService;

    @Override
    public void process(Message message) {
        ComTaskExecutionQueueMessage comTaskExecutionQueueMessage = jsonService.deserialize(message.getPayload(), ComTaskExecutionQueueMessage.class);
        ComTaskExecution comTaskExecution = communicationTaskService.findComTaskExecution(comTaskExecutionQueueMessage.comTaskExecId).orElseThrow(() -> new RuntimeException());
        switch (comTaskExecutionQueueMessage.action) {
            case "scheduleNow": scheduleNow(comTaskExecution);
                break;
            case "runNow": runNow(comTaskExecution);
                break;
            default: // TODO log ("Not implemented: "+connectionTaskQueueMessage.action);
        }
    }

    private void runNow(ComTaskExecution comTaskExecution) {
        if (!comTaskExecution.isObsolete()) {
            comTaskExecution.runNow();
        } else {
            // TODO LOG OBSOLETE
        }
    }

    private void scheduleNow(ComTaskExecution comTaskExecution) {
        if (!comTaskExecution.isObsolete()) {
            comTaskExecution.scheduleNow();
        } else {
            // TODO LOG OBSOLETE
        }
    }

    @Override
    public void onMessageDelete(Message message) {

    }

    public MessageHandler init(CommunicationTaskService communicationTaskService, JsonService jsonService) {
        this.communicationTaskService = communicationTaskService;
        this.jsonService = jsonService;
        return this;
    }
}
