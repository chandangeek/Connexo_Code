/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.protocol.api.ConnectionFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Revalidates a set of connection tasks that
 * depend on a partial connection task that has had
 * its ConnectionFunction modified (either added/updated/removed).
 *
 * @author Stijn Vanhoorelbeke
 * @since 2017-08-17 (11:34)
 */
public final class RevalidateConnectionTasksAfterConnectionFunctionModification extends ConnectionTasksConnectionFunctionRevalidationMessage {

    protected static final String CONNECTION_FUNCTION_CONNECTION_TASKS_DELIMITER = ";";
    protected static final String CONNECTION_TASKS_DELIMITER = ",";
    private static final Logger LOGGER = Logger.getLogger(ConnectionTasksRevalidationMessage.class.getName());

    private ConnectionTaskService connectionTaskService;
    private long previousConnectionFunctionId;
    private List<Long> connectionTaskIds;

    public static RevalidateConnectionTasksAfterConnectionFunctionModification forPublishing(MessageService messageService) {
        return new RevalidateConnectionTasksAfterConnectionFunctionModification(messageService);
    }

    private RevalidateConnectionTasksAfterConnectionFunctionModification(MessageService messageService) {
        super(messageService);
    }

    @Inject
    public RevalidateConnectionTasksAfterConnectionFunctionModification(MessageService messageService, ConnectionTaskService connectionTaskService) {
        this(messageService);
        this.connectionTaskService = connectionTaskService;
    }

    public RevalidateConnectionTasksAfterConnectionFunctionModification with(String properties) {
        String[] split = properties.split(CONNECTION_FUNCTION_CONNECTION_TASKS_DELIMITER);
        return this.with(
                Long.parseLong(split[0]),
                Stream
                        .of(split[1].split(CONNECTION_TASKS_DELIMITER))
                        .map(Long::parseLong)
                        .collect(Collectors.toList()));
    }

    public RevalidateConnectionTasksAfterConnectionFunctionModification with(long previousConnectionFunctionId, List<Long> connectionTaskIds) {
        this.previousConnectionFunctionId = previousConnectionFunctionId;
        this.connectionTaskIds = new ArrayList<>(connectionTaskIds);
        return this;
    }

    @Override
    protected String propertiesPayload() {
        return this.previousConnectionFunctionId +
                CONNECTION_FUNCTION_CONNECTION_TASKS_DELIMITER +
                this.connectionTaskIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(CONNECTION_TASKS_DELIMITER));
    }

    @Override
    protected void process() {
        LOGGER.fine(() -> {
            String connectionTaskIds = this.connectionTaskIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(CONNECTION_TASKS_DELIMITER));
            return "Starting revalidation of connection tasks: " + connectionTaskIds;
        });
        this.connectionTaskIds
                .stream()
                .map(this::findConnectionTask)
                .flatMap(Functions.asStream())
                .map(ServerConnectionTask.class::cast)
                .forEach(connectionTask ->
                        connectionTask.notifyConnectionFunctionUpdate(
                                findConnectionFunctionFor(connectionTask, this.previousConnectionFunctionId),
                                connectionTask.getPartialConnectionTask().getConnectionFunction()
                        ));
    }

    private Optional<ConnectionFunction> findConnectionFunctionFor(ConnectionTask connectionTask, long id) {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = connectionTask.getDevice().getDeviceType().getDeviceProtocolPluggableClass().get();
        return deviceProtocolPluggableClass.getProvidedConnectionFunctions().stream().filter(cf -> cf.getId() == id).findAny();
    }

    private Optional<ConnectionTask> findConnectionTask(Long id) {
        return this.connectionTaskService.findConnectionTask(id);
    }

    protected long getPreviousConnectionFunctionId() {
        return previousConnectionFunctionId;
    }

    protected List<Long> getConnectionTaskIds() {
        return connectionTaskIds;
    }
}