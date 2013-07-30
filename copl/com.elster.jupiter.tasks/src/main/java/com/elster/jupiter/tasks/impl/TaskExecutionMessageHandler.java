package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.consumer.MessageHandler;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.sql.SQLException;

public class TaskExecutionMessageHandler implements MessageHandler {

    private final TaskExecutor taskExecutor;

    public TaskExecutionMessageHandler(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void process(Message message) throws SQLException {
        TaskOccurrence taskOccurrence = getTaskOccurrence(message);
        if (taskOccurrence != null) {
            taskExecutor.execute(taskOccurrence);
        }
    }

    private TaskOccurrence getTaskOccurrence(Message message) throws SQLException {
        return Bus.getOrmClient().getTaskOccurrenceFactory().get(getTaskOccurrenceMessage(message).taskOccurrenceId).get();
    }

    private TaskOccurrenceMessage getTaskOccurrenceMessage(Message message) throws SQLException {
        return Bus.getJsonService().deserialize(message.getPayload(), TaskOccurrenceMessage.class);

    }
}
