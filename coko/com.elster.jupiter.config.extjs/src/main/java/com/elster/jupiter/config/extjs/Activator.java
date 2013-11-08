package com.elster.jupiter.config.extjs;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.elster.jupiter.http.whiteboard.*;


public class Activator implements BundleActivator {

	private volatile ServiceRegistration<HttpResource> registration;
	
	public void start(BundleContext bundleContext) throws Exception {
		String alias = "/cfg";
		HttpResource resource = new HttpResource(alias, "/js/cfg" , new BundleResolver(bundleContext));
		// Comment above and uncomment next line for file based javascript serving, changing second argument as appropriate
		//HttpResource resource = new HttpResource(alias, "/home/lvz/Documents/Workspace/Jupiter/meteringextjs/js/mtr" , new FileResolver());
		registration = bundleContext.registerService(HttpResource.class, resource , null);
	}
	
	public void stop(BundleContext bundleContext) throws Exception {
		registration.unregister();
	}

}
