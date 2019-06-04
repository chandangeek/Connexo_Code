/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.security;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static com.elster.jupiter.issue.security.Privileges.Constants.CREATE_ISSUE;

public class PrivilegesProviderV10_7 implements PrivilegesProvider {
    private final UserService userService;

    @Inject
    public PrivilegesProviderV10_7(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getModuleName() {
        return IssueService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Arrays.asList(
                this.userService.createModuleResourceWithPrivileges(
                        IssueService.COMPONENT_NAME,
                        Privileges.RESOURCE_ISSUES.getKey(),
                        Privileges.RESOURCE_ISSUES_DESCRIPTION.getKey(),
                        Arrays.asList(CREATE_ISSUE)));
    }
}
