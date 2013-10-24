package com.elster.jupiter.unifyingjs;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.elster.jupiter.http.whiteboard.*;


public class Activator implements BundleActivator {

	private volatile ServiceRegistration<HttpResource> registration;
	
	public void start(BundleContext bundleContext) throws Exception {
        HttpResource resource = new HttpResource("/uni", "/js/uni" , new BundleResolver(bundleContext));
		registration = bundleContext.registerService(HttpResource.class, resource , null);
	}
	
	public void stop(BundleContext bundleContext) throws Exception {
		registration.unregister();
	}

}
