/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.device.config.events.EventType;

import javax.inject.Inject;
import java.util.EnumSet;

class UpgraderV10_4_1 implements Upgrader {
    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    UpgraderV10_4_1(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 1));
        installNewEventTypes();
    }

    private void installNewEventTypes() {
        EnumSet.of(
                EventType.SECURITY_ACCESSOR_TYPE_VALIDATE_DELETE,
                EventType.DEVICE_TYPE_PRE_DELETE
        ).forEach(eventType -> eventType.install(eventService));
    }
}
