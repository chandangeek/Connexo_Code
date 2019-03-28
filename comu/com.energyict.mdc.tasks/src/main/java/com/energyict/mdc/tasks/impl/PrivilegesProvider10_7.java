/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.security.Privileges;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.energyict.mdc.tasks.security.Privileges.Constants.EXECUTE_COM_TASK_1;
import static com.energyict.mdc.tasks.security.Privileges.Constants.EXECUTE_COM_TASK_2;
import static com.energyict.mdc.tasks.security.Privileges.Constants.EXECUTE_COM_TASK_3;
import static com.energyict.mdc.tasks.security.Privileges.Constants.EXECUTE_COM_TASK_4;

public class PrivilegesProvider10_7 implements PrivilegesProvider {

    private final UserService userService;

    @Inject
    public PrivilegesProvider10_7(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getModuleName() {
        return TaskService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Collections.singletonList(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_COMMUNICATION_TASK_EXECUTION.getKey(),
                Privileges.RESOURCE_COMMUNICATION_TASK_EXECUTION_DESCRIPTION.getKey(),
                Arrays.asList(EXECUTE_COM_TASK_1, EXECUTE_COM_TASK_2, EXECUTE_COM_TASK_3, EXECUTE_COM_TASK_4)));
    }
}
