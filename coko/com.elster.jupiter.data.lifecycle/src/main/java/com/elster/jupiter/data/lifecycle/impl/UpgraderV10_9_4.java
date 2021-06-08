/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.Optional;

import static com.elster.jupiter.data.lifecycle.impl.Installer.CREATE_PARTITIONS_RECCURENT_TASK_NAME;
import static com.elster.jupiter.data.lifecycle.impl.Installer.RETRY_DELAY_IN_SECONDS;

public class UpgraderV10_9_4 implements Upgrader {
    private final MessageService messageService;
    private final TaskService taskService;

    @Inject
    UpgraderV10_9_4(MessageService messageService, TaskService taskService) {
        this.messageService = messageService;
        this.taskService = taskService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        DestinationSpec destination = upgradeSubscriberSpecs();
        createCreatePartitionsTask(destination);
    }

    private DestinationSpec upgradeSubscriberSpecs() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        return this.createMessageHandlerIfNotPresent(defaultQueueTableSpec, Installer.CREATE_PARTITIONS_DESTINATION_NAME, TranslationKeys.CREATE_PARTITIONS);
    }

    private DestinationSpec createMessageHandlerIfNotPresent(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey subscriberKey) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, RETRY_DELAY_IN_SECONDS);
            queue.activate();
            queue.subscribe(subscriberKey, LifeCycleService.COMPONENTNAME, Layer.DOMAIN);
            return queue;
        } else {
            boolean notSubscribedYet = destinationSpecOptional.get()
                    .getSubscribers()
                    .stream()
                    .noneMatch(spec -> spec.getName().equals(subscriberKey.getKey()));
            if (notSubscribedYet) {
                destinationSpecOptional.get().activate();
                destinationSpecOptional.get().subscribe(subscriberKey, LifeCycleService.COMPONENTNAME, Layer.DOMAIN);
            }
        }
        return destinationSpecOptional.get();
    }

    private void createCreatePartitionsTask(DestinationSpec destination) {
        if (!taskService.getRecurrentTask(CREATE_PARTITIONS_RECCURENT_TASK_NAME).isPresent()) {
            taskService.newBuilder()
                    .setApplication("Admin")
                    .setName(CREATE_PARTITIONS_RECCURENT_TASK_NAME)
                    .setScheduleExpressionString("0 0 19 ? * 1L") // last sunday of the month at 19:00
                    .setDestination(destination)
                    .setPayLoad("Create Partitions")
                    .scheduleImmediately(true)
                    .build();
        }
    }
}
