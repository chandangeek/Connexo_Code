package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiBuilder;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import org.joda.time.MutableDateTime;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.kpi", service = {KpiService.class, InstallService.class}, property = "name=" + KpiService.COMPONENT_NAME)
public class KpiServiceImpl implements IKpiService, InstallService {

    private static final Logger LOGGER = Logger.getLogger(KpiServiceImpl.class.getName());

    private static final long VAULT_ID = 1L;
    private static final long RECORD_SPEC_ID = 1L;
    private static final int MONTHS_PER_YEAR = 12;

    private volatile IdsService idsService;
    private volatile EventService eventService;
    private volatile DataModel dataModel;
    private volatile Vault vault;
    private volatile RecordSpec recordSpec;

    public KpiServiceImpl() {
    }

    @Inject
    KpiServiceImpl(IdsService idsService, EventService eventService, OrmService ormService) {
        setIdsService(idsService);
        setEventService(eventService);
        setOrmService(ormService);
        activate();
        install();
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

    @Override
    public final void install() {
        try {
            dataModel.install(true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Vault newVault = idsService.newVault(COMPONENT_NAME, VAULT_ID, COMPONENT_NAME, 2, 0, true);
            newVault.persist();
            createPartitions(newVault);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            RecordSpec newRecordSpec = idsService.newRecordSpec(COMPONENT_NAME, RECORD_SPEC_ID, "kpi");
            newRecordSpec.addFieldSpec("value", FieldType.NUMBER);
            newRecordSpec.addFieldSpec("target", FieldType.NUMBER);
            newRecordSpec.persist();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            createEventTypes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        initVaultAndRecordSpec();
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.install(eventService);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Could not create event type : " + eventType.name(), e);
            }
        }
    }

    private void createPartitions(Vault vault) {
        MutableDateTime startOfMonth = new MutableDateTime();
        startOfMonth.setMillisOfDay(0);
        startOfMonth.setMonthOfYear(1);
        startOfMonth.setDayOfMonth(1);
        vault.activate(startOfMonth.toDate());
        for (int i = 0; i < MONTHS_PER_YEAR; i++) {
            startOfMonth.addMonths(1);
            vault.addPartition(startOfMonth.toDate());
        }
    }


    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENT_NAME, "CIM Metering");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public final void setIdsService(IdsService idsService) {
        this.idsService = idsService;
        initVaultAndRecordSpec();
    }

    private void initVaultAndRecordSpec() {
        Optional<Vault> foundVault = idsService.getVault(COMPONENT_NAME, VAULT_ID);
        if (foundVault.isPresent()) {
            vault = foundVault.get();
        }
        Optional<RecordSpec> foundSpec = idsService.getRecordSpec(COMPONENT_NAME, RECORD_SPEC_ID);
        if (foundSpec.isPresent()) {
            recordSpec = foundSpec.get();
        }
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
