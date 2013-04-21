package com.elster.jupiter.domain.util.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.elster.jupiter.domain.util.QueryService;

public class Activator implements BundleActivator {

	private volatile ServiceRegistration<QueryService> registration;
	
	public void start(BundleContext bundleContext) throws Exception {
		registration = bundleContext.registerService(QueryService.class,new QueryServiceImpl() , null);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		registration.unregister();
	}

}
