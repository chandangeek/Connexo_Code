/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.security.Privileges;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Takes the necessary steps to install the technical components of the engine configuration bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-26 (08:30)
 */
class Installer implements FullInstaller, PrivilegesProvider {

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
        doTry(
                "Create event types for MDC",
                this::createEventTypes,
                logger
        );
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return EngineConfigurationService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(EngineConfigurationService.COMPONENT_NAME,
                Privileges.RESOURCE_COMMUNICATION.getKey(), Privileges.RESOURCE_COMMUNICATION_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION,
                        Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION,
                        Privileges.Constants.VIEW_STATUS_COMMUNICATION_INFRASTRUCTURE)));
        return resources;
    }


    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(this.eventService);
        }
    }

}