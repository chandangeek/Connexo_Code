package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.util.streams.DecoratedStream;

import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

/**
 * Initiates the checking of all connection tasks
 * that depend on a partial connection task that has had
 * at least one of its required properties removed.
 * It will query all target connection tasks
 * and group them in sets of 100 and publish
 * a {@link RevalidateConnectionTasksAfterPropertyRemoval}
 * message for each set.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-14 (16:58)
 */
public final class StartConnectionTasksRevalidationAfterPropertyRemoval extends ConnectionTasksRevalidationMessage {

    private static final int DEFAULT_NUMBER_OF_CONNECTION_TASKS_PER_TRANSACTION = 250;
    private static final Logger LOGGER = Logger.getLogger(ConnectionTasksRevalidationMessage.class.getName());

    private ServerConnectionTaskService connectionTaskService;
    private long partialConnectionTaskId;
    private int numberOfConnectionTasksPerTransaction = DEFAULT_NUMBER_OF_CONNECTION_TASKS_PER_TRANSACTION;

    public static StartConnectionTasksRevalidationAfterPropertyRemoval forPublishing(MessageService messageService) {
        return new StartConnectionTasksRevalidationAfterPropertyRemoval(messageService);
    }

    private StartConnectionTasksRevalidationAfterPropertyRemoval(MessageService messageService) {
        super(messageService);
    }

    @Inject
    public StartConnectionTasksRevalidationAfterPropertyRemoval(MessageService messageService, ServerConnectionTaskService connectionTaskService) {
        this(messageService);
        this.connectionTaskService = connectionTaskService;
    }

    StartConnectionTasksRevalidationAfterPropertyRemoval(MessageService messageService, ServerConnectionTaskService connectionTaskService, int numberOfConnectionTasksPerTransaction) {
        this(messageService, connectionTaskService);
        this.numberOfConnectionTasksPerTransaction = numberOfConnectionTasksPerTransaction;
    }

    public StartConnectionTasksRevalidationAfterPropertyRemoval with(String properties) {
        return this.with(Long.parseLong(properties));
    }

    public StartConnectionTasksRevalidationAfterPropertyRemoval with(long partialConnectionTaskId) {
        this.partialConnectionTaskId = partialConnectionTaskId;
        return this;
    }

    @Override
    protected String propertiesPayload() {
        return String.valueOf(this.partialConnectionTaskId);
    }

    @Override
    protected void process() {
        LOGGER.fine(() -> "Starting revalidation of connection tasks that relate to partial connection task " + this.partialConnectionTaskId);
        DecoratedStream
                .decorate(this.connectionTaskService.findConnectionTasksForPartialId(this.partialConnectionTaskId).stream())
                .partitionPer(this.numberOfConnectionTasksPerTransaction)
                .map(this::toMessage)
                .forEach(ConnectionTasksRevalidationMessage::publish);
    }

    private RevalidateConnectionTasksAfterPropertyRemoval toMessage(List<Long> connectionTaskIds) {
        return RevalidateConnectionTasksAfterPropertyRemoval.forPublishing(this.getMessageService()).with(connectionTaskIds);
    }

}