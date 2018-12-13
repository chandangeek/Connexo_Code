/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpecBuilder;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstallerImpl implements FullInstaller {

    private final IdsService idsService;
    private final DataModel dataModel;
    private final Clock clock;

    @Inject
    public InstallerImpl(DataModel dataModel, IdsService idsService, Clock clock) {
        this.idsService = idsService;
        this.dataModel = dataModel;
        this.clock = clock;
    }

    private static final int DEFAULT_SLOT_COUNT = 8;

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        createMasterData(logger);
    }

    private void createMasterData(Logger logger) {
        createVaults(logger);
        createRecordSpecs(logger);
    }

    private void createVaults(Logger logger) {
        Vault newVault = idsService.createVault(IdsService.COMPONENTNAME, 1, "Regular TimeSeries Default ", DEFAULT_SLOT_COUNT, 0, true);
        Instant start = Instant.now(clock).truncatedTo(ChronoUnit.DAYS);
        newVault.activate(start);
        newVault.extendTo(start.plus(360, ChronoUnit.DAYS), Logger.getLogger(getClass().getPackage().getName()));
        logger.log(Level.INFO, "Created Vault \"Regular TimeSeries Default \", with slot count " + DEFAULT_SLOT_COUNT + ".");
    }

    private void createRecordSpecs(Logger logger) {
        RecordSpecBuilder builder = idsService.createRecordSpec(IdsService.COMPONENTNAME, 1, "Simple");
        builder.addFieldSpec("value", FieldType.NUMBER);
        builder.create();
        logger.log(Level.INFO, "Created \"Simple\" Record Spec.");
    }

}
