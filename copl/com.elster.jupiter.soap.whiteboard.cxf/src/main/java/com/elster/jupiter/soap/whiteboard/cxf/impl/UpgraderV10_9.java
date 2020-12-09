/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.security.Privileges;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

public class UpgraderV10_9 implements Upgrader {
    public static final Version VERSION = Version.version(10, 9);
    private final UserService userService;

    @Inject
    public UpgraderV10_9(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        userService.saveResourceWithPrivileges(WebServicesService.COMPONENT_NAME,
                Privileges.RESOURCE_WEB_SERVICES.getKey(), Privileges.RESOURCE_WEB_SERVICES_DESCRIPTION.getKey(),
                new String[]{Privileges.Constants.CANCEL_WEB_SERVICES});
    }
}