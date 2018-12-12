/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PrivilegesProviderV10_4 implements PrivilegesProvider {
    private final UserService userService;

    @Inject
    PrivilegesProviderV10_4(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getModuleName() {
        return SecurityManagementService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Collections.singletonList(
                userService.createModuleResourceWithPrivileges(SecurityManagementService.COMPONENTNAME,
                        Privileges.RESOURCE_SECURITY_ACCESSOR_MANAGEMENT.getKey(),
                        Privileges.RESOURCE_SECURITY_ACCESSOR_MANAGEMENT_DESCRIPTION.getKey(),
                        Arrays.asList(
                                Privileges.Constants.VIEW_SECURITY_ACCESSORS,
                                Privileges.Constants.EDIT_SECURITY_ACCESSORS
                        )
                )
        );
    }

    void install() {
        userService.addModulePrivileges(this);
    }
}
