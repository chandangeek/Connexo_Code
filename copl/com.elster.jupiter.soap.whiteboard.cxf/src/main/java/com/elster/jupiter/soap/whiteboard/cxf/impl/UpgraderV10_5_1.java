/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_5_1 implements Upgrader {
    public static final Version VERSION = Version.version(10, 5, 1);
    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    public UpgraderV10_5_1(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
        EventType.ENDPOINT_CONFIGURATION_VALIDATE_DELETE.installIfNotPresent(eventService);
    }
}
