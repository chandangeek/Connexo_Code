/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiBuilder;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.V10_2SimpleUpgrader;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Optional;

import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;

@Component(name = "com.elster.jupiter.kpi", service = {KpiService.class}, property = "name=" + KpiService.COMPONENT_NAME)
public class KpiServiceImpl implements IKpiService {

    static final long VAULT_ID = 1L;
    static final long RECORD_SPEC_ID = 1L;

    private volatile IdsService idsService;
    private volatile EventService eventService;
    private volatile DataModel dataModel;
    private volatile Vault vault;
    private volatile RecordSpec recordSpec;
    private volatile UpgradeService upgradeService;

    public KpiServiceImpl() {
    }

    @Inject
    KpiServiceImpl(IdsService idsService, EventService eventService, OrmService ormService, UpgradeService upgradeService) {
        setIdsService(idsService);
        setEventService(eventService);
        setOrmService(ormService);
        setUpgradeService(upgradeService);
        activate();
    }

    @Activate
    public final void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(IdsService.class).toInstance(idsService);
                bind(EventService.class).toInstance(eventService);
                bind(IKpiService.class).toInstance(KpiServiceImpl.this);
            }
        });
        upgradeService.register(identifier("Pulse", COMPONENT_NAME), dataModel, Installer.class, V10_2SimpleUpgrader.V10_2_UPGRADER);
        initVaultAndRecordSpec();
    }

    @Deactivate
    public final void deactivate() {

    }

    @Override
    public KpiBuilder newKpi() {
        if (vault == null) {
            throw new IllegalStateException("Module not installed yet.");
        }
        return new KpiBuilderImpl(dataModel);
    }

    @Override
    public Optional<Kpi> getKpi(long id) {
        return dataModel.mapper(Kpi.class).getOptional(id);
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENT_NAME, "Key Performance Indicators");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public final void setIdsService(IdsService idsService) {
        this.idsService = idsService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    private void initVaultAndRecordSpec() {
        vault = idsService.getVault(COMPONENT_NAME, VAULT_ID).orElse(null);
        recordSpec = idsService.getRecordSpec(COMPONENT_NAME, RECORD_SPEC_ID).orElse(null);
    }

    public EventService getEventService() {
        return eventService;
    }

    @Reference
    public final void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public Vault getVault() {
        return vault;
    }

    @Override
    public RecordSpec getRecordSpec() {
        return recordSpec;
    }
}
