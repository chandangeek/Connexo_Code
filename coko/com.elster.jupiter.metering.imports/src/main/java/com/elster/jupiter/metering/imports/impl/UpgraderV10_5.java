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
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_5 implements Upgrader {

    private DataModel dataModel;
    private MessageService messageService;
    private QueueTableSpec queueTableSpec;

    @Inject
    public UpgraderV10_5(OrmService ormService, MessageService messageService) {
        this.dataModel = ormService.getDataModel("MSG").get();
        this.messageService = messageService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 5));
        queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        createUsagePointReadingImporterQueue();
    }

    private void createUsagePointReadingImporterQueue() {
        DestinationSpec destinationSpecUPReadImport = queueTableSpec.createDestinationSpec(UsagePointReadingMessageHandlerFactory.DESTINATION_NAME, 60);
        destinationSpecUPReadImport.save();
        destinationSpecUPReadImport.activate();
        destinationSpecUPReadImport.subscribe(TranslationKeys.Labels.USAGEPOINT_RECORD_MESSAGE_SUBSCRIBER, UsagePointReadingMessageHandlerFactory.COMPONENT_NAME, Layer.DOMAIN);
    }
}
