/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_4 implements Upgrader {
    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    public UpgraderV10_4(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 4));
        createNewEventType();
    }

    private void createNewEventType() {
        EventType.FSM_UPDATED.install(eventService);
    }
}
