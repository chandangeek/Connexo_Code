/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataModelService;
import com.elster.jupiter.mdm.usagepoint.data.security.Privileges;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

class PrivilegesProviderV10_3 implements PrivilegesProvider {
    private final UserService userService;

    @Inject
    public PrivilegesProviderV10_3(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getModuleName() {
        return UsagePointDataModelService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Arrays.asList(
                userService.createModuleResourceWithPrivileges(
                        getModuleName(),
                        Privileges.RESOURCE_USAGE_POINT_GROUPS.getKey(),
                        Privileges.RESOURCE_USAGE_POINT_GROUPS_DESCRIPTION.getKey(),
                        Arrays.asList(
                                Privileges.Constants.ADMINISTER_USAGE_POINT_GROUP,
                                Privileges.Constants.ADMINISTER_USAGE_POINT_ENUMERATED_GROUP,
                                Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL)),
                userService.createModuleResourceWithPrivileges(
                        MeteringDataModelService.COMPONENT_NAME,
                        "metering.usagePoint",
                        "metering.usagePoint.description",
                        Arrays.asList(
                                Privileges.Constants.ADMINISTER_VALIDATION_CONFIGURATION,
                                Privileges.Constants.ADMINISTER_ESTIMATION_CONFIGURATION))
        );
    }
}
