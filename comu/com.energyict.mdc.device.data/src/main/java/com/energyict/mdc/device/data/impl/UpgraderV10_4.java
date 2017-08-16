/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.EnumSet;

class UpgraderV10_4 implements Upgrader {

    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    UpgraderV10_4(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4));
        installNewEventTypes();
    }

    private void installNewEventTypes() {
        EnumSet.of(EventType.CONNECTIONTASK_SETASCONNECTIONFUNCTION, EventType.CONNECTIONTASK_CLEARCONNECTIONFUNCTION).forEach(eventType -> eventType.install(eventService));
    }
}