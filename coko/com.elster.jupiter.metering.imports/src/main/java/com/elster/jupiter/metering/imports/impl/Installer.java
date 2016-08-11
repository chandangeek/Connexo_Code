package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

final class Installer implements FullInstaller {

    private final MessageService messageService;

    @Inject
    Installer(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        if (!messageService.getDestinationSpec(UsagePointFileImporterMessageHandler.DESTINATION_NAME).isPresent()) {
            doTry(
                    "Create Usage Point import queue",
                    this::createQueue,
                    logger
            );
        }
    }

    private void createQueue() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(UsagePointFileImporterMessageHandler.DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(UsagePointFileImporterMessageHandler.SUBSCRIBER_NAME).create();
    }
}
