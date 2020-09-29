/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.tasks.EventType;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.tasks.security.Privileges;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

class InstallerImpl implements FullInstaller, PrivilegesProvider {

    private final DataModel dataModel;
    private final UserService userService;
    private final EventService eventService;

    @Inject
    InstallerImpl(DataModel dataModel, UserService userService, EventService eventService) {
        this.dataModel = dataModel;
        this.userService = userService;
        this.eventService = eventService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create event types for MDC device life cycle",
                this::createEventTypes,
                logger
        );
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return TaskService.COMPONENTNAME;
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(this.eventService);
        }
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(TaskService.COMPONENTNAME, Privileges.RESOURCE_TASKS.getKey(), Privileges.RESOURCE_TASKS_DESCRIPTION.getKey(),
                Arrays.asList(
                        Privileges.Constants.VIEW_TASK_OVERVIEW,
                        Privileges.Constants.SUSPEND_TASK_OVERVIEW,
                        Privileges.Constants.ADMINISTER_TASK_OVERVIEW,
                        Privileges.Constants.EXECUTE_ADD_CERTIFICATE_REQUEST_DATA_TASK)));
        return resources;
    }
}
