/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.license.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.license.security.Privileges;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Installer implements FullInstaller, PrivilegesProvider {

    private final DataModel dataModel;
    private final EventService eventService;
    private final UserService userService;

    @Inject
    public Installer(DataModel dataModel, EventService eventService, UserService userService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create event types for LIC",
                this::createEventTypes,
                logger
        );
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return LicenseService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(LicenseService.COMPONENTNAME, Privileges.RESOURCE_LICENSE.getKey(), Privileges.RESOURCE_LICENSE_DESCRIPTION.getKey(),
                Arrays.asList(
                        Privileges.Constants.VIEW_LICENSE, Privileges.Constants.UPLOAD_LICENSE)));
        return resources;
    }

    private void createEventTypes() {
        List<com.elster.jupiter.events.EventType> eventTypesForComponent = eventService.getEventTypesForComponent(LicenseService.COMPONENTNAME);
        for (EventType eventType : EventType.values()) {
            if (!eventTypesForComponent.stream().anyMatch(et -> et.getName().equals(eventType.name()))) {
                eventType.install(eventService);
            }
        }
    }

}
