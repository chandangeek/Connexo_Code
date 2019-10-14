/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.tasks.security.Privileges;
import com.energyict.mdc.tasks.TaskService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

class Installer implements FullInstaller, PrivilegesProvider {

    private final DataModel dataModel;
    private final EventService eventService;
    private final TaskService taskService;
    private final UserService userService;

    @Inject
    Installer(DataModel dataModel, EventService eventService, TaskService taskService, UserService userService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.taskService = taskService;
        this.userService = userService;
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
        userService.addModulePrivileges(this);
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
        systemComTask.setName(TaskService.FIRMWARE_COMTASK_NAME);
        systemComTask.createFirmwareUpgradeTask();
        systemComTask.save();
    }

    @Override
    public String getModuleName() {
        return ServerTaskService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_COMMUNICATION_TASK_EXECUTION.getKey(), Privileges.RESOURCE_COMMUNICATION_TASK_EXECUTION_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_1,
                        Privileges.Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_2,
                        Privileges.Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_3,
                        Privileges.Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_4)));
        return resources;
    }

}
