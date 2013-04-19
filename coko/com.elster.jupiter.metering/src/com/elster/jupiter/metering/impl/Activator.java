package com.elster.jupiter.metering.impl;

import org.osgi.framework.*;
import org.osgi.util.tracker.*;

import com.elster.jupiter.domain.util.FinderService;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.orm.OrmService;

public class Activator implements BundleActivator , ServiceLocator {

	private volatile BundleContext bundleContext;
	private volatile ServiceTracker<OrmService,OrmClient> ormTracker;
	private volatile ServiceTracker<IdsService,IdsService> idsTracker;
	private volatile ServiceTracker<FinderService, FinderService> finderTracker;
	private volatile ServiceTracker<QueryService, QueryService> queryTracker;
	private volatile ServiceRegistration<MeteringService> meteringServiceRegistration;
	

	public void start(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		ormTracker = new ServiceTracker<>(bundleContext, OrmService.class, new OrmTransformer());
		ormTracker.open();
		idsTracker = new ServiceTracker<>(bundleContext, IdsService.class, null);
		idsTracker.open();
		finderTracker = new ServiceTracker<>(bundleContext, FinderService.class, null);
		finderTracker.open();
		queryTracker = new ServiceTracker<>(bundleContext, QueryService.class, null);
		queryTracker.open();
		meteringServiceRegistration = bundleContext.registerService(MeteringService.class , new MeteringServiceImpl(), null);
		Bus.setServiceLocator(this);
	}

	public void stop(BundleContext bundleContext)  {
		meteringServiceRegistration.unregister();
		ormTracker.close();
		idsTracker.close();
		finderTracker.close();
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
	
	@Override
	public FinderService getFinderService() {
		return finderTracker.getService();
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
