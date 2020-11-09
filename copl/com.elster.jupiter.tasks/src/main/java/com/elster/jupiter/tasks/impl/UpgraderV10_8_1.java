/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.tasks.security.Privileges;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class UpgraderV10_8_1 implements Upgrader, PrivilegesProvider {
    private final UserService userService;

    @Inject
    public UpgraderV10_8_1(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        if (!userService.getPrivilege(Privileges.Constants.EXECUTE_ADD_CERTIFICATE_REQUEST_DATA_TASK).isPresent()) {
            userService.addModulePrivileges(this);
        }
    }

    @Override
    public String getModuleName() {
        return TaskService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Collections.singletonList(
                userService.createModuleResourceWithPrivileges(TaskService.COMPONENTNAME,
                        Privileges.RESOURCE_TASKS.getKey(),
                        Privileges.RESOURCE_TASKS_DESCRIPTION.getKey(),
                        Collections.singletonList(
                                Privileges.Constants.EXECUTE_ADD_CERTIFICATE_REQUEST_DATA_TASK
                        )
                )
        );
    }
}
