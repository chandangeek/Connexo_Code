/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_5 implements Upgrader {

    private final QueueTableSpec queueTableSpec;

    @Inject
    public UpgraderV10_5(MessageService messageService) {
        queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
//        DataModel dataModel = this.ormService.getDataModel(MessageService.COMPONENTNAME).get();
//        PreparedStatement statement = connection.prepareStatement("INSERT INTO MSG_SUBSCRIBERSPEC (destination, name, nls_component, nls_layer, systemManaged) VALUES (?,?,?,?,?,)");
//        statement.setString(1, UsagePointReadingMessageHandlerFactory.DESTINATION_NAME);
//        statement.setString(2, UsagePointReadingMessageHandlerFactory.DESTINATION_NAME);
//        statement.setString(3, UsagePointReadingMessageHandlerFactory.COMPONENT_NAME);
//        statement.setString(4, Layer.DOMAIN.name());
//        statement.setBoolean(5, false);
//        return statement;
//    }
        createUsagePointReadingImporterQueue();
    }

    private void createUsagePointReadingImporterQueue() {
        DestinationSpec destinationSpecUPReadImport = queueTableSpec.createDestinationSpec(UsagePointReadingMessageHandlerFactory.DESTINATION_NAME, 60);
        destinationSpecUPReadImport.save();
        destinationSpecUPReadImport.activate();
        destinationSpecUPReadImport.subscribe(TranslationKeys.Labels.USAGEPOINT_RECORD_MESSAGE_SUBSCRIBER, UsagePointFileImporterMessageHandler.COMPONENT_NAME, Layer.DOMAIN);
    }
}
