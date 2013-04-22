package com.elster.jupiter.rest.util.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.elster.jupiter.rest.util.RestQueryService;

public class Activator implements BundleActivator {
	
	private volatile ServiceRegistration<RestQueryService> registration;

	public void start(BundleContext bundleContext) throws Exception {
		registration = bundleContext.registerService(RestQueryService.class, new RestQueryServiceImpl(), null);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		registration.unregister();
	}

}
