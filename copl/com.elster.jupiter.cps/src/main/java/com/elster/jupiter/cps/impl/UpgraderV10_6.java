/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.EventType;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

class UpgraderV10_6 implements Upgrader {

    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    public UpgraderV10_6(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
       this.createEventTypes();
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.createIfNotExists(this.eventService);
        }
    }
}
