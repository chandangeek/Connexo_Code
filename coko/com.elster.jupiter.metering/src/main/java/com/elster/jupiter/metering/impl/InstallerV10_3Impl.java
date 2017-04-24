/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StageSetBuilder;
import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.slp.SyntheticLoadProfileServiceImpl;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;

import javax.inject.Inject;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Provides common functionality which can be used for clean install of 10.3 or for upgrade from 10.2 to 10.3
 */
public class InstallerV10_3Impl implements FullInstaller {

    private final DataModel dataModel;
    private final IdsService idsService;
    private final FiniteStateMachineService stateMachineService;

    @Inject
    InstallerV10_3Impl(DataModel dataModel, IdsService idsService, FiniteStateMachineService stateMachineService) {
        this.dataModel = dataModel;
        this.idsService = idsService;
        this.stateMachineService = stateMachineService;
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

    public void installDefaultStageSets() {
        installEndDeviceStageSet();
    }

    private void installEndDeviceStageSet() {
        StageSetBuilder stageSetBuilder = stateMachineService.newStageSet(MeteringService.END_DEVICE_STAGE_SET_NAME);
        Stream.of(EndDeviceStage.values())
                .forEach(endDeviceStage -> stageSetBuilder.stage(endDeviceStage.getKey()));
        stageSetBuilder.add();
    }
}
