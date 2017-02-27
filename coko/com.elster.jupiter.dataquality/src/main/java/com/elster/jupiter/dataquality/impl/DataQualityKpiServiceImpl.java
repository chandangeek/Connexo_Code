/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.dataquality.DataQualityKpi;
import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.DeviceDataQualityKpi;
import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;
import com.elster.jupiter.dataquality.security.Privileges;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;

@Component(
        name = "com.elster.jupiter.dataquality.kpi",
        service = {DataQualityKpiService.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        immediate = true
)
public class DataQualityKpiServiceImpl implements ServerDataQualityKpiService, MessageSeedProvider, TranslationKeyProvider {

    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile ValidationService validationService;
    private volatile EstimationService estimationService;
    private volatile TransactionService transactionService;
    private volatile MessageService messageService;
    private volatile UserService userService;
    private volatile TaskService taskService;
    private volatile MeteringService meteringService;
    private volatile KpiService kpiService;
    private volatile Thesaurus thesaurus;
    private volatile Clock clock;
    private volatile AppService appService;

    public DataQualityKpiServiceImpl() {
    }

    @Inject
    public DataQualityKpiServiceImpl(OrmService ormService, UpgradeService upgradeService, ValidationService validationService, EstimationService estimationService,
                                     TransactionService transactionService, MessageService messageService, UserService userService, TaskService taskService,
                                     MeteringService meteringService, KpiService kpiService, NlsService nlsService, Clock clock, AppService appService) {
        this();
        setOrmService(ormService);
        setUpgradeService(upgradeService);
        setValidationService(validationService);
        setEstimationService(estimationService);
        setTransactionService(transactionService);
        setMessageService(messageService);
        setUserService(userService);
        setTaskService(taskService);
        setMeteringService(meteringService);
        setKpiService(kpiService);
        setThesaurus(nlsService);
        setClock(clock);
        setAppService(appService);

        activate();
    }

    @Activate
    public final void activate() {
        dataModel.register(this.getModule());
        upgradeService.register(identifier("Pulse", DataQualityKpiService.COMPONENTNAME), dataModel, Installer.class, Collections.emptyMap());
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(DataQualityKpiService.class).toInstance(DataQualityKpiServiceImpl.this);
                bind(MessageService.class).toInstance(messageService);
                bind(UserService.class).toInstance(userService);
                bind(TaskService.class).toInstance(taskService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(KpiService.class).toInstance(kpiService);
                bind(ValidationService.class).toInstance(validationService);
                bind(EstimationService.class).toInstance(estimationService);
                bind(TransactionService.class).toInstance(transactionService);
                bind(Clock.class).toInstance(clock);
                bind(AppService.class).toInstance(appService);
            }
        };
    }

    @Override
    public DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public String getComponentName() {
        return DataQualityKpiService.COMPONENTNAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        keys.addAll(Arrays.asList(TranslationKeys.values()));
        keys.addAll(Arrays.asList(Privileges.values()));
        return keys;
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(DataQualityKpiService.COMPONENTNAME, "Data quality kpi");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(this.dataModel);
        }
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setThesaurus(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setKpiService(KpiService kpiService) {
        this.kpiService = kpiService;
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Override
    public DeviceDataQualityKpi newDataQualityKpi(EndDeviceGroup endDeviceGroup, TemporalAmount calculationFrequency) {
        DeviceDataQualityKpiImpl kpi = dataModel.getInstance(DeviceDataQualityKpiImpl.class).init(endDeviceGroup, calculationFrequency);
        kpi.save();
        return kpi;
    }

    @Override
    public UsagePointDataQualityKpi newDataQualityKpi(UsagePointGroup usagePointGroup, MetrologyPurpose metrologyPurpose, TemporalAmount calculationFrequency) {
        UsagePointDataQualityKpiImpl kpi = dataModel.getInstance(UsagePointDataQualityKpiImpl.class).init(usagePointGroup, metrologyPurpose, calculationFrequency);
        kpi.save();
        return kpi;
    }

    @Override
    public DeviceDataQualityKpiFinder deviceDataQualityKpiFinder() {
        return dataModel.getInstance(DeviceDataQualityKpiFinderImpl.class);
    }

    @Override
    public UsagePointDataQualityKpiFinder usagePointDataQualityKpiFinder() {
        return dataModel.getInstance(UsagePointDataQualityKpiFinderImpl.class);
    }

    @Override
    public Optional<DeviceDataQualityKpi> findDeviceDataQualityKpi(long id) {
        return dataModel.mapper(DeviceDataQualityKpi.class).getOptional(id);
    }

    @Override
    public Optional<UsagePointDataQualityKpi> findUsagePointDataQualityKpi(long id) {
        return dataModel.mapper(UsagePointDataQualityKpi.class).getOptional(id);
    }

    @Override
    public Optional<? extends DataQualityKpi> findDataQualityKpi(long id) {
        return dataModel.mapper(DataQualityKpi.class).getOptional(id);
    }

    @Override
    public Optional<? extends DataQualityKpi> findAndLockDataQualityKpiByIdAndVersion(long id, long version) {
        return dataModel.mapper(DataQualityKpi.class).lockObjectIfVersion(version, id);
    }
}
