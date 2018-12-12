/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstallerImpl implements FullInstaller {

    private final DataModel dataModel;
    private final MessageService messageService;

    @Inject
    public InstallerImpl(DataModel dataModel, MessageService messageService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
    }

    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        createQueueTables(logger);
    }

    private void createQueueTables(Logger logger) {
        messageService.createQueueTableSpec("MSG_RAWQUEUETABLE", "RAW", false);
        logger.log(Level.INFO, "Created QueueTable MSG_RAWQUEUETABLE");
        messageService.createQueueTableSpec("MSG_RAWTOPICTABLE", "RAW", true);
        logger.log(Level.INFO, "Created QueueTable MSG_RAWQUEUETABLE");
    }

}
