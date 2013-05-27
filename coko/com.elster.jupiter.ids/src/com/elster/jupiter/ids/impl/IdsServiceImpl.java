package com.elster.jupiter.ids.impl;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import com.elster.jupiter.ids.*;
import com.elster.jupiter.ids.plumbing.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;

@Component (name="com.elster.jupiter.ids", service={IdsService.class,InstallService.class}, property="name="+Bus.COMPONENTNAME)
public class IdsServiceImpl implements IdsService, InstallService, ServiceLocator {
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
	public void install() {
		new InstallerImpl().install(true,true,true);
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
    	DataModel dataModel = ormService.getDataModel(Bus.COMPONENTNAME);
    	if (dataModel == null) {
    		dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "TimeSeries Data Store");
    		for (TableSpecs spec : TableSpecs.values()) {
    			spec.addTo(dataModel);			
    		}	
    	}
    	ormClient = new OrmClientImpl(dataModel);    		
    }
    
    public void activate(ComponentContext context) {
    	Bus.setServiceLocator(this);
    }

    public void deActivate(ComponentContext context) {
    	Bus.setServiceLocator(null);
    }
    		
}
