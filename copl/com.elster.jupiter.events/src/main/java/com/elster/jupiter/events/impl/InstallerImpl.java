/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

import static com.elster.jupiter.events.EventService.JUPITER_EVENTS;

public class InstallerImpl implements FullInstaller {

    private static final int RETRY_DELAY = 60;

    private final DataModel dataModel;
    private final MessageService messageService;

    @Inject
    public InstallerImpl(DataModel dataModel, MessageService messageService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create and activate the \"" + JUPITER_EVENTS + "\" topic.",
                () -> createAndActivateJupiterEventsTopic(dataModelUpgrader),
                logger
        );
    }

    private void createAndActivateJupiterEventsTopic(DataModelUpgrader dataModelUpgrader) {
        DestinationSpec destinationSpec = getRawTopicTableSpec().createDestinationSpec(JUPITER_EVENTS, RETRY_DELAY);
        destinationSpec.activate();
    }

    private QueueTableSpec getRawTopicTableSpec() {
        return messageService.getQueueTableSpec("MSG_RAWTOPICTABLE").get();
    }
}
