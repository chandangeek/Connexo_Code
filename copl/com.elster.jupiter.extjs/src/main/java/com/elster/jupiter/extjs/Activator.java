package com.elster.jupiter.extjs;

import org.osgi.framework.*;
import com.elster.jupiter.http.whiteboard.*;

public class Activator implements BundleActivator {

	private volatile ServiceRegistration<HttpResource> registration;
	
	public void start(BundleContext bundleContext) throws Exception {
		HttpResource resource = new HttpResource("/ext", "/js/ext" , new BundleResolver(bundleContext));
		registration = bundleContext.registerService(HttpResource.class, resource , null);
	}
	
	public void stop(BundleContext bundleContext) throws Exception {
		registration.unregister();
	}
	
}