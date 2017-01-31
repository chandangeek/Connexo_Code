/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

class Installer implements FullInstaller {

    private final DataModel dataModel;
    private final EventService eventService;
    private final TaskService taskService;

    @Inject
    Installer(DataModel dataModel, EventService eventService, TaskService taskService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.taskService = taskService;
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
                "Create Status Information Com Task",
                this::createStatusInformationComTaskIfNotPresentYet,
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

    private void createStatusInformationComTaskIfNotPresentYet() {
        if (!findStatusInformationComTask().isPresent()) {
            createStatusInformationComTask();
        }
    }

    private Optional<ComTask> findStatusInformationComTask() {
        List<ComTask> comTasks = dataModel.mapper(ComTask.class)
                .find("name", ServerTaskService.STATUS_INFORMATION_COMTASK_NAME);
        return comTasks.size() == 1 ? Optional.of(comTasks.get(0)) : Optional.empty();
    }

    private void createStatusInformationComTask() {
        ComTask comTask = dataModel.getInstance(ComTask.class);
        comTask.setName(ServerTaskService.STATUS_INFORMATION_COMTASK_NAME);
        comTask.createStatusInformationTask();
        comTask.save();
    }

}
