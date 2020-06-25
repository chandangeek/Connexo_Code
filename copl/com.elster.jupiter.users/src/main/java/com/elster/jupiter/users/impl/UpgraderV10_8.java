/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.Lists;

import javax.inject.Inject;
import java.util.List;

public class UpgraderV10_8 implements Upgrader {

    private final DataModel dataModel;
    private final UserService userService;

    @Inject
    public UpgraderV10_8(DataModel dataModel, UserService userService) {
        this.dataModel = dataModel;
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 8));
        final Group provisioningRole = createProvisioningRoleIfNotPresent();
        final Group userManagementRole = userService.findGroup(UserService.DEFAULT_ADMIN_ROLE).orElse(null);
        createProvisioningUserWithDefaultRolesIfNotPresent(Lists.newArrayList(provisioningRole, userManagementRole));
    }

    private Group createProvisioningRoleIfNotPresent() {
        return userService.findGroup(UserService.PROVISIONING_ROLE)
                .orElseGet(() -> userService.createGroup(UserService.PROVISIONING_ROLE, UserService.PROVISIONING_ROLE_DESCRIPTION));
    }

    private void createProvisioningUserWithDefaultRolesIfNotPresent(final List<Group> rolesByDefault) {
        final User provisioningUser = userService.findUser("provisioning")
                .orElseGet(() -> userService.createUser("provisioning", "System for User and Group provisining over SCIM 2.0 protocol"));
        rolesByDefault.forEach(provisioningUser::join);
    }

}
