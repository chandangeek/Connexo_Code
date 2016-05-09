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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Installs the components of the mdc engine bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (14:08)
 */
class Installer implements FullInstaller {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

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
    public void install(DataModelUpgrader dataModelUpgrader) {
        try {
            dataModelUpgrader.upgrade(dataModel, Version.latest());
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        createEventTypesIfNotExist();
        createComServerUser();
    }

    private void createComServerUser() {
        try {
            User comServerUser = userService.createUser(EngineServiceImpl.COMSERVER_USER, EngineServiceImpl.COMSERVER_USER);
            Optional<Group> batchExecutorRole = userService.findGroup(UserService.BATCH_EXECUTOR_ROLE);
            if (batchExecutorRole.isPresent()) {
                comServerUser.join(batchExecutorRole.get());
            } else {
                logger.log(Level.SEVERE, "Could not add role to '" + EngineServiceImpl.COMSERVER_USER + "' user because role '" + UserService.BATCH_EXECUTOR_ROLE + "' is not found");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void createEventTypesIfNotExist() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.createIfNotExists(this.eventService);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

}