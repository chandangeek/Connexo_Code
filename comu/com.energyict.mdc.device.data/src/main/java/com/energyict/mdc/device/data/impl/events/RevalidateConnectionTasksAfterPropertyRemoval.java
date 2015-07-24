package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.util.streams.Functions;

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
 * at least one of its required properties removed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-14 (16:58)
 */
public final class RevalidateConnectionTasksAfterPropertyRemoval extends ConnectionTasksRevalidationMessage {

    private static final Logger LOGGER = Logger.getLogger(ConnectionTasksRevalidationMessage.class.getName());
    static final String DELIMITER = ",";
    private ConnectionTaskService connectionTaskService;
    private List<Long> connectionTaskIds;

    public static RevalidateConnectionTasksAfterPropertyRemoval forPublishing(MessageService messageService) {
        return new RevalidateConnectionTasksAfterPropertyRemoval(messageService);
    }

    private RevalidateConnectionTasksAfterPropertyRemoval(MessageService messageService) {
        super(messageService);
    }

    @Inject
    public RevalidateConnectionTasksAfterPropertyRemoval(MessageService messageService, ConnectionTaskService connectionTaskService) {
        this(messageService);
        this.connectionTaskService = connectionTaskService;
    }

    public RevalidateConnectionTasksAfterPropertyRemoval with(String properties) {
        return this.with(
                Stream
                    .of(properties.split(DELIMITER))
                    .map(Long::parseLong)
                    .collect(Collectors.toList()));
    }

    public RevalidateConnectionTasksAfterPropertyRemoval with(List<Long> connectionTaskIds) {
        this.connectionTaskIds = new ArrayList<>(connectionTaskIds);
        return this;
    }

    @Override
    protected String propertiesPayload() {
        return this.connectionTaskIds
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    protected void process() {
        LOGGER.fine(() -> "Starting revalidation of connection tasks: " + this.propertiesPayload());
        this.connectionTaskIds
                .stream()
                .map(this::findConnectionTask)
                .flatMap(Functions.asStream())
                .map(ServerConnectionTask.class::cast)
                .forEach(ServerConnectionTask::revalidatePropertiesAndAdjustStatus);
    }

    private Optional<ConnectionTask> findConnectionTask(Long id) {
        return this.connectionTaskService.findConnectionTask(id);
    }

}