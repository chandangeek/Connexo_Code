/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataModelService;
import com.elster.jupiter.mdm.usagepoint.data.security.Privileges;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class UsagePointGroupPrivilegesProvider implements PrivilegesProvider {
    private final UserService userService;

    @Inject
    public UsagePointGroupPrivilegesProvider(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getModuleName() {
        return UsagePointDataModelService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Collections.singletonList(
                userService.createModuleResourceWithPrivileges(getModuleName(),
                        Privileges.RESOURCE_USAGE_POINT_GROUPS.getKey(),
                        Privileges.RESOURCE_USAGE_POINT_GROUPS_DESCRIPTION.getKey(),
                        Arrays.asList(
                                Privileges.Constants.ADMINISTER_USAGE_POINT_GROUP,
                                Privileges.Constants.ADMINISTER_USAGE_POINT_ENUMERATED_GROUP,
                                Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL))
        );
    }
}
