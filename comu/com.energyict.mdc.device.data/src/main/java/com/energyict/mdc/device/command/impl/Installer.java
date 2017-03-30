/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.command.security.Privileges;

import javax.inject.Inject;
import java.util.logging.Logger;

public class Installer implements FullInstaller {

    private final DataModel dataModel;
    private final UserService userService;

    @Inject
    public Installer(DataModel dataModel, UserService userService) {
        super();
        this.dataModel = dataModel;
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry("Install command limitation rule privileges", this::cerateResources, logger);
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        createCommandRuleStats();
    }

    private void createCommandRuleStats() {
        CommandRuleStats commandRuleStats = dataModel.getInstance(CommandRuleStats.class);
        Save.CREATE.save(dataModel, commandRuleStats);
    }

    private void cerateResources() {
        PrivilegeCategory approveCategory = userService.findPrivilegeCategory(DualControlService.DUAL_CONTROL_APPROVE_CATEGORY)
                .orElseThrow(() -> new IllegalStateException("Dual control not installed yet"));
        userService.buildResource()
                .component(CommandRuleService.COMPONENT_NAME)
                .name(Privileges.COMMAND_LIMITATION_RULES.getKey())
                .description(Privileges.COMMAND_LIMITATION_RULES_DESCRIPTION.getKey())
                .addPrivilege(Privileges.APPROVE_COMMAND_LIMITATION_RULES.getKey()).in(approveCategory).add()
                .addPrivilege(Privileges.ADMINISTRATE_LIMITATION_RULES.getKey()).add()
                .addPrivilege(Privileges.VIEW_COMMAND_LIMITATION_RULES.getKey()).add()
                .create();

        Group dualControlApprover = userService.createGroup("Command limitation rule approver", "Can approve or reject changes on command limitation rules");

        dualControlApprover.grant("MDC", Privileges.APPROVE_COMMAND_LIMITATION_RULES.getKey());
    }
}
