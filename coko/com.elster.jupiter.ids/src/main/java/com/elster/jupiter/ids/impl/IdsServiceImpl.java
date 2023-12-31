/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.RecordSpecBuilder;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.ResultWrapper;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;

@Component(name = "com.elster.jupiter.ids", service = {IdsService.class, MessageSeedProvider.class}, property = "name=" + IdsService.COMPONENTNAME)
public class IdsServiceImpl implements IdsService, MessageSeedProvider {

    private volatile DataModel dataModel;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UpgradeService upgradeService;

    public IdsServiceImpl() {
    }

    @Inject
    public IdsServiceImpl(Clock clock, OrmService ormService, NlsService nlsService, ThreadPrincipalService threadPrincipalService, UpgradeService upgradeService) {
        this();
        setClock(clock);
        setOrmService(ormService);
        setNlsService(nlsService);
        setThreadPrincipalService(threadPrincipalService);
        setUpgradeService(upgradeService);
        activate();
    }

    @Override
    public Optional<Vault> getVault(String component, long id) {
        return dataModel.mapper(Vault.class).getOptional(component, id);
    }

    @Override
    public Optional<RecordSpec> getRecordSpec(String component, long id) {
        return dataModel.mapper(RecordSpec.class).getOptional(component, id);
    }

    @Override
    public Optional<TimeSeries> getTimeSeries(long id) {
        return dataModel.mapper(TimeSeries.class).getOptional(id);
    }

    @Override
    public TimeSeriesDataStorer createOverrulingStorer() {
        return TimeSeriesDataStorerImpl.createOverrulingStorer(dataModel, clock, thesaurus);
    }

    @Override
    public TimeSeriesDataStorer createUpdatingStorer() {
        return TimeSeriesDataStorerImpl.createUpdatingStorer(dataModel, clock, thesaurus);
    }

    @Override
    public TimeSeriesDataStorer createNonOverrulingStorer() {
        return TimeSeriesDataStorerImpl.createNonOverrulingStorer(dataModel, clock, thesaurus);
    }

    @Override
    public Vault createVault(String component, long id, String name, int slotCount, int textSlotCount, boolean regular) {
        VaultImpl vault = dataModel.getInstance(VaultImpl.class).init(component, id, name, slotCount, textSlotCount, regular);
        vault.persist();
        return vault;
    }

    @Override
    public RecordSpecBuilder createRecordSpec(String component, long id, String name) {
        return new RecordSpecBuilderImpl(dataModel, component, id, name);
    }

    @Override
    public void purge(Logger logger) {
        getVaults().stream()
                .filter(Vault::isActive)
                .forEach(vault -> vault.purge(logger));
    }

    @Override
    public void extendTo(Instant instant, Logger logger) {
        this.extendTo(instant, logger, false);
    }

    @Override
    public ResultWrapper<String> extendTo(Instant instant, Logger logger, boolean dryRun) {
        ResultWrapper<String> result = new ResultWrapper();
        getVaults().stream()
                .filter(Vault::isActive)
                .forEach(vault -> {
                    try {
                        logger.log(Level.INFO, "Extending vault '" + vault.toString() + "', component '" + vault.getComponentName() + "' up to " + instant + "...");
                        vault.extendTo(instant, dryRun, logger);
                    } catch (Exception ex) {
                        if (vault.isPartitioned()) {
                            result.addFailedObject(vault.getTableName());
                            if (vault.hasJournal()) {
                                result.addFailedObject(vault.getJournalTableName());
                            }
                        }
                        logger.log(Level.SEVERE, "Failed to extend vault '" + vault.toString() + "', up to " + instant + ". Exception: " + ex.getLocalizedMessage() + ".");
                        ex.printStackTrace();
                    }
                });
        return result;
    }

    @Override
    public List<TimeSeriesEntry> getEntries(List<Pair<TimeSeries, Instant>> scope) {
        return scope.stream()
                .collect(Collectors.groupingBy(timeSeriesAndInstant -> timeSeriesAndInstant.getFirst().getVault()))
                .entrySet()
                .stream()
                .map(vaultWithTimeSeriesAndInstants -> ((IVault) vaultWithTimeSeriesAndInstants.getKey()).getEntries(vaultWithTimeSeriesAndInstants.getValue()))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    List<VaultImpl> getVaults() {
        return dataModel.stream(VaultImpl.class).select();
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "TimeSeries Data Store");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public final void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(getComponentName(), getLayer());
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Activate
    public final void activate() {
        dataModel.register(getModule());

        upgradeService.register(
                identifier("Pulse", COMPONENTNAME),
                dataModel,
                InstallerImpl.class,
                ImmutableMap.<Version, Class<? extends Upgrader>>builder()
                        .put(version(10, 4, 24), UpgraderV10_4_24.class)
                        .put(version(10, 9), UpgraderV10_9.class)
                        .build());
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                this.bind(DataModel.class).toInstance(dataModel);
                this.bind(IdsService.class).toInstance(IdsServiceImpl.this);
                this.bind(Clock.class).toInstance(clock);
                this.bind(Thesaurus.class).toInstance(thesaurus);
                this.bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
            }
        };
    }

    private String getComponentName() {
        return COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }
}
