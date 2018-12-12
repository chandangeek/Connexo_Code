/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Created by bbl on 10/06/2016.
 */
public class UpgraderV10_2 implements Upgrader {

    private static final String COM_SERVER_INTERNAL_USER = "comServerInternal";
    private static final String COM_SERVER_INTERNAL_USER_GROUP = "ComServerResources";

    private UserService userService;

    @Inject
    public UpgraderV10_2(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        Optional<User> comServerInternalAccessAccount = userService.findUser(COM_SERVER_INTERNAL_USER);
        Optional<Group> comServerInternalGroup = userService.findGroup(COM_SERVER_INTERNAL_USER_GROUP);
        comServerInternalGroup.ifPresent(Group::delete);
        comServerInternalAccessAccount.ifPresent(User::delete);
    }
}
