package com.elster.jupiter.rest.util.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.elster.jupiter.domain.util.FinderService;
import com.elster.jupiter.rest.util.RestQueryService;

public class Activator implements BundleActivator {
	
	private volatile ServiceTracker<FinderService, FinderService> finderTracker;
	private volatile ServiceRegistration<RestQueryService> registration;

	public void start(BundleContext bundleContext) throws Exception {
		finderTracker = new ServiceTracker<>(bundleContext, FinderService.class, null);
		finderTracker.open();
		registration = bundleContext.registerService(RestQueryService.class, new RestQueryServiceImpl(), null);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		finderTracker.close();
		registration.unregister();
	}

}
