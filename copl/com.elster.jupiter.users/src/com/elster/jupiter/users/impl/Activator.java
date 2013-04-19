package com.elster.jupiter.users.impl;

import org.osgi.framework.*;
import org.osgi.util.tracker.*;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;

public class Activator implements BundleActivator,ServiceLocator {
	private static final long  WAITTIMEOUT = 10000L;

	private volatile BundleContext bundleContext;
	private volatile ServiceTracker<OrmService,OrmClient> ormTracker;
	private volatile ServiceTracker<TransactionService, TransactionService> txMgrTracker;
	private volatile ServiceRegistration<UserService> userServiceRegistration;

	public void start(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		ormTracker = new ServiceTracker<>(bundleContext, OrmService.class, new OrmTransformer());
		ormTracker.open();
		txMgrTracker = new ServiceTracker<>(bundleContext, TransactionService.class,null);
		txMgrTracker.open();		
		userServiceRegistration = bundleContext.registerService(UserService.class, new UserServiceImpl(),null);
		Bus.setServiceLocator(this);
	}

	public void stop(BundleContext bundleContext)  {		
		ormTracker.close();
		txMgrTracker.close();
		userServiceRegistration.unregister();
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
	
	public TransactionService getTransactionManager() {
		try {
			TransactionService txMgr = txMgrTracker.waitForService(WAITTIMEOUT);
			if (txMgr == null) {
				throw new IllegalStateException("Transaction Manager not available");
			}
			return txMgr;
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
