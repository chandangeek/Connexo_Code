/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.EnumSet;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_3 implements Upgrader {

    static final Version VERSION = version(10, 3);
    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    public UpgraderV10_3(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
        installNewEventTypes();
    }

    private void installNewEventTypes() {
        EnumSet.of(EventType.USAGEPOINTGROUP_VALIDATE_DELETED)
                .forEach(eventType -> eventType.install(eventService));
    }
}

