package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;

/**
 * Handles Messages that contain a TaskOccurrenceMessage and passes the matching TaskOccurrence instance to the configured TaskExecutor
 */
class TaskExecutionMessageHandler implements MessageHandler {

    private final TaskExecutor taskExecutor;

    public TaskExecutionMessageHandler(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void process(Message message) {
        TaskOccurrence taskOccurrence = getTaskOccurrence(message);
        if (taskOccurrence != null) {
            taskExecutor.execute(taskOccurrence);
        }
    }

    private TaskOccurrence getTaskOccurrence(Message message) {
        return Bus.getOrmClient().getTaskOccurrenceFactory().get(getTaskOccurrenceMessage(message).taskOccurrenceId).get();
    }

    private TaskOccurrenceMessage getTaskOccurrenceMessage(Message message) {
        return Bus.getJsonService().deserialize(message.getPayload(), TaskOccurrenceMessage.class);

    }
}
