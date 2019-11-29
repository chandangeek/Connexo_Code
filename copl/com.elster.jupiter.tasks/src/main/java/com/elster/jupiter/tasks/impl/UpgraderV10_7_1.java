/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.tasks.security.Privileges;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class UpgraderV10_7_1 implements Upgrader, PrivilegesProvider {
    private final DataModel dataModel;
    private final UserService userService;

    @Inject
    public UpgraderV10_7_1(DataModel dataModel, UserService userService) {
        this.dataModel = dataModel;
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return TaskService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(TaskService.COMPONENTNAME, Privileges.RESOURCE_TASKS.getKey(), Privileges.RESOURCE_TASKS_DESCRIPTION.getKey(),
                Collections.singletonList(
                        Privileges.Constants.SUSPEND_TASK_OVERVIEW)));
        return resources;
    }
}
