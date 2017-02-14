/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;

/**
 * Handles Messages that contain a TaskOccurrenceMessage and passes the matching TaskOccurrence instance to the configured TaskExecutor
 */
class TaskExecutionMessageHandler implements MessageHandler {

    private final TaskExecutor taskExecutor;
    private final DataModel dataModel;
    private final JsonService jsonService;
    private final TransactionService transactionService;

    public TaskExecutionMessageHandler(DataModel dataModel, TaskExecutor taskExecutor, JsonService jsonService, TransactionService transactionService) {
        this.dataModel = dataModel;
        this.taskExecutor = taskExecutor;
        this.jsonService = jsonService;
        this.transactionService = transactionService;
    }

    @Override
    public void process(Message message) {
        TaskOccurrenceImpl taskOccurrence = getTaskOccurrence(message);
        taskOccurrence.start();
        taskExecutor.execute(taskOccurrence);
    }

    @Override
    public void onMessageDelete(Message message) {
        TaskOccurrenceImpl taskOccurrence = getTaskOccurrence(message);
        boolean successFullPost = false;
        try {
            taskExecutor.postExecute(taskOccurrence);
            successFullPost = true;
        } finally {
            try (TransactionContext context = transactionService.getContext()) {
                taskOccurrence.hasRun(successFullPost);
                context.commit();
            }
        }
    }

    private TaskOccurrenceImpl getTaskOccurrence(Message message) {
        return (TaskOccurrenceImpl) dataModel.mapper(TaskOccurrence.class).getExisting(getTaskOccurrenceMessage(message).taskOccurrenceId);
    }

    private TaskOccurrenceMessage getTaskOccurrenceMessage(Message message) {
        return jsonService.deserialize(message.getPayload(), TaskOccurrenceMessage.class);
    }
}
