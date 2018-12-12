/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.kore.api.security.Privileges;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

class PublicRestApplicationPrivilegesProvider implements PrivilegesProvider {

    private final UserService userService;

    @Inject
    public PublicRestApplicationPrivilegesProvider(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getModuleName() {
        return PublicRestAppServiceImpl.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Collections.singletonList(
                userService.createModuleResourceWithPrivileges(
                        getModuleName(),
                        Privileges.RESOURCE_PUBLIC_API.getKey(),
                        Privileges.RESOURCE_PUBLIC_API_DESCRIPTION.getKey(),
                        Collections.singletonList(Privileges.Constants.PUBLIC_REST_API)
                )
        );
    }
}
