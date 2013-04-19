package com.elster.jupiter.transaction.impl;

import java.sql.SQLException;

import org.osgi.framework.*;
import org.osgi.util.tracker.*;

import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.bootstrap.*;

import javax.sql.DataSource;

public class Activator implements BundleActivator {

	private volatile ServiceTracker<BootstrapService, BootstrapService> bootstrapTracker;
	private volatile ServiceRegistration<DataSource> dataSourceRegistration;
	private volatile ServiceRegistration<TransactionService> transactionManagerRegistration;

	public void start(BundleContext context) throws SQLException  {
		bootstrapTracker = new ServiceTracker<>(context, BootstrapService.class, new BootstrapCustomizer(context));
		bootstrapTracker.open();		
	}

	public void stop(BundleContext bundleContext) {
		bootstrapTracker.close();
	}
	
	void registerServices(BundleContext context , DataSource source) {
		TransactionServiceImpl transactionManager = new TransactionServiceImpl(source);
		TransactionalDataSource txSource = new TransactionalDataSource(transactionManager);
		dataSourceRegistration = context.registerService(DataSource.class,txSource,null);
		transactionManagerRegistration = context.registerService(TransactionService.class, transactionManager, null);		
	}
	
	void unregisterServices() {	
		dataSourceRegistration.unregister();
		transactionManagerRegistration.unregister();
	}

	private class BootstrapCustomizer implements ServiceTrackerCustomizer<BootstrapService, BootstrapService> {
		private final BundleContext context;
		
		BootstrapCustomizer(BundleContext context) {
			this.context = context;
		}
		
		@Override
		public BootstrapService addingService(ServiceReference<BootstrapService> reference) {
			BootstrapService bootstrapService = context.getService(reference);
			registerServices(context,bootstrapService.getDataSource());			
			return bootstrapService;
		}

		@Override
		public void modifiedService(ServiceReference<BootstrapService> reference,BootstrapService service) {			
		}

		@Override
		public void removedService(ServiceReference<BootstrapService> reference, BootstrapService service) {
			unregisterServices();
			
		}
	}
	
}
