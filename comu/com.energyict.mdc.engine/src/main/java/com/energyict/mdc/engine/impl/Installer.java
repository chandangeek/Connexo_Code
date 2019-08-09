/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.*;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.security.Privileges;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Installs the components of the mdc engine bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (14:08)
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
                "Create event types for CES",
                this::createEventTypesIfNotExist,
                logger
        );
        doTry(
                "Create ComServer user",
                this::createComServerUser,
                logger
        );
        doTry(
                "Publish events",
                this::publishEvents,
                logger
        );
        userService.addModulePrivileges(this);
    }

    private void createComServerUser() {
        User comServerUser = userService.createUser(EngineServiceImpl.COMSERVER_USER, EngineServiceImpl.COMSERVER_USER);
        Optional<Group> batchExecutorRole = userService.findGroup(UserService.BATCH_EXECUTOR_ROLE);
        comServerUser.join(batchExecutorRole.orElseThrow(() -> new IllegalStateException("Could not find batch executor role.")));
    }

    private void createEventTypesIfNotExist() {
        for (EventType eventType : EventType.values()) {
            eventType.createIfNotExists(this.eventService);
        }
    }

    private void publishEvents() {
        Stream.of(EventType.DEVICE_CONNECTION_COMPLETION.topic(),
                EventType.DEVICE_CONNECTION_FAILURE.topic(),
                EventType.UNKNOWN_INBOUND_DEVICE.topic(),
                EventType.UNKNOWN_SLAVE_DEVICE.topic())
                .map(eventService::getEventType)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(eventType -> {
                    eventType.setPublish(true);
                    eventType.update();
                });
    }

    @Override
    public String getModuleName() {
        return EngineService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_MOBILE_COMSERVER.getKey(), Privileges.RESOURCE_MOBILE_COMSERVER_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.OPERATE_MOBILE_COMSERVER)));
        return resources;
    }
}