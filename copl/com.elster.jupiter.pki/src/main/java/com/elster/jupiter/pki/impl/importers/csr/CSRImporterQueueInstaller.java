/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.impl.SecurityManagementServiceImpl;
import com.elster.jupiter.pki.impl.TranslationKeys;

import javax.inject.Inject;

public class CSRImporterQueueInstaller {
    private final MessageService messageService;

    @Inject
    public CSRImporterQueueInstaller(MessageService messageService) {
        this.messageService = messageService;
    }

    public void installIfNotPresent() {
        if (!messageService.getDestinationSpec(CSRImporterMessageHandlerFactory.DESTINATION_NAME).isPresent()) {
            install();
        }
    }

    public void install() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(CSRImporterMessageHandlerFactory.DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(TranslationKeys.CSR_IMPORTER_MESSAGE_HANDLER, SecurityManagementService.COMPONENTNAME, new SecurityManagementServiceImpl().getLayer());
    }
}
