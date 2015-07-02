package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Takes the necessary steps to install the technical components of the finite state machine bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:57)
 */
public class Installer {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final UserService userService;
    private final EventService eventService;

    public Installer(DataModel dataModel, UserService userService, EventService eventService) {
        super();
        this.dataModel = dataModel;
        this.userService = userService;
        this.eventService = eventService;
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
            } catch (Exception e) {
                this.logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

}