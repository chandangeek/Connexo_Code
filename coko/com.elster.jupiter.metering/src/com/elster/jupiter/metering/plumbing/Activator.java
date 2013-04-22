package com.elster.jupiter.metering.plumbing;

import org.osgi.framework.*;
import org.osgi.util.tracker.*;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.impl.MeteringServiceImpl;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.cache.CacheService;

public class Activator implements BundleActivator , ServiceLocator {

	private volatile BundleContext bundleContext;
	private volatile ServiceTracker<OrmService,OrmClient> ormTracker;
	private volatile ServiceTracker<CacheService,CacheService> cacheTracker;
	private volatile ServiceTracker<IdsService,IdsService> idsTracker;
	private volatile ServiceTracker<QueryService, QueryService> queryTracker;
	private volatile ServiceRegistration<MeteringService> meteringServiceRegistration;
	

	public void start(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		ormTracker = new ServiceTracker<>(bundleContext, OrmService.class, new OrmTransformer());
		ormTracker.open();
		cacheTracker = new ServiceTracker<>(bundleContext, CacheService.class, null);
		cacheTracker.open();		
		idsTracker = new ServiceTracker<>(bundleContext, IdsService.class, null);
		idsTracker.open();
		queryTracker = new ServiceTracker<>(bundleContext, QueryService.class, null);
		queryTracker.open();
		meteringServiceRegistration = bundleContext.registerService(MeteringService.class , new MeteringServiceImpl(), null);
		Bus.setServiceLocator(this);
	}

	public void stop(BundleContext bundleContext)  {
		meteringServiceRegistration.unregister();
		ormTracker.close();
		cacheTracker.close();
		idsTracker.close();
		queryTracker.close();
		Bus.setServiceLocator(null);
	}

	@Override
	public IdsService getIdsService() {
		return idsTracker.getService();			
	}
	
	@Override
	public OrmClient getOrmClient() {
		return ormTracker.getService();
	}

	public CacheService getCacheService() {
		return cacheTracker.getService();
	}
	
	@Override
	public QueryService getQueryService() {
		return queryTracker.getService();
	}
	
	private class OrmTransformer implements ServiceTrackerCustomizer<OrmService,OrmClient> {
		
		OrmTransformer() {			
		}
		
		@Override
		public OrmClient addingService(ServiceReference<OrmService> serviceReference) {
			return new OrmClientImpl(bundleContext.getService(serviceReference));
		}

		@Override
		public void modifiedService(ServiceReference<OrmService> serviceReference, OrmClient client) {				
		}

		@Override
		public void removedService(ServiceReference<OrmService> serviceReference, OrmClient client) {
			bundleContext.ungetService(serviceReference);
		}
	}

		
}
