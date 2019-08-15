/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.security.Privileges;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION;
import static com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION;
import static com.energyict.mdc.device.data.security.Privileges.Constants.RUN_WITH_PRIO;

public class PrivilegesProviderV10_6_1 implements PrivilegesProvider {
    private final UserService userService;

    @Inject
    public PrivilegesProviderV10_6_1(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getModuleName() {
        return DeviceDataServices.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Arrays.asList(
                this.userService.createModuleResourceWithPrivileges(
                        DeviceDataServices.COMPONENT_NAME,
                        Privileges.RESOURCE_DEVICE_COMMUNICATIONS.getKey(),
                        Privileges.RESOURCE_DEVICE_COMMUNICATIONS_DESCRIPTION.getKey(),
                        Arrays.asList(ADMINISTRATE_DEVICE_COMMUNICATION, OPERATE_DEVICE_COMMUNICATION, RUN_WITH_PRIO)));
    }
}
