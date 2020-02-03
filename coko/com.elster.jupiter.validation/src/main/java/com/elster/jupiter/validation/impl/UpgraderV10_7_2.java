/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.validation.EventType;
import com.google.inject.Inject;

public class UpgraderV10_7_2 implements Upgrader {
    private final DataModel dataModel;
    private final MessageService messageService;
    private final InstallerImpl installer;
    private final EventService eventService;

    @Inject
    UpgraderV10_7_2(DataModel dataModel, MessageService messageService, InstallerImpl installer, EventService eventService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.installer = installer;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7, 2));
        addNewEventsForMessageService();
    }

    private void addNewEventsForMessageService() {
        EventType.SUSPECT_VALUE_CREATED.install(eventService);
    }
}
