package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.config.security.Privileges;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

/**
 * Represents the Installer for the DeviceConfiguration module
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:42)
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
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
        createEventTypes();
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