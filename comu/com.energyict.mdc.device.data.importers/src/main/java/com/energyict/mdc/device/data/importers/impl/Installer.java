package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import javax.inject.Inject;

class Installer implements FullInstaller {

    private final MessageService messageService;

    @Inject
    Installer(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader) {
        ExceptionCatcher.executing(
                this::createDestinationAndSubscriber
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
    }

    private void createDestinationAndSubscriber() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(DeviceDataImporterMessageHandler.DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(DeviceDataImporterMessageHandler.SUBSCRIBER_NAME);
    }

}
