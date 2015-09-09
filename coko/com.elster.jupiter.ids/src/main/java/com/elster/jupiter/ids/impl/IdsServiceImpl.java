package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.ids", service = {IdsService.class, InstallService.class, MessageSeedProvider.class}, property = "name=" + IdsService.COMPONENTNAME)
public class IdsServiceImpl implements IdsService, InstallService, MessageSeedProvider {

    private volatile DataModel dataModel;
    private volatile Clock clock;
    private Thesaurus thesaurus;

    public IdsServiceImpl() {
    }

    @Inject
    public IdsServiceImpl(Clock clock, OrmService ormService, NlsService nlsService) {
        this();
        setClock(clock);
        setOrmService(ormService);
        setNlsService(nlsService);
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
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
    public void install() {
        new InstallerImpl(this, dataModel).install(true, true);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Collections.singletonList("ORM");
    }

    @Override
    public Vault newVault(String component, long id, String name, int slotCount, int textSlotCount, boolean regular) {
        return dataModel.getInstance(VaultImpl.class).init(component, id, name, slotCount, textSlotCount, regular);
    }

    @Override
    public RecordSpec newRecordSpec(String component, long id, String name) {
        return dataModel.getInstance(RecordSpecImpl.class).init(component, id, name);
    }

    @Override
    public void purge(Logger logger) {
    	getVaults().stream()
    		.filter(Vault::isActive)
    		.forEach(vault -> vault.purge(logger));
    }

    @Override
    public void extendTo(Instant instant, Logger logger) {
    	getVaults().stream()
    		.filter(Vault::isActive)
    		.forEach(vault -> vault.extendTo(instant, logger));
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

    @Activate
    public final void activate() {
        dataModel.register(getModule());
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                this.bind(DataModel.class).toInstance(dataModel);
                this.bind(IdsService.class).toInstance(IdsServiceImpl.this);
                this.bind(Clock.class).toInstance(clock);
                this.bind(Thesaurus.class).toInstance(thesaurus);
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
