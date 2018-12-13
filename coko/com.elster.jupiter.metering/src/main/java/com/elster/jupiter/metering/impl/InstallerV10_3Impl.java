/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StageSetBuilder;
import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.impl.aggregation.CalendarTimeSeriesCacheHandlerFactory;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.metering.impl.slp.SyntheticLoadProfileServiceImpl;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;

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

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final DataModel dataModel;
    private final IdsService idsService;
    private final MessageService messageService;
    private final FiniteStateMachineService stateMachineService;
    private final ServerMetrologyConfigurationService metrologyConfigurationService;

    @Inject
    InstallerV10_3Impl(DataModel dataModel,
                       IdsService idsService,
                       MessageService messageService,
                       FiniteStateMachineService stateMachineService,
                       ServerMetrologyConfigurationService metrologyConfigurationService) {
        this.dataModel = dataModel;
        this.idsService = idsService;
        this.messageService = messageService;
        this.stateMachineService = stateMachineService;
        this.metrologyConfigurationService = metrologyConfigurationService;
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
        doTry(
                "Create Queues",
                this::createQueues,
                logger
        );
        doTry(
                "Create Check Metrology Purpose",
                this::createNewPurposes,
                logger
        );
        doTry(
                "Install default stage sets",
                this::installDefaultStageSets,
                logger
        );

    }

    private void createRecordSpec() {
        idsService.createRecordSpec(SyntheticLoadProfileService.COMPONENTNAME, SyntheticLoadProfileServiceImpl.RECORD_SPEC_ID, "Synthetic Load Profile Interval Data")
                .addFieldSpec("Value", FieldType.NUMBER)
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

    private void createNewPurposes() {
        metrologyConfigurationService.createMetrologyPurpose(DefaultMetrologyPurpose.CHECK);
        metrologyConfigurationService.createMetrologyPurpose(DefaultMetrologyPurpose.MARKET);
    }

    public void createQueues() {
        this.createQueue(CalendarTimeSeriesCacheHandlerFactory.TASK_DESTINATION, DefaultTranslationKey.CALENDAR_TIMESERIES_CACHE_HANDLER_SUBSCRIBER);
    }

    private void createQueue(String queueDestination, TranslationKey queueSubscriber) {
        QueueTableSpec defaultQueueTableSpec = this.messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(queueDestination, DEFAULT_RETRY_DELAY_IN_SECONDS);
        destinationSpec.activate();
        destinationSpec.subscribe(queueSubscriber, MeteringDataModelService.COMPONENT_NAME, Layer.DOMAIN);
    }

}