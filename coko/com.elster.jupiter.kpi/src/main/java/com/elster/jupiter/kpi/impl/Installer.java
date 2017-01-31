/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

class Installer implements FullInstaller {

    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;
    private final IdsService idsService;

    @Inject
    Installer(DataModel dataModel, EventService eventService, IdsService idsService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.idsService = idsService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create KPI Vault",
                this::createVault,
                logger
        );
        doTry(
                "Create KPI Record Spec",
                this::createRecordSpec,
                logger
        );
        doTry(
                "Create event types for KPI",
                this::createEventTypes,
                logger
        );
    }

    private void createRecordSpec() {
        RecordSpec newRecordSpec = idsService.createRecordSpec(KpiService.COMPONENT_NAME, KpiServiceImpl.RECORD_SPEC_ID, "kpi")
                .addFieldSpec("value", FieldType.NUMBER)
                .addFieldSpec("target", FieldType.NUMBER)
                .create();
    }

    private void createVault() {
        Vault newVault = idsService.createVault(KpiService.COMPONENT_NAME, KpiServiceImpl.VAULT_ID, KpiService.COMPONENT_NAME, 2, 0, true);
        createPartitions(newVault);
    }

    private void logException(Exception e) {
        if (e instanceof RuntimeException) {
            logException((RuntimeException) e);
            return;
        }
        logException(new RuntimeException(e));
    }

    private void logException(RuntimeException e) {
        LOGGER.log(Level.SEVERE, e.getMessage() == null ? e.toString() : e.getMessage(), e);
        throw e;
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.install(eventService);
            } catch (RuntimeException e) {
                logException(e);
            }
        }
    }

    private void createPartitions(Vault vault) {
        Instant start = YearMonth.now().atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        vault.activate(start);
        vault.extendTo(start.plus(360, ChronoUnit.DAYS), Logger.getLogger(getClass().getPackage().getName()));
    }

}
