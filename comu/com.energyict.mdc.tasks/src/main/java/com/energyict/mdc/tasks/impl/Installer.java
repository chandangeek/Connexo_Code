/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import com.energyict.mdc.tasks.TaskService;

import javax.inject.Inject;

import java.util.logging.Logger;

class Installer implements FullInstaller {

    private final DataModel dataModel;
    private final EventService eventService;
    private final TaskService taskService;
    private final UpgraderV10_7 upgraderV10_7;

    @Inject
    Installer(DataModel dataModel, EventService eventService, TaskService taskService,
            UpgraderV10_7 upgraderV10_7) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.taskService = taskService;
        this.upgraderV10_7 = upgraderV10_7;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create event types for CTS",
                this::createEventTypes,
                logger
        );
        doTry(
                "Create Firmware Com Task",
                this::createFirmwareComTaskIfNotPresentYet,
                logger
        );
        doTry(
                "Create Firmware Com Task",
                () -> upgraderV10_7.migrate(dataModelUpgrader),
                logger
        );
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.createIfNotExists(this.eventService);
        }
    }

    private void createFirmwareComTaskIfNotPresentYet() {
        if (!taskService.findFirmwareComTask().isPresent()) {
            createFirmwareComTask();
        }
    }

    private void createFirmwareComTask() {
        SystemComTask systemComTask = dataModel.getInstance(SystemComTask.class);
        systemComTask.setName(ServerTaskService.FIRMWARE_COMTASK_NAME);
        systemComTask.createFirmwareUpgradeTask();
        systemComTask.save();
    }

}
