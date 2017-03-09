/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.impl.slp.SyntheticLoadProfileServiceImpl;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

/**
 * Provides common functionality which can be used for clean install of 10.3 or for upgrade from 10.2 to 10.3
 */
public class InstallerV10_3Impl implements FullInstaller {

    private final DataModel dataModel;
    private final IdsService idsService;

    @Inject
    InstallerV10_3Impl(DataModel dataModel, IdsService idsService) {
        this.dataModel = dataModel;
        this.idsService = idsService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry(
                "Create SLP Vault",
                this::createVault,
                logger
        );
        doTry(
                "Create SLP Record Spec",
                this::createRecordSpec,
                logger
        );
    }

    private void createRecordSpec() {
        idsService.createRecordSpec(SyntheticLoadProfileService.COMPONENTNAME, SyntheticLoadProfileServiceImpl.RECORD_SPEC_ID, "slp")
                .addFieldSpec("value", FieldType.NUMBER)
                .create();
    }

    private void createVault() {
        Vault newVault = idsService.createVault(SyntheticLoadProfileService.COMPONENTNAME, SyntheticLoadProfileServiceImpl.VAULT_ID, SyntheticLoadProfileService.COMPONENTNAME, 1, 0, true);
        createPartitions(newVault);
    }

    private void createPartitions(Vault vault) {
        Instant start = YearMonth.now().atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        vault.activate(start);
        vault.extendTo(start.plus(360, ChronoUnit.DAYS), Logger.getLogger(getClass().getPackage().getName()));
    }
}
