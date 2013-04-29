package com.elster.jupiter.ids.impl;

import org.osgi.service.component.annotations.*;
import com.elster.jupiter.ids.*;
import com.elster.jupiter.ids.plumbing.*;
import com.elster.jupiter.orm.OrmService;

@Component (name = "com.elster.jupiter.ids" , service = IdsService.class)
public class IdsServiceImpl implements IdsService, ServiceLocator {
	private volatile OrmClient ormClient;
	
	@Override
	public Vault getVault(String component, long id) {
		return getOrmClient().getVaultFactory().get(component,id);
	}

	@Override
	public RecordSpec getRecordSpec(String component, long id) {
		return getOrmClient().getRecordSpecFactory().get(component,id);
	}

	@Override
	public TimeSeries getTimeSeries(long id) {
		return getOrmClient().getTimeSeriesFactory().get(id);
	}
	
	@Override
	public TimeSeriesDataStorer createStorer(boolean overrules) {
		return new TimeSeriesDataStorerImpl(overrules);
	}
	
	@Override
	public void install(boolean executeDdl,boolean storeMappings , boolean createMasterData) {
		new InstallerImpl().install(executeDdl, storeMappings, createMasterData);
	}
	
	@Override
	public OrmClient getOrmClient() {
		return ormClient;
	}

	@Override
	public Vault newVault(String component, long id, String name, int slotCount , boolean regular) {
		return new VaultImpl(component,id,name,slotCount,regular);
	}

	@Override
	public RecordSpec newRecordSpec(String component, long id,String name) {
		return new RecordSpecImpl(component, id, name);
	}

    @Reference 
    public void setOrmService(OrmService ormService) {
    	this.ormClient = new OrmClientImpl(ormService);
    }
    
    @Activate
    public void activate() {
    	Bus.setServiceLocator(this);
    }
    
    @Deactivate
    public void deActivate() {
    	Bus.setServiceLocator(null);
    }
}
