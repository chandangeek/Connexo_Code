/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;

import javax.inject.Inject;
import java.util.logging.Logger;

class SskInstaller implements FullInstaller {

    private final MessageService messageService;

    @Inject
    SskInstaller(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry(
                "Create Data Import Queue",
                this::createDestinationAndSubscriber,
                logger
        );
    }

    private void createDestinationAndSubscriber() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(SecureDeviceKeyImporterMessageHandler.DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(TranslationKeys.SECURE_SHIPMENT_KEY_SUBSCRIBER, SecureDeviceKeyImporterMessageHandler.COMPONENT_NAME, Layer.DOMAIN);
    }

}