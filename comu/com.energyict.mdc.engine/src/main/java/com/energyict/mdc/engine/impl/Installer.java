package com.energyict.mdc.engine.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Installs the components of the mdc engine bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (14:08)
 */
public class Installer {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;
    private final UserService userService;

    public Installer(DataModel dataModel, EventService eventService, UserService userService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.userService = userService;
    }

    public void install(boolean executeDdl) {
        try {
            dataModel.install(executeDdl, true);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        createEventTypesIfNotExist();
        createComServerUser();
    }

    private void createComServerUser() {
        try {
            userService.createUser(EngineServiceImpl.COMSERVER_USER, EngineServiceImpl.COMSERVER_USER);
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