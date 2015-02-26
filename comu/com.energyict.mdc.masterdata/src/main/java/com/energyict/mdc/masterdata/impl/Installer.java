package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the Installer for the MasterDataService module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:46)
 */
public class Installer {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;
    private final MeteringService meteringService;
    private final MasterDataService masterDataService;

    public Installer(DataModel dataModel, EventService eventService, MeteringService meteringService, MasterDataService masterDataService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.meteringService = meteringService;
        this.masterDataService = masterDataService;
    }

    public void install(boolean executeDdl, boolean createDefaults) {
        try {
            this.dataModel.install(executeDdl, true);
        }
        catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
        createEventTypes();
        if (createDefaults) {
            this.createDefaults();
        }
    }

    private void createDefaults() {
        this.createRegisterTypes();
    }

    private void createRegisterTypes() {
        MasterDataGenerator.generateRegisterTypes(meteringService, masterDataService);
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