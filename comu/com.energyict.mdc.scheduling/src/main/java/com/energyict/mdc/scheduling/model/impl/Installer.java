/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.events.EventType;
import com.energyict.mdc.scheduling.security.Privileges;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents the Installer for the DeviceConfiguration module
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:42)
 */
class Installer implements FullInstaller, PrivilegesProvider {

    private final DataModel dataModel;
    private final EventService eventService;
    private final UserService userService;

    @Inject
    Installer(DataModel dataModel, EventService eventService, UserService userService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create event types for SCH",
                this::createEventTypes,
                logger
        );

        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return SchedulingService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(SchedulingService.COMPONENT_NAME, Privileges.RESOURCE_SCHEDULES.getKey(), Privileges.RESOURCE_SCHEDULES_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE, Privileges.Constants.VIEW_SHARED_COMMUNICATION_SCHEDULE)));
        return resources;
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            install(eventType);
        }
    }

    @TransactionRequired
    void install(EventType eventType) {
        if (!eventService.getEventType(eventType.topic()).isPresent()) {
            EventTypeBuilder eventTypeBuilder = this.eventService.buildEventTypeWithTopic(eventType.topic())
                    .name(eventType.name())
                    .component(SchedulingService.COMPONENT_NAME)
                    .category("Crud")
                    .scope("System");
            eventType.addCustomProperties(eventTypeBuilder);
            eventTypeBuilder.create();
        }
    }

}