/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

class UpgraderV10_7 implements Upgrader {

    private final DataModel dataModel;
    private final MessageService messageService;
    private final Logger logger;

    @Inject
    public UpgraderV10_7(DataModel dataModel, MessageService messageService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7));
        messageService.createQueueTableSpec("MSG_PRIORITIZEDROWTABLE", "RAW", false, true);
        logger.log(Level.INFO, "Created QueueTable MSG_PRIORITIZEDROWTABLE");
    }

}
