/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import com.energyict.mdc.sap.soap.webservices.security.Privileges;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrivilegesProviderV10_7_2 implements PrivilegesProvider {
    private final UserService userService;

    @Inject
    public PrivilegesProviderV10_7_2(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getModuleName() {
        return WebServiceActivator.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_SAP.getKey(), Privileges.RESOURCE_SAP_DESCRIPTION.getKey(),
                Collections.singletonList(Privileges.Constants.SEND_WEB_SERVICE_REQUEST)));
        return resources;
    }
}
