package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.consumer.MessageHandler;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import oracle.jdbc.aq.AQMessage;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.sql.SQLException;

public class TaskExecutionMessageHandler implements MessageHandler {

    private final TaskExecutor taskExecutor;

    public TaskExecutionMessageHandler(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void process(AQMessage message) throws SQLException {
        TaskOccurrence taskOccurrence = getTaskOccurrence(message);
        if (taskOccurrence != null) taskExecutor.execute(taskOccurrence);
    }

    private TaskOccurrence getTaskOccurrence(AQMessage message) throws SQLException {
        try {
			return Bus.getOrmClient().getTaskOccurrenceFactory().get(getTaskOccurrenceMessage(message).taskOccurrenceId).get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//throw new RuntimeException(e);
			return null;
		}
    }

    private TaskOccurrenceMessage getTaskOccurrenceMessage(AQMessage message) throws SQLException {
        try {
            return new ObjectMapper().readValue(message.getPayload(), TaskOccurrenceMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}
