package com.elster.jupiter.security.thread.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.elster.jupiter.security.thread.ThreadPrincipalService;

public class Activator implements BundleActivator {

	private ServiceRegistration<ThreadPrincipalService> registration;
	
	public void start(BundleContext bundleContext) throws Exception {
		registration = bundleContext.registerService(ThreadPrincipalService.class, new ThreadPrincipalServiceImpl(), null);		
	}

	public void stop(BundleContext bundleContext) throws Exception {
		registration.unregister();
	}

}
