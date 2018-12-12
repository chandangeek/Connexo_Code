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

public class PrivilegesProviderV10_4_1 implements PrivilegesProvider {
    private final UserService userService;

    @Inject
    public PrivilegesProviderV10_4_1(UserService userService) {
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
                        Privileges.RESOURCE_CRL_REQUEST.getKey(),
                        Privileges.RESOURCE_CRL_REQUEST_DESCRIPTION.getKey(),
                        Arrays.asList(Privileges.Constants.VIEW_CRL_REQUEST, Privileges.Constants.ADMINISTER_CRL_REQUEST)));
    }
}
