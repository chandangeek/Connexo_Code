/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.device.lifecycle.config.EventType;

import javax.inject.Inject;


public class UpgraderV10_6 implements Upgrader {
    private final EventService eventService;

    @Inject
    public UpgraderV10_6(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        EventType.TRANSITION_FAILED.install(eventService);
        EventType.DEVICE_LIFECYCLE_TRASITION_DELETE.install(eventService);
    }


}
