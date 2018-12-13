/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

class Installer implements FullInstaller {

    private final MessageService messageService;

    @Inject
    Installer(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry(
                "Create Data Import queue",
                this::createDestinationAndSubscriber,
                logger
        );
    }

    private void createDestinationAndSubscriber() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(DeviceDataImporterMessageHandler.DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(TranslationKeys.DATA_IMPORTER_SUBSCRIBER, DeviceDataImporterMessageHandler.COMPONENT, Layer.DOMAIN);
    }

}