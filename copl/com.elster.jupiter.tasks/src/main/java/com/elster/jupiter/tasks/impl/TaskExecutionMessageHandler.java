package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.json.JsonService;

/**
 * Handles Messages that contain a TaskOccurrenceMessage and passes the matching TaskOccurrence instance to the configured TaskExecutor
 */
class TaskExecutionMessageHandler implements MessageHandler {

    private final TaskExecutor taskExecutor;
    private final DataModel dataModel;
    private final JsonService jsonService;

    public TaskExecutionMessageHandler(DataModel dataModel, TaskExecutor taskExecutor, JsonService jsonService) {
        this.dataModel = dataModel;
        this.taskExecutor = taskExecutor;
        this.jsonService = jsonService;
    }

    @Override
    public void process(Message message) {
        TaskOccurrenceImpl taskOccurrence = getTaskOccurrence(message);
        taskExecutor.execute(taskOccurrence);
    }

    @Override
    public void onMessageDelete(Message message) {
        TaskOccurrenceImpl taskOccurrence = getTaskOccurrence(message);
        taskExecutor.postExecute(taskOccurrence);
        taskOccurrence.hasRun();
    }

    private TaskOccurrenceImpl getTaskOccurrence(Message message) {
        return (TaskOccurrenceImpl) dataModel.mapper(TaskOccurrence.class).getExisting(getTaskOccurrenceMessage(message).taskOccurrenceId);
    }

    private TaskOccurrenceMessage getTaskOccurrenceMessage(Message message) {
        return jsonService.deserialize(message.getPayload(), TaskOccurrenceMessage.class);
    }
}
