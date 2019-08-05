package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.security.Privileges;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrivilegesProviderV10_7 implements PrivilegesProvider {
    private final UserService userService;

    @Inject
    public PrivilegesProviderV10_7(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getModuleName() {
        return WebServicesService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_WEB_SERVICES.getKey(), Privileges.RESOURCE_WEB_SERVICES_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.VIEW_HISTORY_WEB_SERVICES,
                        Privileges.Constants.RETRY_WEB_SERVICES)));
        return resources;
    }
}
