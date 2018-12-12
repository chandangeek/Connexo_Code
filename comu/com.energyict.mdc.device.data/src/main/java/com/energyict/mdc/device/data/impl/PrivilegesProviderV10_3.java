/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.security.Privileges;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PrivilegesProviderV10_3 implements PrivilegesProvider {

    private final UserService userService;

    @Inject
    public PrivilegesProviderV10_3(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getModuleName() {
        return DeviceDataServices.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Collections.singletonList(
                this.userService.createModuleResourceWithPrivileges(
                        DeviceDataServices.COMPONENT_NAME,
                        Privileges.RESOURCE_DEVICES.getKey(),
                        Privileges.RESOURCE_DEVICES_DESCRIPTION.getKey(),
                        Arrays.asList(Privileges.Constants.ADMINISTER_VALIDATION_CONFIGURATION, Privileges.Constants.ADMINISTER_ESTIMATION_CONFIGURATION))
        );
    }
}
