/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.device.lifecycle.config.EventType;

import javax.inject.Inject;


public class UpgraderV10_6 implements Upgrader {
    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    public UpgraderV10_6(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 6));
        EventType.TRANSITION_FAILED.install(eventService);
        EventType.DEVICE_LIFECYCLE_TRASITION_DELETE.install(eventService);
        EventType.TRANSITION_DONE.install(eventService);
    }
}
