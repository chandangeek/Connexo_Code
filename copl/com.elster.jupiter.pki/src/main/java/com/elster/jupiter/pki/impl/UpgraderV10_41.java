package com.elster.jupiter.pki.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.pki.impl.importers.CertificateImporterMessageHandler;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpgraderV10_41 implements Upgrader {
    private final OrmService ormService;
    private final MessageService messageService;

    @Inject
    UpgraderV10_41(OrmService ormService, MessageService messageService) {
        this.ormService = ormService;
        this.messageService = messageService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        this.createImportQueue();
    }

    private void createImportQueue() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(CertificateImporterMessageHandler.DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(TranslationKeys.CERTIFICATE_MESSAGE_SUBSCRIBER, CertificateImporterMessageHandler.COMPONENT_NAME, Layer.DOMAIN);
    }
}
