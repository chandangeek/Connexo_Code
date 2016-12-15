package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.dualcontrol.Privileges;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.logging.Logger;

class Installer implements FullInstaller {

    private final DataModel dataModel;
    private final UserService userService;

    @Inject
    Installer(DataModel dataModel, UserService userService) {
        this.dataModel = dataModel;
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Add dual control privileges",
                this::createResource,
                logger
        );
    }

    public Void createResource() {
        PrivilegeCategory dualControlApprove = userService.createPrivilegeCategory("DualControlApprove");
        PrivilegeCategory dualControlGrant = userService.createPrivilegeCategory("DualControlGrant");

        Resource resource = userService.buildResource()
                .component(DualControlService.COMPONENT_NAME)
                .name(Privileges.RESOURCE_TOU_CALENDARS.getKey())
                .description(Privileges.RESOURCE_TOU_CALENDARS_DESCRIPTION.getKey())
                .addGrantPrivilege(Privileges.GRANT_DUAL_CONTROL_APPROVAL.getKey())
                .in(dualControlGrant)
                .forCategory(dualControlApprove)
                .add()
                .create();

        Group dualControlAdministrator = userService.createGroup("Dual control administrator", "Dual control administrative privileges");

        dualControlAdministrator.grant("SYS", Privileges.GRANT_DUAL_CONTROL_APPROVAL.getKey());

        return null;

    }
}
