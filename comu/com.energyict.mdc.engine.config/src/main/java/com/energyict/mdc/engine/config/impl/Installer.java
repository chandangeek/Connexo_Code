package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Takes the necessary steps to install the technical components of the engine configuration bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-26 (08:30)
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
            this.dataModel.install(executeDdl, true);
        }
        catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
        this.createEventTypes();
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.install(this.eventService);
            }
            catch (Exception e) {
                this.logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

}