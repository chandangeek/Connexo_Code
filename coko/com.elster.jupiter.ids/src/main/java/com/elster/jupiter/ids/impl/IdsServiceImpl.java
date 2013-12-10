package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.ids.plumbing.Bus;
import com.elster.jupiter.ids.plumbing.InstallerImpl;
import com.elster.jupiter.ids.plumbing.OrmClient;
import com.elster.jupiter.ids.plumbing.OrmClientImpl;
import com.elster.jupiter.ids.plumbing.ServiceLocator;
import com.elster.jupiter.ids.plumbing.TableSpecs;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.ids", service = {IdsService.class, InstallService.class}, property = "name=" + Bus.COMPONENTNAME)
public class IdsServiceImpl implements IdsService, InstallService, ServiceLocator {

    private volatile OrmClient ormClient;
    private volatile Clock clock;

    public IdsServiceImpl() {
    }

    @Inject
    public IdsServiceImpl(Clock clock, OrmService ormService) {
        setClock(clock);
        setOrmService(ormService);
        activate();
        install();
    }

    @Override
    public Optional<Vault> getVault(String component, long id) {
        return getOrmClient().getVaultFactory().get(component, id);
    }

    @Override
    public Optional<RecordSpec> getRecordSpec(String component, long id) {
        return getOrmClient().getRecordSpecFactory().get(component, id);
    }

    @Override
    public Optional<TimeSeries> getTimeSeries(long id) {
        return getOrmClient().getTimeSeriesFactory().get(id);
    }

    @Override
    public TimeSeriesDataStorer createStorer(boolean overrules) {
        return new TimeSeriesDataStorerImpl(overrules);
    }

    @Override
    public void install() {
        new InstallerImpl().install(true, true, true);
    }

    @Override
    public OrmClient getOrmClient() {
        return ormClient;
    }

    @Override
    public Vault newVault(String component, long id, String name, int slotCount, boolean regular) {
        return new VaultImpl(component, id, name, slotCount, regular);
    }

    @Override
    public RecordSpec newRecordSpec(String component, long id, String name) {
        return new RecordSpecImpl(component, id, name);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "TimeSeries Data Store");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        ormService.register(dataModel);
        ormClient = new OrmClientImpl(dataModel);
    }

    @Activate
    public void activate() {
        Bus.setServiceLocator(this);
    }

    @Deactivate
    public void deactivate() {
        Bus.clearServiceLocator(this);
    }

    public Clock getClock() {
        return clock;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
