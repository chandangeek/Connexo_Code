/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import com.energyict.mdc.device.lifecycle.config.EventType;

import javax.inject.Inject;

public class UpgraderV10_4 implements Upgrader {
    private final EventService eventService;

    @Inject
    public UpgraderV10_4(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        EventType.DEVICE_LIFECYCLE_UPDATE.install(eventService);
    }
}
