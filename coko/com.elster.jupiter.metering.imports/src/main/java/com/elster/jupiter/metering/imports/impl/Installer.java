/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

final class Installer implements FullInstaller {

    private final MessageService messageService;
    private QueueTableSpec queueTableSpec;

    @Inject
    Installer(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        if (!messageService.getDestinationSpec(UsagePointFileImporterMessageHandler.DESTINATION_NAME).isPresent()) {
            doTry(
                    "Create Usage Point import queue",
                    this::createQueue,
                    logger
            );
        }
        if (!messageService.getDestinationSpec(UsagePointReadingMessageHandlerFactory.DESTINATION_NAME).isPresent()) {
            doTry("Create Usage Point Reading Import queue",
                    this::createUsagePointReadingImporterQueue,
                    logger
            );
        }
    }

    private void createQueue() {
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(UsagePointFileImporterMessageHandler.DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(TranslationKeys.Labels.USAGEPOINT_MESSAGE_SUBSCRIBER, UsagePointFileImporterMessageHandler.COMPONENT_NAME, Layer.DOMAIN);
    }

    private void createUsagePointReadingImporterQueue() {
        DestinationSpec destinationSpecUPReadImport = queueTableSpec.createDestinationSpec(UsagePointReadingMessageHandlerFactory.DESTINATION_NAME, 60);
        destinationSpecUPReadImport.save();
        destinationSpecUPReadImport.activate();
        destinationSpecUPReadImport.subscribe(TranslationKeys.Labels.USAGEPOINT_RECORD_MESSAGE_SUBSCRIBER, UsagePointReadingMessageHandlerFactory.COMPONENT_NAME, Layer.DOMAIN);
    }

}