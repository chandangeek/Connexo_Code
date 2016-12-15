package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.util.time.Never;

import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

public class Installer implements FullInstaller {

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final DataModel dataModel;
    private final MessageService messageService;
    private final TaskService taskService;

    @Inject
    public Installer(DataModel dataModel, MessageService messageService, TaskService taskService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.taskService = taskService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(this.dataModel, Version.latest());
        doTry(
                "Create message handlers",
                this::createMessageHandlers,
                logger
        );
        doTry(
                "Create recurrent task",
                this::createChangeRequestRecurrentTask,
                logger
        );
    }

    private void createMessageHandlers() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        this.createMessageHandler(defaultQueueTableSpec, ServerUsagePointLifeCycleService.DESTINATION_NAME, TranslationKeys.QUEUE_SUBSCRIBER);
    }

    private void createChangeRequestRecurrentTask() {
        Optional<DestinationSpec> destinationSpec = this.messageService.getDestinationSpec(ServerUsagePointLifeCycleService.DESTINATION_NAME);
        if (destinationSpec.isPresent()) {
            this.taskService.newBuilder().setApplication("Pulse")
                    .setName(ServerUsagePointLifeCycleService.EXECUTOR_TASK)
                    .setScheduleExpression(Never.NEVER)
                    .setDestination(destinationSpec.get())
                    .setPayLoad(TranslationKeys.QUEUE_SUBSCRIBER.getDefaultFormat())
                    .build();
        }
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey subscriberName) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
            queue.activate();
            queue.subscribe(subscriberName, UsagePointLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
        } else {
            boolean notSubscribedYet = !destinationSpecOptional.get().getSubscribers().stream().anyMatch(spec -> spec.getName().equals(subscriberName.getKey()));
            if (notSubscribedYet) {
                destinationSpecOptional.get().activate();
                destinationSpecOptional.get().subscribe(subscriberName, UsagePointLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
            }
        }
    }
}
