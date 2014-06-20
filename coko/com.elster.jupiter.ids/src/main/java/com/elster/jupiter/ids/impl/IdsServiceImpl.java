package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.ids.plumbing.InstallerImpl;
import com.elster.jupiter.ids.plumbing.TableSpecs;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.ids", service = {IdsService.class, InstallService.class}, property = "name=" + IdsService.COMPONENTNAME)
public class IdsServiceImpl implements IdsService, InstallService {

	private volatile DataModel dataModel;
    private volatile Clock clock;

    public IdsServiceImpl() {
    }

    @Inject
    public IdsServiceImpl(Clock clock, OrmService ormService) {
        setClock(clock);
        setOrmService(ormService);
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
    public TimeSeriesDataStorer createStorer(boolean overrules) {
        return new TimeSeriesDataStorerImpl(dataModel,clock,overrules);
    }

    @Override
    public void install() {
        new InstallerImpl(this,dataModel).install(true, true);
    }

    @Override
    public Vault newVault(String component, long id, String name, int slotCount, boolean regular) {
        return dataModel.getInstance(VaultImpl.class).init(component, id, name, slotCount, regular);
    }

    @Override
    public RecordSpec newRecordSpec(String component, long id, String name) {
        return dataModel.getInstance(RecordSpecImpl.class).init(component, id, name);
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
    
    @Activate 
    public final void activate() {
    	dataModel.register(getModule());
    }
    
    private Module getModule() {
    	return new AbstractModule() {
			@Override
			protected void configure() {
				this.bind(DataModel.class).toInstance(dataModel);
				this.bind(Clock.class).toInstance(clock);
			}
    	};
    }
}
