/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.config.security.Privileges;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

/**
 * Represents the Installer for the DeviceConfiguration module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:42)
 */
public class Installer implements FullInstaller, PrivilegesProvider {

    private final DataModel dataModel;
    private final EventService eventService;
    private final UserService userService;

    @Inject
    public Installer(DataModel dataModel, EventService eventService, UserService userService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        createEventTypes();
        userService.addModulePrivileges(this);
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(this.eventService);
        }
    }

    @Override
    public String getModuleName() {
        return DeviceConfigurationService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Arrays.asList(
                this.userService.createModuleResourceWithPrivileges(DeviceConfigurationService.COMPONENTNAME, Privileges.RESOURCE_MASTER_DATA
                        .getKey(), Privileges.RESOURCE_MASTER_DATA_DESCRIPTION.getKey(), Arrays.asList(Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA)),
                this.userService.createModuleResourceWithPrivileges(DeviceConfigurationService.COMPONENTNAME, Privileges.RESOURCE_DEVICE_TYPES
                        .getKey(), Privileges.RESOURCE_DEVICE_TYPES_DESCRIPTION.getKey(), Arrays.asList(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE)),
                this.userService.createModuleResourceWithPrivileges(DeviceConfigurationService.COMPONENTNAME, Privileges.RESOURCE_DEVICE_SECURITY
                        .getKey(), Privileges.RESOURCE_DEVICE_SECURITY_DESCRIPTION.getKey(), Arrays.asList(DeviceSecurityUserAction
                        .values()).stream().map(DeviceSecurityUserAction::getPrivilege).collect(toList())),
                this.userService.createModuleResourceWithPrivileges(DeviceConfigurationService.COMPONENTNAME, Privileges.RESOURCE_DEVICE_COMMANDS
                        .getKey(), Privileges.RESOURCE_DEVICE_COMMANDS_DESCRIPTION.getKey(), Arrays.asList(DeviceMessageUserAction
                        .values()).stream().map(DeviceMessageUserAction::getPrivilege).collect(toList()))
        );
    }


}