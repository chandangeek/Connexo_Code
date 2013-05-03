package com.elster.jupiter.metering.impl;

import static com.elster.jupiter.metering.plumbing.Bus.COMPONENTNAME;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.plumbing.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.orm.cache.ComponentCache;

@Component (name = "com.elster.jupiter.metering", service=MeteringService.class)
public class MeteringServiceImpl implements MeteringService , ServiceLocator {

	private volatile OrmClient ormClient;
	private volatile ComponentCache componentCache;
	private volatile IdsService idsService;
	private volatile QueryService queryService;

	
	@Override 
	public ServiceCategory getServiceCategory(ServiceKind kind) {
		return getOrmClient().getServiceCategoryFactory().get(kind);
	}
	
	@Override
	public ReadingType getReadingType(String mRid) {
		return getOrmClient().getReadingTypeFactory().get(mRid);
	}
	
	@Override
	public void install(boolean executeDdl,boolean storeMappings , boolean createMasterData) {
		new InstallerImpl().install(executeDdl, storeMappings, createMasterData);
	}
	
	@Override
	public ServiceLocation newServiceLocation() {	
		return new ServiceLocationImpl();
	}

	@Override
	public ServiceLocation findServiceLocation(String mRID) {
		return getOrmClient().getServiceLocationFactory().getUnique("mRID", mRID);				
	}

	@Override
	public ServiceLocation findServiceLocation(long id) {
		return getOrmClient().getServiceLocationFactory().get(id);				
	}
	
	@Override
	public UsagePoint findUsagePoint(long id) {
		return getOrmClient().getUsagePointFactory().get(id);				
	}
	
	@Override
	public ReadingStorer createStorer(boolean overrules) {
		return new ReadingStorerImpl(overrules);
	}

	@Override 
	public Query<UsagePoint> getUsagePointQuery() {
		return getQueryService().wrap(
			getOrmClient().getUsagePointFactory().with(
				getOrmClient().getServiceLocationFactory(),
				getOrmClient().getMeterActivationFactory(),
			//	getOrmClient().getChannelFactory(),
				getOrmClient().getMeterFactory()));		
	}

	@Override
	public Query<MeterActivation> getMeterActivationQuery() {
		return getQueryService().wrap(
			getOrmClient().getMeterActivationFactory().with(
				getOrmClient().getUsagePointFactory(),
				getOrmClient().getMeterFactory(),
				getOrmClient().getServiceLocationFactory()));					
	}
	
	@Override
	public Query<ServiceLocation> getServiceLocationQuery() {
		return getQueryService().wrap(
			getOrmClient().getServiceLocationFactory().with(
				getOrmClient().getUsagePointFactory(),
				getOrmClient().getMeterActivationFactory(),
				//getOrmClient().getChannelFactory(),
				getOrmClient().getMeterFactory()));										
	}

	@Override
	public OrmClient getOrmClient() {
		return ormClient;
	}

	@Override
	public ComponentCache getComponentCache() {
		return componentCache;
	}

	@Override
	public IdsService getIdsService() {
		return idsService;
	}

	@Override
	public QueryService getQueryService() {
		return queryService;
	}

	@Reference
	public void setOrmService(OrmService ormService) {
		DataModel dataModel = ormService.getDataModel(Bus.COMPONENTNAME);
		if (dataModel == null) {
			dataModel = ormService.newDataModel(COMPONENTNAME, "CIM Metering");
			for (TableSpecs spec : TableSpecs.values()) {
				spec.addTo(dataModel);			
			}						
		}
		this.ormClient = new OrmClientImpl(dataModel);
	}
	
	@Reference(name = "ZCacheService")
	public void setCacheService(CacheService cacheService) {
		this.componentCache = cacheService.getComponentCache(ormClient.getDataModel());
	}
	
	@Reference
	public void setIdsService(IdsService idsService) {
		this.idsService = idsService;
	}
	
	@Reference
	public void setQueryService(QueryService queryService) {
		this.queryService = queryService;
	}

	@Activate	
	public void activate() {
		Bus.setServiceLocator(this);		
	}
	
	@Deactivate
	public void deactivate() {
		Bus.setServiceLocator(null);
	}
	
}
