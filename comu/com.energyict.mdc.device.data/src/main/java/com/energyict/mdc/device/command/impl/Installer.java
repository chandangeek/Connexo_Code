package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;


import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.command.security.Privileges;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class Installer implements FullInstaller, PrivilegesProvider {

    private final DataModel dataModel;
    private final UserService userService;

    @Inject
    public Installer(DataModel dataModel, UserService userService, EventService eventService) {
        super();
        this.dataModel = dataModel;
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        createCommandRuleStats();
        userService.addModulePrivileges(this);
    }

    private void createCommandRuleStats() {
        CommandRuleStats commandRuleStats = dataModel.getInstance(CommandRuleStats.class);
        Save.CREATE.save(dataModel, commandRuleStats);
    }

    @Override
    public String getModuleName() {
        return CommandRuleService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Collections.singletonList(
                this.userService.createModuleResourceWithPrivileges(CommandRuleService.COMPONENT_NAME,
                        Privileges.COMMAND_LIMITATION_RULES.getKey(),
                        Privileges.COMMAND_LIMITATION_RULES_DESCRIPTION.getKey(),
                        Arrays.asList(Privileges.Constants.ADMINISTRATE_COMMAND_LIMITATION_RULE, Privileges.Constants.VIEW_COMMAND_LIMITATION_RULE)
                ));
    }
}
