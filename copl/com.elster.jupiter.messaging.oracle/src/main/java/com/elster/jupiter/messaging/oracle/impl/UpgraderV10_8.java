/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

import static com.elster.jupiter.messaging.MessageService.QueueTable.JUPITEREVENTS_RAW_QUEUE_TABLE;

public class UpgraderV10_8 implements Upgrader {

    private final DataModel dataModel;
    private final MessageService messageService;

    @Inject
    public UpgraderV10_8(DataModel dataModel, MessageService messageService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 8));
        messageService.createQueueTableSpec(JUPITEREVENTS_RAW_QUEUE_TABLE.getQueueTableName(), "RAW", JUPITEREVENTS_RAW_QUEUE_TABLE.getStorageClause(), true);

    }

}
