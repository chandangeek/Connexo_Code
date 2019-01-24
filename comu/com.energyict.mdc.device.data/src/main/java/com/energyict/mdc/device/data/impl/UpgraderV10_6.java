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

import javax.inject.Inject;

public class UpgraderV10_6 implements Upgrader {

    private final DataModel dataModel;
    private final EventService eventService;
    private final UserService userService;
    private final PrivilegesProviderV10_6 privilegesProviderV10_6;

    @Inject
    public UpgraderV10_6(DataModel dataModel, EventService eventService, UserService userService,
                         PrivilegesProviderV10_6 privilegesProviderV10_6) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.userService = userService;
        this.privilegesProviderV10_6 = privilegesProviderV10_6;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 6));
        userService.addModulePrivileges(privilegesProviderV10_6);
        // Validation for Device Configuration Change on data loggers and multi-elememt devices
        EventType.COMTASKEXECUTION_STARTED.createIfNotExists(eventService);
        EventType.COMTASKEXECUTION_COMPLETED.createIfNotExists(eventService);
        EventType.COMTASKEXECUTION_FAILED.createIfNotExists(eventService);
        EventType.SCHEDULED_COMTASKEXECUTION_STARTED.createIfNotExists(eventService);
        EventType.SCHEDULED_COMTASKEXECUTION_COMPLETED.createIfNotExists(eventService);
        EventType.SCHEDULED_COMTASKEXECUTION_FAILED.createIfNotExists(eventService);
    }
}