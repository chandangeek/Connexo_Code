package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InstallerV10_3Impl implements PrivilegesProvider {

    private final UserService userService;

    @Inject
    public InstallerV10_3Impl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getModuleName() {
        return MeteringDataModelService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(
                userService.createModuleResourceWithPrivileges(
                        getModuleName(),
                        DefaultTranslationKey.RESOURCE_USAGE_POINT.getKey(),
                        DefaultTranslationKey.RESOURCE_USAGE_POINT_DESCRIPTION.getKey(),
                        Collections.singletonList(Privileges.Constants.MANAGE_USAGE_POINT_ATTRIBUTES)));
        return resources;
    }
}
