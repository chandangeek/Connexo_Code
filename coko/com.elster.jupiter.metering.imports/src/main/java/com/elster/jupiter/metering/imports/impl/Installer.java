package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import javax.inject.Inject;

final class Installer implements FullInstaller {

    private final MessageService messageService;

    @Inject
    Installer(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader) {
        if (!messageService.getDestinationSpec(UsagePointFileImporterMessageHandler.DESTINATION_NAME).isPresent()) {
            ExceptionCatcher.executing(
                    () -> {
                        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
                        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(UsagePointFileImporterMessageHandler.DESTINATION_NAME, 60);
                        destinationSpec.save();
                        destinationSpec.activate();
                        destinationSpec.subscribe(UsagePointFileImporterMessageHandler.SUBSCRIBER_NAME);
                    }
            ).andHandleExceptionsWith(Throwable::printStackTrace).execute();
        }
    }
}
