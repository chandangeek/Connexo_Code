/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.energyict.mdc.masterdata.MasterDataService;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Represents the Installer for the MasterDataService module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:46)
 */
class Installer implements FullInstaller {

    private final DataModel dataModel;
    private final EventService eventService;
    private final MeteringService meteringService;
    private final MasterDataService masterDataService;

    @Inject
    Installer(DataModel dataModel, EventService eventService, MeteringService meteringService, MasterDataService masterDataService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.meteringService = meteringService;
        this.masterDataService = masterDataService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create event types for MDC master data",
                this::createEventTypes,
                logger
        );
        doTry(
                "Create default register types",
                this::createDefaults,
                logger
        );

    }

    private void createDefaults() {
        this.createRegisterTypes();
    }

    private void createRegisterTypes() {
        MasterDataGenerator.generateRegisterTypes(meteringService, masterDataService);
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(this.eventService);
        }
    }

}