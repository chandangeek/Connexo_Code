/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.security.Privileges;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by bvn on 5/3/16.
 */
public class Installer implements FullInstaller, PrivilegesProvider {
    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;
    private final UserService userService;

    @Inject
    public Installer(DataModel dataModel, EventService eventService, UserService userService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.userService = userService;
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            if (!this.eventService.getEventType(eventType.topic()).isPresent()) {
                this.eventService.buildEventTypeWithTopic(eventType.topic())
                        .name(eventType.name())
                        .component(WebServicesService.COMPONENT_NAME)
                        .category("Crud")
                        .scope("System").create();
                //                            .shouldPublish();
            }
        }
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry("Create event types", this::createEventTypes, logger);
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return WebServicesService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_WEB_SERVICES.getKey(), Privileges.RESOURCE_WEB_SERVICES_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.VIEW_WEB_SERVICES, Privileges.Constants.ADMINISTRATE_WEB_SERVICES)));
        return resources;
    }
}
