package com.energyict.mdc.masterdata.impl;

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

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
    private final MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private final MasterDataService masterDataService;

    public Installer(DataModel dataModel, EventService eventService, MeteringService meteringService, MdcReadingTypeUtilService mdcReadingTypeUtilService, MasterDataService masterDataService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.meteringService = meteringService;
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
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
        this.createPhenomena();
        this.createRegisterTypes();
    }

    private void createRegisterTypes() {
        MasterDataGenerator.generateRegisterTypes(meteringService, mdcReadingTypeUtilService, masterDataService);
    }

    private void createPhenomena() {
        try {
            Phenomenon undefined = this.masterDataService.newPhenomenon("Undefined", Unit.getUndefined());
            undefined.save();
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
        this.generatePhenomenaFromStaticList();
    }

    private void generatePhenomenaFromStaticList() {
        MasterDataGenerator.generatePhenomena(masterDataService);
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