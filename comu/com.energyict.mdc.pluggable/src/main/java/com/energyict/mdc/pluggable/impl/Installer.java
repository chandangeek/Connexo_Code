package com.energyict.mdc.pluggable.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Installs the pluggable bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (15:08)
 */
public class Installer {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;

    public Installer(DataModel dataModel, EventService eventService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    public void install(boolean executeDdl, boolean createMasterData) {
        try {
            this.dataModel.install(executeDdl, true);
            if (createMasterData) {
                this.createMasterData();
            }
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        createEventTypes();
    }

    private void createMasterData() {
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.install(this.eventService);
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

}