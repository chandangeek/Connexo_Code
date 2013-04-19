package com.elster.jupiter.ids.impl;

import org.osgi.framework.*;
import org.osgi.util.tracker.*;

import com.elster.jupiter.ids.*;
import com.elster.jupiter.orm.OrmService;

public class Activator implements BundleActivator , ServiceLocator {

	private static final long  WAITTIMEOUT = 10000L;

	private volatile BundleContext bundleContext;
	private volatile ServiceTracker<OrmService,OrmClient> ormTracker;
	private volatile ServiceRegistration<IdsService> idsRegistration;

	public void start(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		ormTracker = new ServiceTracker<>(bundleContext, OrmService.class, new OrmTransformer());
		ormTracker.open();
		idsRegistration = bundleContext.registerService(IdsService.class , new IdsServiceImpl(), null);
		Bus.setServiceLocator(this);
	}

	public void stop(BundleContext bundleContext)  {
		idsRegistration.unregister();
		ormTracker.close();
		Bus.setServiceLocator(null);
	}

	public OrmClient getOrmClient() {
		try {
			OrmClient client = ormTracker.waitForService(WAITTIMEOUT);
			if (client == null) {
				throw new IllegalStateException("Orm Service not available");
			}
			return client;
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
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
