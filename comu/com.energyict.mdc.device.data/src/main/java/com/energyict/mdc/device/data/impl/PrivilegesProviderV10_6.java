/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
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

import static com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTRATE_ZONE;
import static com.elster.jupiter.metering.security.Privileges.Constants.VIEW_ZONE;

public class PrivilegesProviderV10_6 implements PrivilegesProvider {
    private final UserService userService;

    @Inject
    public PrivilegesProviderV10_6(UserService userService) {
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
                        Privileges.RESOURCE_DEVICE_ZONES.getKey(),
                        Privileges.RESOURCE_DEVICE_ZONES_DESCRIPTION.getKey(),
                        Arrays.asList(ADMINISTRATE_ZONE, VIEW_ZONE)));
    }
}
