package com.elster.jupiter.metering.rest;

import java.util.*;

import javax.ws.rs.core.Application;

import org.osgi.framework.*;
import org.osgi.util.tracker.*;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.*;

public class Activator implements BundleActivator, ServiceLocator {

	private volatile ServiceTracker<MeteringService,MeteringService> meteringTracker;
	private volatile ServiceTracker<TransactionService,TransactionService> txManagerTracker;
	private volatile ServiceTracker<RestQueryService,RestQueryService> queryTracker;
	private volatile ServiceRegistration<Application> applicationRegistration;
	
	public void start(BundleContext bundleContext) throws Exception {
		try {			
			meteringTracker = new ServiceTracker<>(bundleContext, MeteringService.class,null);
			meteringTracker.open();			
			txManagerTracker = new ServiceTracker<>(bundleContext, TransactionService.class,null);
			txManagerTracker.open();
			queryTracker = new ServiceTracker<>(bundleContext, RestQueryService.class, null);
			queryTracker.open();
			applicationRegistration = bundleContext.registerService(Application.class, getApplication() , getRegistrationProperties());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Bus.setServiceLocator(this);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		meteringTracker.close();		
		txManagerTracker.close();
		queryTracker.close();
		applicationRegistration.unregister();
		Bus.setServiceLocator(null);
	}
	
	private Application getApplication() {
		return new MeteringApplication();
	}
	
	private Dictionary<String,Object> getRegistrationProperties() {
		Dictionary<String,Object> result = new Hashtable<>();
		result.put("alias", "/kore");
		return result;
	}
	
	@Override
	public MeteringService getMeteringService() {
		return meteringTracker.getService();
	}
	
	@Override
	public TransactionService getTransactionService() {
		return txManagerTracker.getService();
	}
	
	@Override
	public RestQueryService getQueryService() {
		return queryTracker.getService();
	}

}
