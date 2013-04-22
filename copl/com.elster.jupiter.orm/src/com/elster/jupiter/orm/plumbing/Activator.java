package com.elster.jupiter.orm.plumbing;

import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import javax.sql.DataSource;

import java.security.Principal;
import java.sql.*;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.impl.OrmServiceImpl;
import com.elster.jupiter.security.thread.ThreadPrincipalService;


public class Activator implements BundleActivator , ServiceLocator {
 
	private volatile BundleContext bundleContext;
	private volatile ServiceTracker<DataSource, DataSource> dataSourceTracker;
	private volatile ServiceTracker<ThreadPrincipalService, ThreadPrincipalService> principalTracker;
	private volatile ServiceTracker<OrmService, OrmClient>  ormTracker;
	private volatile ServiceRegistration< OrmService> ormServiceRegistration;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		this.bundleContext = bundleContext;
		dataSourceTracker = new ServiceTracker<>(bundleContext, DataSource.class, null);
		dataSourceTracker.open();
		principalTracker = new ServiceTracker<>(bundleContext,  ThreadPrincipalService.class, null);
		principalTracker.open();
		OrmService service = new OrmServiceImpl();
		ormServiceRegistration = bundleContext.registerService(OrmService.class,service,null);
		ormTracker = new ServiceTracker<>(bundleContext, OrmService.class, new OrmTransformer());
		ormTracker.open();
		Bus.setServiceLocator(this);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		ormServiceRegistration.unregister();		
		dataSourceTracker.close();
		principalTracker.close();
		ormTracker.close();
		Bus.setServiceLocator(null);
	}
	
	@Override
	public OrmClient getOrmClient() {
		return ormTracker.getService();
	}
	
	@Override
	public Principal getPrincipal() {
		return principalTracker.getService().getPrincipal();
	}
	
	@Override
	public Connection getConnection(boolean transactionRequired) throws SQLException {		
		DataSource source = dataSourceTracker.getService();
		if (source == null) {
			throw new SQLException("DataSource service not available");
		}
		Connection result = source.getConnection();
		if (transactionRequired && result.getAutoCommit()) {
			result.close();
			throw new TransactionRequiredException();
		}
		return result;	
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
