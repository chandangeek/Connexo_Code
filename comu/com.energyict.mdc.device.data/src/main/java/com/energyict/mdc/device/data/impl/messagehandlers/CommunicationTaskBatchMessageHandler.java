/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionQueueMessage;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This message handler will trigger connections to rerun.
 * Created by bvn on 3/25/15.
 */
public class CommunicationTaskBatchMessageHandler implements MessageHandler {
    private static final Logger LOGGER = Logger.getLogger(CommunicationTaskBatchMessageHandler.class.getSimpleName());
    private CommunicationTaskService communicationTaskService;
    private ConnectionTaskService connectionTaskService;
    private JsonService jsonService;

    @Override
    public void process(Message message) {
        ComTaskExecutionQueueMessage comTaskExecutionQueueMessage = jsonService.deserialize(message.getPayload(), ComTaskExecutionQueueMessage.class);
        ComTaskExecution comTaskExecution = communicationTaskService.findComTaskExecution(comTaskExecutionQueueMessage.comTaskExecId).orElseThrow(() -> new RuntimeException());
        long comTaskId = comTaskExecution.getId();
        Optional<ConnectionTask> ct = connectionTaskService.findAndLockConnectionTaskById(comTaskExecution.getConnectionTaskId());
        comTaskExecution = communicationTaskService.findAndLockComTaskExecutionById(comTaskId)
                .orElseThrow(() -> new IllegalStateException("ComTaskExecution with id: " + comTaskId + " not found."));
        switch (comTaskExecutionQueueMessage.action) {
            case "scheduleNow":
                scheduleNow(comTaskExecution);
                break;
            case "runNow":
                runNow(comTaskExecution);
                break;
            default:
                LOGGER.log(Level.WARNING, "Unknown action for ComTaskExecution: " + comTaskExecutionQueueMessage.action);
        }
    }

    private void runNow(ComTaskExecution comTaskExecution) {
        if (!comTaskExecution.isObsolete()) {
            communicationTaskService.findAndLockComTaskExecutionById(comTaskExecution.getId()).ifPresent(ComTaskExecution::runNow);
            LOGGER.info("ComTaskExecution '" + comTaskExecution.getId() + "': runNow()");
        } else {
            LOGGER.info("ComTaskExecution '" + comTaskExecution.getId() + "' skipped: it is obsolete");
        }
    }

    private void scheduleNow(ComTaskExecution comTaskExecution) {
        if (!comTaskExecution.isObsolete()) {
            comTaskExecution.scheduleNow();
            LOGGER.info("ComTaskExecution '" + comTaskExecution.getId() + "': scheduleNow()");
        } else {
            LOGGER.info("ComTaskExecution '" + comTaskExecution.getId() + "' skipped: it is obsolete");
        }
    }

    @Override
    public void onMessageDelete(Message message) {

    }

    public MessageHandler init(CommunicationTaskService communicationTaskService, JsonService jsonService, ConnectionTaskService connectionTaskService) {
        this.communicationTaskService = communicationTaskService;
        this.jsonService = jsonService;
        this.connectionTaskService = connectionTaskService;
        return this;
    }
}
