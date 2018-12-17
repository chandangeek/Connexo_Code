/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.scheduling.model.ComSchedule;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

class UpgraderV10_6 implements Upgrader {

    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    UpgraderV10_6(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 6));
        // Validation for Device Configuration Change on data loggers and multi-elememt devices
        EventType.TOU_COMTASKEXECUTION_STARTED.createIfNotExists(eventService);
        EventType.TOU_COMTASKEXECUTION_COMPLETED.createIfNotExists(eventService);
        EventType.TOU_COMTASKEXECUTION_FAILED.createIfNotExists(eventService);
        EventType.VERIFICATION_STARTED.createIfNotExists(eventService);
        EventType.VERIFICATION_COMPLETED.createIfNotExists(eventService);
        EventType.VERIFICATION_FAILED.createIfNotExists(eventService);
    }
}
