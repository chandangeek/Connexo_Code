/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;

import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

/**
 * Initiates the checking of all connection tasks
 * that depend on a partial connection task that has had
 * its ConnectionFunction modified (either added/updated/removed).
 * It will query all target connection tasks
 * and group them in sets of 250 and publish
 * a {@link RevalidateConnectionTasksAfterConnectionFunctionModification}
 * message for each set.
 *
 * @author Stijn Vanhoorelbeke
 * @since 2017-08-17 (11:34)
 */
public final class StartConnectionTasksRevalidationAfterConnectionFunctionModification extends ConnectionTasksConnectionFunctionRevalidationMessage {

    private static final int DEFAULT_NUMBER_OF_CONNECTION_TASKS_PER_TRANSACTION = 250;
    protected static final String PARTIAL_CONNECTION_TASK_CONNECTION_FUNCTION_DELIMITER = ";";
    private static final Logger LOGGER = Logger.getLogger(ConnectionTasksRevalidationMessage.class.getName());

    private ServerConnectionTaskService connectionTaskService;
    private long partialConnectionTaskId;
    private long previousConnectionFunctionId;
    private int numberOfConnectionTasksPerTransaction = DEFAULT_NUMBER_OF_CONNECTION_TASKS_PER_TRANSACTION;

    public static StartConnectionTasksRevalidationAfterConnectionFunctionModification forPublishing(MessageService messageService) {
        return new StartConnectionTasksRevalidationAfterConnectionFunctionModification(messageService);
    }

    private StartConnectionTasksRevalidationAfterConnectionFunctionModification(MessageService messageService) {
        super(messageService);
    }

    @Inject
    public StartConnectionTasksRevalidationAfterConnectionFunctionModification(MessageService messageService, ServerConnectionTaskService connectionTaskService) {
        this(messageService);
        this.connectionTaskService = connectionTaskService;
    }

    StartConnectionTasksRevalidationAfterConnectionFunctionModification(MessageService messageService, ServerConnectionTaskService connectionTaskService, int numberOfConnectionTasksPerTransaction) {
        this(messageService, connectionTaskService);
        this.numberOfConnectionTasksPerTransaction = numberOfConnectionTasksPerTransaction;
    }

    public StartConnectionTasksRevalidationAfterConnectionFunctionModification with(String properties) {
        String[] split = properties.split(PARTIAL_CONNECTION_TASK_CONNECTION_FUNCTION_DELIMITER);
        return this.with(Long.parseLong(split[0]), Long.parseLong(split[1]));
    }

    public StartConnectionTasksRevalidationAfterConnectionFunctionModification with(long partialConnectionTaskId, long previousConnectionFunctionId) {
        this.partialConnectionTaskId = partialConnectionTaskId;
        this.previousConnectionFunctionId = previousConnectionFunctionId;
        return this;
    }

    @Override
    protected String propertiesPayload() {
            return String.valueOf(this.partialConnectionTaskId) + PARTIAL_CONNECTION_TASK_CONNECTION_FUNCTION_DELIMITER + String.valueOf(this.previousConnectionFunctionId);
    }

    @Override
    protected void process() {
        LOGGER.fine(() -> "Starting revalidation of connection tasks that relate to partial connection task " + this.partialConnectionTaskId);
        DecoratedStream
                .decorate(this.connectionTaskService.findConnectionTasksForPartialId(this.partialConnectionTaskId).stream())
                .partitionPer(this.numberOfConnectionTasksPerTransaction)
                .map(this::toMessage)
                .forEach(ConnectionTasksConnectionFunctionRevalidationMessage::publish);
    }

    protected long getPartialConnectionTaskId() {
        return partialConnectionTaskId;
    }

    protected long getPreviousConnectionFunctionId() {
        return previousConnectionFunctionId;
    }

    private RevalidateConnectionTasksAfterConnectionFunctionModification toMessage(List<Long> connectionTaskIds) {
        return RevalidateConnectionTasksAfterConnectionFunctionModification.forPublishing(this.getMessageService()).with(previousConnectionFunctionId, connectionTaskIds);
    }
}