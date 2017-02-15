/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Installs the components of the mdc engine bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (14:08)
 */
class Installer implements FullInstaller {

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
        Stream.of(EventType.DEVICE_CONNECTION_COMPLETION.topic(), EventType.DEVICE_CONNECTION_FAILURE.topic())
                .map(eventService::getEventType)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(eventType -> {
                    eventType.setPublish(true);
                    eventType.update();
                });
    }

}