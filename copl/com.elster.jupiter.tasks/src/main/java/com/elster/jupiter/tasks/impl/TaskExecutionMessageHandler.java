package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.consumer.MessageHandler;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import oracle.jdbc.aq.AQMessage;

import java.sql.SQLException;

public class TaskExecutionMessageHandler implements MessageHandler {

    private final TaskExecutor taskExecutor;

    public TaskExecutionMessageHandler(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void process(AQMessage message) throws SQLException {
        TaskOccurrence taskOccurrence = getTaskOccurrence(message);
        if (taskOccurrence != null) {
            taskExecutor.execute(taskOccurrence);
        }
    }

    private TaskOccurrence getTaskOccurrence(AQMessage message) throws SQLException {
        return Bus.getOrmClient().getTaskOccurrenceFactory().get(getTaskOccurrenceMessage(message).taskOccurrenceId).get();
    }

    private TaskOccurrenceMessage getTaskOccurrenceMessage(AQMessage message) throws SQLException {
        return Bus.getJsonService().deserialize(message.getPayload(), TaskOccurrenceMessage.class);

    }
}
